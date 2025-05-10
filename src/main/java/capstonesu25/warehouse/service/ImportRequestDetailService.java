package capstonesu25.warehouse.service;

import capstonesu25.warehouse.entity.ImportRequest;
import capstonesu25.warehouse.entity.ImportRequestDetail;
import capstonesu25.warehouse.entity.Item;
import capstonesu25.warehouse.entity.Provider;
import capstonesu25.warehouse.model.importrequest.importrequestdetail.ImportRequestDetailRequest;
import capstonesu25.warehouse.model.importrequest.importrequestdetail.ImportRequestDetailResponse;
import capstonesu25.warehouse.repository.ImportRequestDetailRepository;
import capstonesu25.warehouse.repository.ImportRequestRepository;
import capstonesu25.warehouse.repository.ItemRepository;
import capstonesu25.warehouse.repository.ProviderRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ImportRequestDetailService {
    private final ImportRequestRepository importRequestRepository;
    private final ImportRequestDetailRepository importRequestDetailRepository;
    private final ItemRepository itemRepository;
    private static final Logger LOGGER = LoggerFactory.getLogger(ImportRequestDetailService.class);
    private final ProviderRepository providerRepository;
    private static final   String TODAY_PREFIX = LocalDate.now() + "_";

    public void createImportRequestDetail(List<ImportRequestDetailRequest> detailRequests, Long importRequestId) {
        LOGGER.info("Creating import order detail");
        // Validate that all items belong to the same provider
        checkSameProvider(detailRequests);
        // Get the original import request to copy its properties
        ImportRequest originalRequest = importRequestRepository.findById(importRequestId)
                .orElseThrow(() -> new RuntimeException("Import request not found"));

        OptionalInt latestBatchSuffix = findLatestBatchSuffixForToday();
        int batchSuffix = latestBatchSuffix.isPresent() ? latestBatchSuffix.getAsInt() + 1 : 1;

        // Group by providerId using the item's providers
        Map<Long, List<ImportRequestDetailRequest>> requestsByProvider = new HashMap<>();

        for (ImportRequestDetailRequest req : detailRequests) {
            Item item = itemRepository.findById(req.getItemId())
                    .orElseThrow(() -> new RuntimeException("Item not found with ID: " + req.getItemId()));

            for (Provider provider : item.getProviders()) {
                requestsByProvider
                        .computeIfAbsent(provider.getId(), k -> new ArrayList<>())
                        .add(req);
            }
        }

        // Process each provider group
        for (Map.Entry<Long, List<ImportRequestDetailRequest>> entry : requestsByProvider.entrySet()) {
            Long providerId = entry.getKey();
            List<ImportRequestDetailRequest> requests = entry.getValue();

            // Create new ImportRequest for each provider
            ImportRequest newImportRequest = new ImportRequest();
            newImportRequest.setImportReason(originalRequest.getImportReason());
            newImportRequest.setStatus(originalRequest.getStatus());
            newImportRequest.setType(originalRequest.getType());
            newImportRequest.setExportRequest(originalRequest.getExportRequest());
            newImportRequest.setBatchCode(TODAY_PREFIX + batchSuffix);

            // Set provider
            Provider provider = providerRepository.findById(providerId)
                    .orElseThrow(() -> new RuntimeException("Provider not found with ID: " + providerId));
            newImportRequest.setProvider(provider);

            // Save the new import request
            ImportRequest savedImportRequest = importRequestRepository.save(newImportRequest);

            // Create import request details
            for (ImportRequestDetailRequest req : requests) {
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

        // Delete the original empty import request
        importRequestRepository.deleteById(importRequestId);
    }



    public void deleteImportRequestDetail(Long importRequestDetailId) {
        LOGGER.info("Deleting import request detail");
        importRequestDetailRepository.deleteById(importRequestDetailId);
    }

    public Page<ImportRequestDetailResponse> getImportRequestDetailsByImportRequestId(Long importRequestId, int page, int limit) {
        LOGGER.info("Getting import request detail for ImportRequest ID: {}", importRequestId);
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), limit);
        Page<ImportRequestDetail> importRequestDetails = importRequestDetailRepository
                .findImportRequestDetailsByImportRequest_Id(importRequestId, pageable);

        return importRequestDetails.map(this::mapToResponse);
    }


    public ImportRequestDetailResponse getImportRequestDetailById(Long importRequestDetailId) {
        LOGGER.info("Getting import request detail for ImportRequestDetail ID: {}", importRequestDetailId);
        ImportRequestDetail importRequestDetail = importRequestDetailRepository.findById(importRequestDetailId).orElseThrow();
        return mapToResponse(importRequestDetail);
    }

    private OptionalInt findLatestBatchSuffixForToday() {
        LOGGER.info("Finding latest batch suffix for today");
        List<ImportRequest> requests = importRequestRepository.findByBatchCodeStartingWith(TODAY_PREFIX);

        return requests.stream()
                .map(ImportRequest::getBatchCode)
                .map(code -> code.substring(TODAY_PREFIX.length()))
                .mapToInt(Integer::parseInt)
                .max(); // returns OptionalInt
    }

    private void checkSameProvider(List<ImportRequestDetailRequest> request) {
        for(ImportRequestDetailRequest itemOrder : request) {
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


}
