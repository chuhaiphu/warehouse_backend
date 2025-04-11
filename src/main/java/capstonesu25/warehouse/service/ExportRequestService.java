package capstonesu25.warehouse.service;

import capstonesu25.warehouse.entity.Account;
import capstonesu25.warehouse.entity.ExportRequest;
import capstonesu25.warehouse.entity.ImportRequest;
import capstonesu25.warehouse.entity.ExportRequestDetail;
import capstonesu25.warehouse.enums.AccountStatus;
import capstonesu25.warehouse.model.exportrequest.ExportRequestRequest;
import capstonesu25.warehouse.model.exportrequest.ExportRequestResponse;
import capstonesu25.warehouse.model.importrequest.AssignStaffExportRequest;
import capstonesu25.warehouse.repository.AccountRepository;
import capstonesu25.warehouse.repository.ExportRequestRepository;
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
public class ExportRequestService {
    private final ExportRequestRepository exportRequestRepository;
    private final AccountRepository accountRepository;
    private static final Logger LOGGER = LoggerFactory.getLogger(ExportRequestService.class);

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

    public ExportRequestResponse assignStaffToExportRequest(AssignStaffExportRequest request) {
        LOGGER.info("Assigning staff to export request with ID: " + request.getExportRequestId());
        ExportRequest exportRequest = exportRequestRepository.findById(request.getExportRequestId()).orElseThrow();
        if (request.getAccountId() != null) {
            LOGGER.info("Assigning staff with account ID: " + request.getAccountId() + " to export request");
            exportRequest.setAssignedWareHouseKeeper(
                accountRepository.findById(request.getAccountId()).orElseThrow()
            );
        }
        exportRequestRepository.save(exportRequest);
        return mapToResponse(exportRequest);
    }

    public ExportRequestResponse assignStaff(Long exportRequestId, Long accountId) {
        LOGGER.info("Assigning staff to export request with ID: " + exportRequestId);
        ExportRequest exportRequest = exportRequestRepository.findById(exportRequestId)
                .orElseThrow(() -> new NoSuchElementException("Export Request not found with ID: " + exportRequestId));
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new NoSuchElementException("Account not found with ID: " + accountId));
        if(account.getStatus() !=  AccountStatus.ACTIVE) {
            throw new IllegalArgumentException("Account is "+ account.getStatus());
        }

        exportRequest.setAssignedWareHouseKeeper(account);
        LOGGER.info("Update account status to INACTIVE");
        account.setStatus(AccountStatus.INACTIVE);
        accountRepository.save(account);
        return mapToResponse(exportRequestRepository.save(exportRequest));
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