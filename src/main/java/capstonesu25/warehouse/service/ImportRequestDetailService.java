package capstonesu25.warehouse.service;

import capstonesu25.warehouse.entity.ImportRequest;
import capstonesu25.warehouse.entity.ImportRequestDetail;
import capstonesu25.warehouse.entity.Provider;
import capstonesu25.warehouse.enums.DetailStatus;
import capstonesu25.warehouse.model.importrequest.importrequestdetail.ImportRequestDetailRequest;
import capstonesu25.warehouse.model.importrequest.importrequestdetail.ImportRequestDetailResponse;
import capstonesu25.warehouse.repository.ImportRequestDetailRepository;
import capstonesu25.warehouse.repository.ImportRequestRepository;
import capstonesu25.warehouse.repository.ItemRepository;
import capstonesu25.warehouse.repository.ProviderRepository;
import capstonesu25.warehouse.utils.ExcelUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ImportRequestDetailService {
    private final ImportRequestRepository importRequestRepository;
    private final ImportRequestDetailRepository importRequestDetailRepository;
    private final ItemRepository itemRepository;
    private static final Logger LOGGER = LoggerFactory.getLogger(ImportRequestDetailService.class);
    private final ProviderRepository providerRepository;


    public void createImportRequestDetail(MultipartFile file, Long importRequestId) {
        LOGGER.info("Creating import order detail");
        LOGGER.info("Finding import order by id: {}", importRequestId);

        ImportRequest importRequest = importRequestRepository.findById(importRequestId)
                .orElseThrow(() -> new RuntimeException("Import order not found"));

        List<ImportRequestDetailRequest> requests = ExcelUtil.processExcelFile(file, ImportRequestDetailRequest.class);

        for (ImportRequestDetailRequest request : requests) {
            if (request.getItemId().size() != request.getQuantity().size()) {
                throw new IllegalArgumentException("Mismatch between itemIds and quantities");
            }
            LOGGER.info("update provider for import request with providerId: {}", request.getProviderId());
            Provider provider = providerRepository.findById(request.getProviderId())
                    .orElseThrow(() -> new RuntimeException("Provider not found with ID: " + request.getProviderId()));
            importRequest.setProvider(provider);
            importRequestRepository.save(importRequest);

            for (int i = 0; i < request.getItemId().size(); i++) {
                ImportRequestDetail importRequestDetail = new ImportRequestDetail();
                importRequestDetail.setImportRequest(importRequest);
                importRequestDetail.setExpectQuantity(request.getQuantity().get(i));
                importRequestDetail.setItem(itemRepository.findById(request.getItemId().get(i))
                        .orElseThrow(() -> new RuntimeException("Item not found with ID: " + request.getItemId())));
                importRequestDetail.setActualQuantity(0);

                importRequestDetailRepository.save(importRequestDetail);
            }
        }
    }


    public void deleteImportRequestDetail(Long importRequestDetailId) {
        LOGGER.info("Deleting import request detail");
        importRequestDetailRepository.deleteById(importRequestDetailId);
    }

    public List<ImportRequestDetailResponse> getImportRequestDetailsByImportRequestId(Long importRequestId, int page, int limit) {
        LOGGER.info("Getting import request detail for ImportRequest ID: {}", importRequestId);
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), limit);
        Page<ImportRequestDetail> importRequestDetails = importRequestDetailRepository
                .findImportRequestDetailsByImportRequest_Id(importRequestId, pageable);

        return importRequestDetails.getContent()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public ImportRequestDetailResponse getImportRequestDetailById(Long importRequestDetailId) {
        LOGGER.info("Getting import request detail for ImportRequestDetail ID: {}", importRequestDetailId);
        ImportRequestDetail importRequestDetail = importRequestDetailRepository.findById(importRequestDetailId).orElseThrow();
        return mapToResponse(importRequestDetail);
    }

    private ImportRequestDetailResponse mapToResponse(ImportRequestDetail importRequestDetail) {
        return new ImportRequestDetailResponse(
                importRequestDetail.getId(),
                importRequestDetail.getImportRequest() != null ? importRequestDetail.getImportRequest().getId() : null,
                importRequestDetail.getItem() != null ? importRequestDetail.getItem().getId() : null,
                importRequestDetail.getItem() != null ? importRequestDetail.getItem().getName() : null,
                importRequestDetail.getExpectQuantity(),
                importRequestDetail.getActualQuantity(),
                importRequestDetail.getStatus() != null ? importRequestDetail.getStatus() : null
        );
    }


}
