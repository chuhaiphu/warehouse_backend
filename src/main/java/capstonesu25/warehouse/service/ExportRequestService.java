package capstonesu25.warehouse.service;

import capstonesu25.warehouse.entity.*;
import capstonesu25.warehouse.enums.AccountRole;
import capstonesu25.warehouse.enums.AccountStatus;
import capstonesu25.warehouse.enums.ExportType;
import capstonesu25.warehouse.enums.ImportStatus;
import capstonesu25.warehouse.model.exportrequest.exportborrowing.ExportBorrowingRequest;
import capstonesu25.warehouse.model.exportrequest.exportliquidation.ExportLiquidationRequest;
import capstonesu25.warehouse.model.exportrequest.exportpartial.ExportPartialRequest;
import capstonesu25.warehouse.model.exportrequest.exportproduction.ExportRequestRequest;
import capstonesu25.warehouse.model.exportrequest.ExportRequestResponse;
import capstonesu25.warehouse.model.exportrequest.exportreturn.ExportReturnRequest;
import capstonesu25.warehouse.model.importrequest.AssignStaffExportRequest;
import capstonesu25.warehouse.repository.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ExportRequestService {
    private final ExportRequestRepository exportRequestRepository;
    private final AccountRepository accountRepository;
    private final ImportRequestRepository importRequestRepository;
    private final StaffPerformanceRepository staffPerformanceRepository;
    private final ConfigurationRepository configurationRepository;
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

    public Page<ExportRequestResponse> getAllExportRequestByAssignStaff( Long staffId, int page, int limit) {
        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by(Sort.Direction.DESC, "createdDate"));
        Page<ExportRequest> exportRequests = exportRequestRepository.findAllByAssignedStaff_Id(staffId, pageable);
        return exportRequests.map(this::mapToResponse);
    }

    public ExportRequestResponse getExportRequestById(Long id) {
        ExportRequest exportRequest = exportRequestRepository.findById(id).orElseThrow();
        return mapToResponse(exportRequest);
    }

    public ExportRequestResponse createExportPartialRequest (ExportPartialRequest request) {
        LOGGER.info("Creating export partial request");
        if(!checkType(ExportType.PARTIAL, request.getType())) {
            LOGGER.error("Invalid export type: " + request.getType());
            throw new IllegalArgumentException("Invalid export type: " + request.getType());
        }
        if(request.getDepartmentId() == null && request.getReceiverName() == null
                && request.getReceiverPhone() == null && request.getReceiverAddress() == null) {
            LOGGER.error("Department ID, receiver name, phone, and address cannot be null");
            throw new IllegalArgumentException("Department ID, receiver name, phone, and address cannot be null");
        }

        ExportRequest exportRequest = new ExportRequest();
        if(request.getDepartmentId() != null) {
            exportRequest.setDepartmentId(request.getDepartmentId());
        }
        exportRequest.setReceiverName(request.getReceiverName());
        exportRequest.setReceiverPhone(request.getReceiverPhone());
        exportRequest.setReceiverAddress(request.getReceiverAddress());
        exportRequest.setExportReason(request.getExportReason());
        exportRequest.setType(request.getType());

        validateForTimeDate(request.getExportDate(), request.getExportTime());
        exportRequest.setExportDate(request.getExportDate());
        exportRequest.setExportTime(request.getExportTime());
        exportRequest.setStatus(ImportStatus.NOT_STARTED);

        return mapToResponse(exportRequestRepository.save(exportRequest));
    }

    public ExportRequestResponse createExportLiquidationRequest(ExportLiquidationRequest request) {
        LOGGER.info("Creating export liquidation request");
        if(!checkType(ExportType.LIQUIDATION, request.getType())) {
            LOGGER.error("Invalid export type: " + request.getType());
            throw new IllegalArgumentException("Invalid export type: " + request.getType());
        }

        ExportRequest exportRequest = new ExportRequest();
        exportRequest.setReceiverName(request.getReceiverName());
        exportRequest.setReceiverPhone(request.getReceiverPhone());
        exportRequest.setReceiverAddress(request.getReceiverAddress());
        exportRequest.setExportReason(request.getExportReason());
        exportRequest.setType(request.getType());

        validateForTimeDate(request.getExportDate(), request.getExportTime());
        exportRequest.setExportDate(request.getExportDate());
        exportRequest.setExportTime(request.getExportTime());
        exportRequest.setStatus(ImportStatus.NOT_STARTED);

        return mapToResponse(exportRequestRepository.save(exportRequest));
    }

    public ExportRequestResponse createExportBorrowingRequest(ExportBorrowingRequest request) {
        LOGGER.info("Creating export borrowing request");
        if(!checkType(ExportType.BORROWING, request.getType())) {
            LOGGER.error("Invalid export type: " + request.getType());
            throw new IllegalArgumentException("Invalid export type: " + request.getType());
        }

        if(request.getDepartmentId() == null && request.getReceiverName() == null
                && request.getReceiverPhone() == null && request.getReceiverAddress() == null) {
            LOGGER.error("Department ID, receiver name, phone, and address cannot be null");
            throw new IllegalArgumentException("Department ID, receiver name, phone, and address cannot be null");
        }

        ExportRequest exportRequest = new ExportRequest();
        if(request.getDepartmentId() != null) {
            exportRequest.setDepartmentId(request.getDepartmentId());
        }
        exportRequest.setReceiverName(request.getReceiverName());
        exportRequest.setReceiverPhone(request.getReceiverPhone());
        exportRequest.setReceiverAddress(request.getReceiverAddress());
        exportRequest.setExportReason(request.getExportReason());
        exportRequest.setType(request.getType());

        validateForTimeDate(request.getExportDate(), request.getExportTime());
        exportRequest.setExportDate(request.getExportDate());
        exportRequest.setExportTime(request.getExportTime());
        exportRequest.setExpectedReturnDate(request.getExpectedReturnDate());
        exportRequest.setStatus(ImportStatus.NOT_STARTED);

        return mapToResponse(exportRequestRepository.save(exportRequest));
    }

    public ExportRequestResponse createExportReturnRequest(ExportReturnRequest request) {
        LOGGER.info("Creating export production request");
        if(!checkType(ExportType.RETURN, request.getType())) {
            LOGGER.error("Invalid export type: " + request.getType());
            throw new IllegalArgumentException("Invalid export type: " + request.getType());
        }

        List<ImportRequest> list = request.getImportRequestIds().stream()
            .map(importRequestId -> importRequestRepository.findById(importRequestId).orElseThrow())
            .toList();

        LOGGER.info("Check if any import request in request is invalid");
        for(ImportRequest importRequest : list ) {
            if(!Objects.equals(importRequest.getProvider().getId(), request.getProviderId())) {
                LOGGER.error("Invalid import request: " + importRequest.getId());
                throw new IllegalArgumentException("Invalid import request: " + importRequest.getId());
            }
        }

        ExportRequest exportRequest = new ExportRequest();
        exportRequest.setExportReason(request.getExportReason());
        exportRequest.setProviderId(request.getProviderId());
        exportRequest.setType(request.getType());

        validateForTimeDate(request.getExportDate(), request.getExportTime());
        exportRequest.setExportDate(request.getExportDate());
        exportRequest.setExportTime(request.getExportTime());
        exportRequest.setImportRequests(list);
        exportRequest.setStatus(ImportStatus.NOT_STARTED);

        return mapToResponse(exportRequestRepository.save(exportRequest));
    }

    public ExportRequestResponse createExportProductionRequest(ExportRequestRequest request) {
        LOGGER.info("Creating export production request");
        if(!checkType(ExportType.PRODUCTION, request.getType())) {
            LOGGER.error("Invalid export type: " + request.getType());
            throw new IllegalArgumentException("Invalid export type: " + request.getType());
        }
        if(request.getDepartmentId() == null && request.getReceiverName() == null
        && request.getReceiverPhone() == null && request.getReceiverAddress() == null) {
            LOGGER.error("Department ID, receiver name, phone, and address cannot be null");
            throw new IllegalArgumentException("Department ID, receiver name, phone, and address cannot be null");
        }

        ExportRequest exportRequest = new ExportRequest();
        exportRequest.setExportReason(request.getExportReason());
        if(request.getDepartmentId() != null) {
            exportRequest.setDepartmentId(request.getDepartmentId());
        }
        exportRequest.setReceiverName(request.getReceiverName());
        exportRequest.setReceiverPhone(request.getReceiverPhone());
        exportRequest.setReceiverAddress(request.getReceiverAddress());
        exportRequest.setType(request.getType());

        validateForTimeDate(request.getExportDate(), request.getExportTime());
        exportRequest.setExportDate(request.getExportDate());
        exportRequest.setExportTime(request.getExportTime());
        exportRequest.setStatus(ImportStatus.NOT_STARTED);
        
        if (request.getAssignedWareHouseKeeperId() != null) {
            Account account = accountRepository.findById(request.getAssignedWareHouseKeeperId())
                .orElseThrow(() -> new IllegalArgumentException("Account not found with ID: " + request.getAssignedWareHouseKeeperId()));
            validateAccountForAssignment(account);
            updateAccountStatusForExportRequest(account, exportRequest);
            exportRequest.setAssignedStaff(account);

        }

        ExportRequest newExportRequest = exportRequestRepository.save(exportRequest);
        return mapToResponse(newExportRequest);
    }

    public ExportRequestResponse assignStaffToExportRequest(AssignStaffExportRequest request) {
        LOGGER.info("Assigning staff to export request with ID: " + request.getExportRequestId());
        ExportRequest exportRequest = exportRequestRepository.findById(request.getExportRequestId()).orElseThrow();

        if(exportRequest.getAssignedStaff() != null) {
            LOGGER.info("Return working for pre staff: {}",exportRequest.getAssignedStaff().getEmail());
            StaffPerformance staffPerformance = staffPerformanceRepository.
                    findByExportRequestIdAndAssignedStaff_Id(exportRequest.getId(),exportRequest.getAssignedStaff().getId());
            if(staffPerformance != null) {
                LOGGER.info("Delete working time for pre staff: {}",exportRequest.getAssignedStaff().getEmail());
                staffPerformanceRepository.delete(staffPerformance);
            }
        }

        if (request.getAccountId() != null) {
            LOGGER.info("Assigning staff with account ID: " + request.getAccountId() + " to export request");
            Account staff = accountRepository.findById(request.getAccountId()).orElseThrow(
                    () -> new IllegalArgumentException("Staff not found with ID: " + request.getAccountId())
            );
            validateAccountForAssignment(staff);
            setTimeForStaffPerformance(staff, exportRequest);
            exportRequest.setAssignedStaff(staff);
        }
        exportRequestRepository.save(exportRequest);
        return mapToResponse(exportRequest);
    }

    private void setTimeForStaffPerformance(Account account, ExportRequest exportRequest) {
        int totalMinutes = 0;
        for (ExportRequestDetail detail : exportRequest.getExportRequestDetails()) {
            LOGGER.info("Calculating expected working time for item: " + detail.getItem().getName());
            totalMinutes += detail.getQuantity() * detail.getItem().getCountingMinutes();
        }
        LocalTime expectedWorkingTime = LocalTime.of(0, 0).plusMinutes(totalMinutes);
        StaffPerformance staffPerformance = new StaffPerformance();
        staffPerformance.setExpectedWorkingTime(expectedWorkingTime);
        staffPerformance.setDate(exportRequest.getExportDate());
        staffPerformance.setImportOrderId(exportRequest.getId());
        staffPerformance.setAssignedStaff(account);
        staffPerformanceRepository.save(staffPerformance);
    }

    private boolean checkType(ExportType expect, ExportType actual) {
        return expect == actual;
    }

    private ExportRequestResponse mapToResponse(ExportRequest exportRequest) {
        return new ExportRequestResponse(
            exportRequest.getId(),
            exportRequest.getExportReason(),
            exportRequest.getReceiverName(),
            exportRequest.getReceiverPhone(),
            exportRequest.getReceiverAddress(),
            exportRequest.getDepartmentId(),
            exportRequest.getProviderId(),
            exportRequest.getStatus(),
            exportRequest.getType(),
            exportRequest.getExportDate(),
            exportRequest.getExportTime(),
            exportRequest.getExpectedReturnDate(),
            exportRequest.getAssignedStaff() != null ? exportRequest.getAssignedStaff().getId() : null,
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

    private void validateAccountForAssignment(Account account) {
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new IllegalStateException("Cannot assign staff: Account is not active");
        }

        if (account.getRole() != AccountRole.STAFF) {
            throw new IllegalStateException("Cannot assign staff: Account is not a staff member");
        }
    }

    private void updateAccountStatusForExportRequest(Account account, ExportRequest exportRequest) {
        LOGGER.info("Update account status to INACTIVE");
        if(exportRequest.getAssignedStaff() != null) {
            // If the import order is being reassigned, set the previous staff's status to ACTIVE
            LOGGER.info("Update previous staff status to ACTIVE");
            Account preStaff = exportRequest.getAssignedStaff();
            preStaff.setStatus(AccountStatus.ACTIVE);
            accountRepository.save(preStaff);
        }
        account.setStatus(AccountStatus.INACTIVE);
        accountRepository.save(account);
    }
    private void validateForTimeDate(LocalDate date, LocalTime time) {
        LOGGER.info("Validating time and date for import order");
        Configuration configuration = configurationRepository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Configuration not found with name: importOrder"));

        long minutesToAdd = configuration.getCreateRequestTimeAtLeast().getHour() * 60
                + configuration.getCreateRequestTimeAtLeast().getMinute();

        LOGGER.info("Check if date is in the past");
        if(date.isBefore(LocalDate.now())) {
            throw new IllegalStateException("Cannot set time for import order: Date is in the past");
        }

        LOGGER.info("Check if time set is too early");
        if (date.isEqual(LocalDate.now()) &&
                LocalTime.now()
                        .plusMinutes(minutesToAdd)
                        .isAfter(time)) {
            throw new IllegalStateException("Cannot set time for import order: Time is too early");
        }

    }
} 