package capstonesu25.warehouse.service;

import capstonesu25.warehouse.entity.ImportOrder;
import capstonesu25.warehouse.entity.ImportRequest;
import capstonesu25.warehouse.entity.ImportRequestDetail;
import capstonesu25.warehouse.enums.ImportStatus;
import capstonesu25.warehouse.model.importrequest.ImportRequestRequest;
import capstonesu25.warehouse.model.importrequest.ImportRequestResponse;
import capstonesu25.warehouse.repository.ExportRequestRepository;
import capstonesu25.warehouse.repository.ImportRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ImportRequestService {
    private final ImportRequestRepository importRequestRepository;
    private final ExportRequestRepository exportRequestRepository;

    public List<ImportRequestResponse> getAllImportRequests() {
        return importRequestRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<ImportRequestResponse> getAllImportRequestsByPage(int page, int limit){
        Pageable pageable = PageRequest.of(page - 1, limit);
        Page<ImportRequest> importRequests = importRequestRepository.findAll(pageable);
        return importRequests.stream()
                .map(this::mapToResponse)
                .toList();
    }
    public ImportRequestResponse getImportRequestById(Long id) {
        ImportRequest importRequest = importRequestRepository.findById(id).orElseThrow();
        return mapToResponse(importRequest);
    }

    public ImportRequestResponse createImportRequest(ImportRequestRequest request) {
        ImportRequest importRequest = new ImportRequest();
        if(request.getExportRequestId() != null) {
            importRequest.setExportRequest(exportRequestRepository.findById(request.getExportRequestId()).orElseThrow());
        }
        importRequest.setImportReason(request.getImportReason());
        importRequest.setType(request.getImportType());
        importRequest.setStatus(ImportStatus.NOT_STARTED);
        ImportRequest newImportRequest = importRequestRepository.save(importRequest);
        return mapToResponse(newImportRequest);
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
