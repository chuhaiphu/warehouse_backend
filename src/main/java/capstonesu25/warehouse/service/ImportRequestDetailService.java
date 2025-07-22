package capstonesu25.warehouse.service;

import capstonesu25.warehouse.annotation.transactionLog.TransactionLoggable;
import capstonesu25.warehouse.entity.*;
import capstonesu25.warehouse.enums.ImportType;
import capstonesu25.warehouse.enums.RequestStatus;
import capstonesu25.warehouse.model.importrequest.ImportRequestResponse;
import capstonesu25.warehouse.model.importrequest.importrequestdetail.ImportRequestCreateWithDetailRequest;
import capstonesu25.warehouse.model.importrequest.importrequestdetail.ImportRequestDetailResponse;
import capstonesu25.warehouse.repository.*;
import capstonesu25.warehouse.utils.Mapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ImportRequestDetailService {
    private final ImportRequestRepository importRequestRepository;
    private final ImportRequestDetailRepository importRequestDetailRepository;  
    private final ItemRepository itemRepository;
    private final ConfigurationRepository configurationRepository;
    private static final Logger LOGGER = LoggerFactory.getLogger(ImportRequestDetailService.class);
    private final ProviderRepository providerRepository;

    @TransactionLoggable(type = "IMPORT_REQUEST", action = "CREATE", objectIdSource = "importRequestId")
    public List<ImportRequestResponse> createImportRequestWithDetails(List<ImportRequestCreateWithDetailRequest> detailRequests) {
        LOGGER.info("Creating import request detail svc");
        
        // Validate that all items belong to the same provider
        checkSameProvider(detailRequests);
        
        // Get common data from first request
        ImportRequestCreateWithDetailRequest firstRequest = detailRequests.get(0);
        String importReason = firstRequest.getImportReason();
        ImportType importType = firstRequest.getImportType();

        OptionalInt latestBatchSuffix = findLatestBatchSuffixForToday();
        int batchSuffix = latestBatchSuffix.isPresent() ? latestBatchSuffix.getAsInt() + 1 : 1;

        // Group by providerId
        Map<Long, List<ImportRequestCreateWithDetailRequest>> requestsByProvider = new HashMap<>();
        for (ImportRequestCreateWithDetailRequest req : detailRequests) {
            requestsByProvider
                    .computeIfAbsent(req.getProviderId(), k -> new ArrayList<>())
                    .add(req);
        }

        List<ImportRequestResponse> createdImportRequestResponses = new ArrayList<>();

        // Process each provider group
        for (Map.Entry<Long, List<ImportRequestCreateWithDetailRequest>> entry : requestsByProvider.entrySet()) {
            Long providerId = entry.getKey();
            List<ImportRequestCreateWithDetailRequest> requests = entry.getValue();

            // Create ImportRequest for each provider
            ImportRequest importRequest = new ImportRequest();
            importRequest.setId(createImportRequestId());
            importRequest.setImportReason(importReason);
            importRequest.setStatus(RequestStatus.NOT_STARTED);
            importRequest.setType(importType);
            importRequest.setBatchCode(getTodayPrefix() + batchSuffix);

            Configuration configuration = configurationRepository.findAll()
                    .stream()
                    .findFirst()
                    .orElseThrow(() -> new NoSuchElementException("Configuration not found"));

            LocalDate startDate = LocalDate.now();
            LocalDate endDate = startDate.plusDays(configuration.getMaxAllowedDaysForImportRequestProcess());

            if(firstRequest.getEndDate() != null) {
                if(firstRequest.getEndDate().isBefore(firstRequest.getStartDate())) {
                    throw new IllegalArgumentException("End date cannot be before start date.");
                }

                long daysBetween = ChronoUnit.DAYS.between(firstRequest.getStartDate(), firstRequest.getEndDate());

                if (daysBetween > configuration.getMaxAllowedDaysForImportRequestProcess()) {
                    throw new IllegalArgumentException("End date cannot be after the maximum allowed date for import request processing.");
                }

                endDate = firstRequest.getEndDate();
            }

            if(firstRequest.getStartDate() != null) {
                if(firstRequest.getStartDate().isAfter(endDate)) {
                    throw new IllegalArgumentException("Start date cannot be after end date.");
                }

                if(firstRequest.getStartDate().isBefore(LocalDate.now())) {
                    throw new IllegalArgumentException("Start date cannot be in the past.");
                }

                startDate = firstRequest.getStartDate();
            }

            importRequest.setStartDate(startDate);
            importRequest.setEndDate(endDate);

            // Set export request if provided

            // Set provider
            Provider provider = providerRepository.findById(providerId)
                    .orElseThrow(() -> new RuntimeException("Provider not found with ID: " + providerId));
            importRequest.setProvider(provider);

            // Save the import request
            ImportRequest savedImportRequest = importRequestRepository.save(importRequest);

            // Create import request details
            List<ImportRequestDetail> savedDetails = new ArrayList<>();
            for (ImportRequestCreateWithDetailRequest req : requests) {
                ImportRequestDetail detail = new ImportRequestDetail();
                detail.setImportRequest(savedImportRequest);
                detail.setExpectQuantity(req.getQuantity());
                detail.setItem(itemRepository.findById(req.getItemId())
                        .orElseThrow(() -> new RuntimeException("Item not found with ID: " + req.getItemId())));
                detail.setActualQuantity(0);
                detail.setOrderedQuantity(0);
                ImportRequestDetail savedDetail = importRequestDetailRepository.save(detail);
                savedDetails.add(savedDetail);
            }

            // Set the saved details to the saved import request for mapping
            savedImportRequest.setDetails(savedDetails);
            
            // Convert to response and add to result list
            ImportRequestResponse response = Mapper.mapToImportRequestResponse(savedImportRequest);
            createdImportRequestResponses.add(response);
        }
        
        return createdImportRequestResponses;
    }

    public void deleteImportRequestDetail(Long importRequestDetailId) {
        LOGGER.info("Deleting import request detail");
        importRequestDetailRepository.deleteById(importRequestDetailId);
    }

    public List<ImportRequestDetailResponse>getImportRequestDetailsByImportRequestId(String importRequestId) {
        LOGGER.info("Getting import request detail for ImportRequest ID: {}", importRequestId);
        List<ImportRequestDetail> importRequestDetails = importRequestDetailRepository
                .findImportRequestDetailsByImportRequest_Id(importRequestId);

        return importRequestDetails.stream().map(Mapper::mapToImportRequestDetailResponse).toList();
    }

    public ImportRequestDetailResponse getImportRequestDetailById(Long importRequestDetailId) {
        LOGGER.info("Getting import request detail for ImportRequestDetail ID: {}", importRequestDetailId);
        ImportRequestDetail importRequestDetail = importRequestDetailRepository.findById(importRequestDetailId).orElseThrow();
        return Mapper.mapToImportRequestDetailResponse(importRequestDetail);
    }

    private OptionalInt findLatestBatchSuffixForToday() {
        LOGGER.info("Finding latest batch suffix for today");
        List<ImportRequest> requests = importRequestRepository.findByBatchCodeStartingWith(getTodayPrefix());

        return requests.stream()
                .map(ImportRequest::getBatchCode)
                .map(code -> code.substring(getTodayPrefix().length()))
                .mapToInt(Integer::parseInt)
                .max(); // returns OptionalInt
    }

    private void checkSameProvider(List<ImportRequestCreateWithDetailRequest> request) {
        for(ImportRequestCreateWithDetailRequest itemOrder : request) {
            Item item = itemRepository.findById(itemOrder.getItemId())
                    .orElseThrow(() -> new NoSuchElementException("Item not found with ID: " + itemOrder.getItemId()));
            boolean providerMatch = item.getProviders().stream()
                    .anyMatch(provider -> Objects.equals(provider.getId(), itemOrder.getProviderId()));

            if (!providerMatch) {
                throw new IllegalArgumentException("Item with ID: " + itemOrder.getItemId() +
                        " does not belong to the provider with ID: " +  itemOrder.getProviderId());
            }
        }
    }

    private String createImportRequestId() {
        String prefix = "PN";
        LocalDate today = LocalDate.now();

        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

        int todayCount = importRequestRepository.countByCreatedAtBetween(startOfDay, endOfDay);

        String datePart = today.format(DateTimeFormatter.BASIC_ISO_DATE);
        String sequence = String.format("%03d", todayCount + 1);

        return String.format("%s-%s-%s", prefix, datePart, sequence);
    }

    private String getTodayPrefix() {
        return LocalDate.now() + "_";
    }
}
