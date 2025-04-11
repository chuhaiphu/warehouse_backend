package capstonesu25.warehouse.service;

import capstonesu25.warehouse.entity.ImportOrder;
import capstonesu25.warehouse.entity.ImportRequest;
import capstonesu25.warehouse.entity.ImportRequestDetail;
import capstonesu25.warehouse.enums.ImportStatus;
import capstonesu25.warehouse.model.importrequest.ImportRequestCreateRequest;
import capstonesu25.warehouse.model.importrequest.ImportRequestResponse;
import capstonesu25.warehouse.model.importrequest.ImportRequestUpdateRequest;
import capstonesu25.warehouse.repository.ExportRequestRepository;
import capstonesu25.warehouse.repository.ImportRequestRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class ImportRequestService {
    private final ImportRequestRepository importRequestRepository;
    private final ExportRequestRepository exportRequestRepository;
    private static final Logger LOGGER = LoggerFactory.getLogger(ImportRequestService.class);

    public List<ImportRequestResponse> getAllImportRequests() {
        LOGGER.info("Get all import requests");
        return importRequestRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    public Page<ImportRequestResponse> getAllImportRequestsByPage(int page, int limit) {
        LOGGER.info("Get all import requests by page");
        Pageable pageable = PageRequest.of(page - 1, limit);
        Page<ImportRequest> importRequests = importRequestRepository.findAll(pageable);
        return importRequests.map(this::mapToResponse);
    }

    public ImportRequestResponse getImportRequestById(Long id) {
        LOGGER.info("Get import request by id: " + id);
        ImportRequest importRequest = importRequestRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("ImportRequest not found with ID: " + id));
        return mapToResponse(importRequest);
    }

    public ImportRequestResponse createImportRequest(ImportRequestCreateRequest request) {
        LOGGER.info("Create new import request");
        
        ImportRequest importRequest = new ImportRequest();
        importRequest.setImportReason(request.getImportReason());
        importRequest.setType(request.getImportType());
        importRequest.setStatus(ImportStatus.NOT_STARTED);
        
        if (request.getExportRequestId() != null) {
            importRequest.setExportRequest(exportRequestRepository.findById(request.getExportRequestId())
                    .orElseThrow(() -> new NoSuchElementException("ExportRequest not found with ID: " + request.getExportRequestId())));
        }
        
        return mapToResponse(importRequestRepository.save(importRequest));
    }

    public ImportRequestResponse updateImportRequest(ImportRequestUpdateRequest request) {
        LOGGER.info("Update import request");
        
        ImportRequest importRequest = importRequestRepository.findById(request.getImportRequestId())
                .orElseThrow(() -> new NoSuchElementException("ImportRequest not found with ID: " + request.getImportRequestId()));
        
        importRequest.setImportReason(request.getImportReason());
        
        return mapToResponse(importRequestRepository.save(importRequest));
    }

    private ImportRequestResponse mapToResponse(ImportRequest importRequest) {
        return new ImportRequestResponse(
                importRequest.getId(),
                importRequest.getImportReason(),
                importRequest.getType(),
                importRequest.getStatus(),
                importRequest.getProvider() != null ? importRequest.getProvider().getId() : null,
                importRequest.getExportRequest() != null ? importRequest.getExportRequest().getId() : null,
                importRequest.getDetails() != null ?
                        importRequest.getDetails().stream().map(ImportRequestDetail::getId).toList() :
                        List.of(),
                importRequest.getImportOrders() != null ?
                        importRequest.getImportOrders().stream().map(ImportOrder::getId).toList() :
                        List.of(),
                importRequest.getCreatedBy(),
                importRequest.getUpdatedBy(),
                importRequest.getCreatedDate(),
                importRequest.getUpdatedDate()
        );
    }
}
