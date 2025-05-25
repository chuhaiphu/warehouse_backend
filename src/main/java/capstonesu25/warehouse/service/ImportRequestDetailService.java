package capstonesu25.warehouse.service;

import capstonesu25.warehouse.entity.ImportRequest;
import capstonesu25.warehouse.entity.ImportRequestDetail;
import capstonesu25.warehouse.entity.Item;
import capstonesu25.warehouse.entity.Provider;
import capstonesu25.warehouse.enums.ImportType;
import capstonesu25.warehouse.enums.RequestStatus;
import capstonesu25.warehouse.model.importrequest.importrequestdetail.ImportRequestCreateWithDetailRequest;
import capstonesu25.warehouse.model.importrequest.importrequestdetail.ImportRequestDetailResponse;
import capstonesu25.warehouse.repository.ExportRequestRepository;
import capstonesu25.warehouse.repository.ImportRequestDetailRepository;
import capstonesu25.warehouse.repository.ImportRequestRepository;
import capstonesu25.warehouse.repository.ItemRepository;
import capstonesu25.warehouse.repository.ProviderRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ImportRequestDetailService {
    private final ImportRequestRepository importRequestRepository;
    private final ImportRequestDetailRepository importRequestDetailRepository;
    private final ExportRequestRepository exportRequestRepository;
    private final ItemRepository itemRepository;
    private static final Logger LOGGER = LoggerFactory.getLogger(ImportRequestDetailService.class);
    private final ProviderRepository providerRepository;

    public List<String> createImportRequestDetail(List<ImportRequestCreateWithDetailRequest> detailRequests) {
        LOGGER.info("Creating import request detail svc");
        
        // Validate that all items belong to the same provider
        checkSameProvider(detailRequests);
        
        // Get common data from first request
        ImportRequestCreateWithDetailRequest firstRequest = detailRequests.get(0);
        String importReason = firstRequest.getImportReason();
        ImportType importType = firstRequest.getImportType();
        String exportRequestId = firstRequest.getExportRequestId();

        OptionalInt latestBatchSuffix = findLatestBatchSuffixForToday();
        int batchSuffix = latestBatchSuffix.isPresent() ? latestBatchSuffix.getAsInt() + 1 : 1;

        // Group by providerId
        Map<Long, List<ImportRequestCreateWithDetailRequest>> requestsByProvider = new HashMap<>();
        for (ImportRequestCreateWithDetailRequest req : detailRequests) {
            requestsByProvider
                    .computeIfAbsent(req.getProviderId(), k -> new ArrayList<>())
                    .add(req);
        }

        List<String> createdImportRequestIds = new ArrayList<>();

        // Process each provider group
        for (Map.Entry<Long, List<ImportRequestCreateWithDetailRequest>> entry : requestsByProvider.entrySet()) {
            Long providerId = entry.getKey();
            List<ImportRequestCreateWithDetailRequest> requests = entry.getValue();

            // Create ImportRequest for each provider
            ImportRequest importRequest = new ImportRequest();
            importRequest.setId(createImportRequestDetailId());
            importRequest.setImportReason(importReason);
            importRequest.setStatus(RequestStatus.NOT_STARTED);
            importRequest.setType(importType);
            importRequest.setBatchCode(getTodayPrefix() + batchSuffix);

            // Set export request if provided
            if (exportRequestId != null) {
                importRequest.setExportRequest(exportRequestRepository.findById(exportRequestId)
                        .orElseThrow(() -> new RuntimeException("Export request not found with ID: " + exportRequestId)));
            }

            // Set provider
            Provider provider = providerRepository.findById(providerId)
                    .orElseThrow(() -> new RuntimeException("Provider not found with ID: " + providerId));
            importRequest.setProvider(provider);

            // Save the import request
            ImportRequest savedImportRequest = importRequestRepository.save(importRequest);
            createdImportRequestIds.add(savedImportRequest.getId());

            // Create import request details
            for (ImportRequestCreateWithDetailRequest req : requests) {
                ImportRequestDetail detail = new ImportRequestDetail();
                detail.setImportRequest(savedImportRequest);
                detail.setExpectQuantity(req.getQuantity());
                detail.setItem(itemRepository.findById(req.getItemId())
                        .orElseThrow(() -> new RuntimeException("Item not found with ID: " + req.getItemId())));
                detail.setActualQuantity(0);
                detail.setOrderedQuantity(0);
                importRequestDetailRepository.save(detail);
            }
        }
        
        return createdImportRequestIds;
    }

    public void deleteImportRequestDetail(Long importRequestDetailId) {
        LOGGER.info("Deleting import request detail");
        importRequestDetailRepository.deleteById(importRequestDetailId);
    }

    public List<ImportRequestDetailResponse>getImportRequestDetailsByImportRequestId(String importRequestId) {
        LOGGER.info("Getting import request detail for ImportRequest ID: {}", importRequestId);
        List<ImportRequestDetail> importRequestDetails = importRequestDetailRepository
                .findImportRequestDetailsByImportRequest_Id(importRequestId);

        return importRequestDetails.stream().map(this::mapToResponse).toList();
    }

    public ImportRequestDetailResponse getImportRequestDetailById(Long importRequestDetailId) {
        LOGGER.info("Getting import request detail for ImportRequestDetail ID: {}", importRequestDetailId);
        ImportRequestDetail importRequestDetail = importRequestDetailRepository.findById(importRequestDetailId).orElseThrow();
        return mapToResponse(importRequestDetail);
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

    private ImportRequestDetailResponse mapToResponse(ImportRequestDetail importRequestDetail) {
        return new ImportRequestDetailResponse(
                importRequestDetail.getId(),
                importRequestDetail.getImportRequest() != null ? importRequestDetail.getImportRequest().getId() : null,
                importRequestDetail.getItem() != null ? importRequestDetail.getItem().getId() : null,
                importRequestDetail.getItem() != null ? importRequestDetail.getItem().getName() : null,
                importRequestDetail.getActualQuantity(),
                importRequestDetail.getExpectQuantity(),
                importRequestDetail.getOrderedQuantity(),
                importRequestDetail.getStatus() != null ? importRequestDetail.getStatus() : null
        );
    }

    private String createImportRequestDetailId() {
        String prefix = "PN";
        LocalDate today = LocalDate.now();

        int todayCount = (int) importRequestRepository.findAll().stream()
                .filter(req -> req.getId().startsWith(prefix + "-" + today.format(DateTimeFormatter.BASIC_ISO_DATE)))
                .count();

        String datePart = today.format(DateTimeFormatter.BASIC_ISO_DATE);
        String sequence = String.format("%03d", todayCount + 1);

        return String.format("%s-%s-%s", prefix, datePart, sequence);
    }

    private String getTodayPrefix() {
        return LocalDate.now() + "_";
    }
}
