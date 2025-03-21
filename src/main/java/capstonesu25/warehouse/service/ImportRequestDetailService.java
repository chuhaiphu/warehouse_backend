package capstonesu25.warehouse.service;

import capstonesu25.warehouse.entity.ImportRequest;
import capstonesu25.warehouse.entity.ImportRequestDetail;
import capstonesu25.warehouse.enums.DetailStatus;
import capstonesu25.warehouse.model.importrequest.importrequestdetail.ImportRequestDetailRequest;
import capstonesu25.warehouse.model.importrequest.importrequestdetail.ImportRequestDetailResponse;
import capstonesu25.warehouse.repository.ImportRequestDetailRepository;
import capstonesu25.warehouse.repository.ImportRequestRepository;
import capstonesu25.warehouse.repository.ItemRepository;
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

    public void createImportRequestDetail(MultipartFile file, Long importRequestId) {
        LOGGER.info("Creating import request detail");
        LOGGER.info("finding import request by id: {}", importRequestId);
        ImportRequest importRequest = importRequestRepository.findById(importRequestId).orElseThrow();
        List<ImportRequestDetailRequest> list =  ExcelUtil.processExcelFile(file,
                ImportRequestDetailRequest.class);

        for(ImportRequestDetailRequest request : list) {
            ImportRequestDetail importRequestDetail = new ImportRequestDetail();
            importRequestDetail.setImportRequest(importRequest);
            importRequestDetail.setExpectQuantity(request.getQuantity());
            importRequestDetail.setItem(itemRepository.findById(request.getItemId()).orElseThrow());
            importRequestDetail.setActualQuantity(0);
            importRequestDetailRepository.save(importRequestDetail);
        }
    }

    public void updateImportRequestDetail(List<ImportRequestDetailRequest> list, Long importRequestId) {
        LOGGER.info("Updating import request detail");

        List<ImportRequestDetail> importRequestDetails = importRequestDetailRepository
                .findImportRequestDetailsByImportRequest_Id(importRequestId);

        Map<Long, ImportRequestDetail> detailMap = importRequestDetails.stream()
                .collect(Collectors.toMap(d -> d.getItem().getId(), d -> d));

        for (ImportRequestDetailRequest request : list) {
            ImportRequestDetail detail = detailMap.get(request.getItemId());
            if (detail != null) {
                detail.setExpectQuantity(request.getQuantity());
                detail.setActualQuantity(request.getActualQuantity());
                if(request.getQuantity() == request.getActualQuantity()) {
                    detail.setStatus(DetailStatus.MATCH);
                }
                if(request.getQuantity() > request.getActualQuantity()) {
                    detail.setStatus(DetailStatus.LACK);
                }
                if(request.getQuantity() < request.getActualQuantity()) {
                    detail.setStatus(DetailStatus.EXCESS);
                }
            }
        }
        importRequestDetailRepository.saveAll(importRequestDetails);
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
