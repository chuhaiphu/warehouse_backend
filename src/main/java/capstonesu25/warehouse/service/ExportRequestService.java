package capstonesu25.warehouse.service;

import capstonesu25.warehouse.entity.ExportRequest;
import capstonesu25.warehouse.entity.ImportRequest;
import capstonesu25.warehouse.entity.ExportRequestDetail;
import capstonesu25.warehouse.model.exportrequest.ExportRequestRequest;
import capstonesu25.warehouse.model.exportrequest.ExportRequestResponse;
import capstonesu25.warehouse.repository.AccountRepository;
import capstonesu25.warehouse.repository.ExportRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExportRequestService {
    private final ExportRequestRepository exportRequestRepository;
    private final AccountRepository accountRepository;

    public List<ExportRequestResponse> getAllExportRequests() {
        return exportRequestRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    public Page<ExportRequestResponse> getAllExportRequestsByPage(int page, int limit) {
        Pageable pageable = PageRequest.of(page - 1, limit);
        Page<ExportRequest> exportRequests = exportRequestRepository.findAll(pageable);
        return exportRequests.map(this::mapToResponse);
    }

    public ExportRequestResponse getExportRequestById(Long id) {
        ExportRequest exportRequest = exportRequestRepository.findById(id).orElseThrow();
        return mapToResponse(exportRequest);
    }

    public ExportRequestResponse createExportRequest(ExportRequestRequest request) {
        ExportRequest exportRequest = new ExportRequest();
        exportRequest.setExportReason(request.getExportReason());
        exportRequest.setReceiverName(request.getReceiverName());
        exportRequest.setReceiverPhone(request.getReceiverPhone());
        exportRequest.setReceiverAddress(request.getReceiverAddress());
        exportRequest.setType(request.getType());
        exportRequest.setExportDate(request.getExportDate());
        exportRequest.setExportTime(request.getExportTime());
        exportRequest.setStatus("NOT_STARTED");
        
        if (request.getAssignedWareHouseKeeperId() != null) {
            exportRequest.setAssignedWareHouseKeeper(
                accountRepository.findById(request.getAssignedWareHouseKeeperId()).orElseThrow()
            );
        }

        ExportRequest newExportRequest = exportRequestRepository.save(exportRequest);
        return mapToResponse(newExportRequest);
    }

    private ExportRequestResponse mapToResponse(ExportRequest exportRequest) {
        return new ExportRequestResponse(
            exportRequest.getId(),
            exportRequest.getExportReason(),
            exportRequest.getReceiverName(),
            exportRequest.getReceiverPhone(),
            exportRequest.getReceiverAddress(),
            exportRequest.getStatus(),
            exportRequest.getType(),
            exportRequest.getExportDate(),
            exportRequest.getExportTime(),
            exportRequest.getAssignedWareHouseKeeper() != null ? exportRequest.getAssignedWareHouseKeeper().getId() : null,
            exportRequest.getPaper() != null ? exportRequest.getPaper().getId() : null,
            exportRequest.getImportRequests() != null ?
                exportRequest.getImportRequests().stream().map(ImportRequest::getId).toList() :
                List.of(),
            exportRequest.getExportRequestDetails() != null ?
                exportRequest.getExportRequestDetails().stream().map(ExportRequestDetail::getId).toList() :
                List.of(),
            exportRequest.getCreatedBy(),
            exportRequest.getUpdatedBy(),
            exportRequest.getCreatedDate(),
            exportRequest.getUpdatedDate()
        );
    }
} 