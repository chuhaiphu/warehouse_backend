package capstonesu25.warehouse.service;

import capstonesu25.warehouse.entity.*;
import capstonesu25.warehouse.enums.*;
import capstonesu25.warehouse.model.account.AccountResponse;
import capstonesu25.warehouse.model.account.ActiveAccountRequest;
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
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class ExportRequestService {
    private final ExportRequestRepository exportRequestRepository;
    private final AccountRepository accountRepository;
    private final ImportRequestRepository importRequestRepository;
    private final StaffPerformanceRepository staffPerformanceRepository;
    private final ConfigurationRepository configurationRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final StoredLocationRepository storedLocationRepository;
    private final ItemRepository itemRepository;
    private final AccountService accountService;
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

    public Page<ExportRequestResponse> getAllExportRequestByAssignStaff(Long staffId, int page, int limit) {
        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by(Sort.Direction.DESC, "createdDate"));

        // Fetch both sets
        Page<ExportRequest> exportRequestsConfirmStaff = exportRequestRepository.findAllByAssignedStaff_Id(staffId, Pageable.unpaged());
        Page<ExportRequest> exportRequestsCountingStaff = exportRequestRepository.findAllByCountingStaffId(staffId, Pageable.unpaged());

        // Merge, remove duplicates by ID
        Map<String, ExportRequest> uniqueRequests = new HashMap<>();
        Stream.concat(exportRequestsConfirmStaff.getContent().stream(), exportRequestsCountingStaff.getContent().stream())
                .forEach(req -> uniqueRequests.putIfAbsent(req.getId(), req));

        // Sort by createdDate DESC
        List<ExportRequest> sortedMergedList = uniqueRequests.values().stream()
                .sorted(Comparator.comparing(ExportRequest::getCreatedDate).reversed())
                .collect(Collectors.toList());

        // Manual pagination
        int start = Math.min((page - 1) * limit, sortedMergedList.size());
        int end = Math.min(start + limit, sortedMergedList.size());
        List<ExportRequestResponse> pagedResponses = sortedMergedList.subList(start, end).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(pagedResponses, pageable, sortedMergedList.size());
    }



    public ExportRequestResponse getExportRequestById(String id) {
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
        exportRequest.setId(createExportRequestId());
        if(request.getDepartmentId() != null) {
            exportRequest.setDepartmentId(request.getDepartmentId());
        }
        exportRequest.setReceiverName(request.getReceiverName());
        exportRequest.setReceiverPhone(request.getReceiverPhone());
        exportRequest.setReceiverAddress(request.getReceiverAddress());
        exportRequest.setExportReason(request.getExportReason());
        exportRequest.setType(request.getType());

        LOGGER.info("Check counting date and counting time is valid?");
        validateForTimeDate(request.getCountingDate(), request.getCountingTime());
        exportRequest.setCountingDate(request.getCountingDate());
        exportRequest.setCountingTime(request.getCountingTime());
        LOGGER.info("Check export date and export time is valid?");
        validateForTimeDate(request.getExportDate(), request.getExportTime());
        exportRequest.setExportDate(request.getExportDate());
        exportRequest.setExportTime(request.getExportTime());
        exportRequest.setStatus(ImportStatus.NOT_STARTED);

        ExportRequest export = exportRequestRepository.save(exportRequest);
        export = autoAssignCountingStaff(exportRequest);
        return mapToResponse(export);
    }

    public ExportRequestResponse createExportLiquidationRequest(ExportLiquidationRequest request) {
        LOGGER.info("Creating export liquidation request");
        if(!checkType(ExportType.LIQUIDATION, request.getType())) {
            LOGGER.error("Invalid export type: " + request.getType());
            throw new IllegalArgumentException("Invalid export type: " + request.getType());
        }

        ExportRequest exportRequest = new ExportRequest();
        exportRequest.setId(createExportRequestId());
        exportRequest.setReceiverName(request.getReceiverName());
        exportRequest.setReceiverPhone(request.getReceiverPhone());
        exportRequest.setReceiverAddress(request.getReceiverAddress());
        exportRequest.setExportReason(request.getExportReason());
        exportRequest.setType(request.getType());

        LOGGER.info("Check counting date and counting time is valid?");
        validateForTimeDate(request.getCountingDate(), request.getCountingTime());
        exportRequest.setCountingDate(request.getCountingDate());
        exportRequest.setCountingTime(request.getCountingTime());
        LOGGER.info("Check export date and export time is valid?");
        validateForTimeDate(request.getExportDate(), request.getExportTime());
        exportRequest.setExportDate(request.getExportDate());
        exportRequest.setExportTime(request.getExportTime());
        exportRequest.setStatus(ImportStatus.NOT_STARTED);

        ExportRequest export = exportRequestRepository.save(exportRequest);
        export = autoAssignCountingStaff(exportRequest);
        return mapToResponse(export);
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
        exportRequest.setId(createExportRequestId());
        if(request.getDepartmentId() != null) {
            exportRequest.setDepartmentId(request.getDepartmentId());
        }
        exportRequest.setReceiverName(request.getReceiverName());
        exportRequest.setReceiverPhone(request.getReceiverPhone());
        exportRequest.setReceiverAddress(request.getReceiverAddress());
        exportRequest.setExportReason(request.getExportReason());
        exportRequest.setType(request.getType());

        LOGGER.info("Check counting date and counting time is valid?");
        validateForTimeDate(request.getCountingDate(), request.getCountingTime());
        exportRequest.setCountingDate(request.getCountingDate());
        exportRequest.setCountingTime(request.getCountingTime());
        LOGGER.info("Check export date and export time is valid?");
        validateForTimeDate(request.getExportDate(), request.getExportTime());
        exportRequest.setExportDate(request.getExportDate());
        exportRequest.setExportTime(request.getExportTime());
        exportRequest.setExpectedReturnDate(request.getExpectedReturnDate());
        exportRequest.setStatus(ImportStatus.NOT_STARTED);

        ExportRequest export = exportRequestRepository.save(exportRequest);
        export = autoAssignCountingStaff(exportRequest);
        return mapToResponse(export);
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
        exportRequest.setId(createExportRequestId());
        exportRequest.setExportReason(request.getExportReason());
        exportRequest.setProviderId(request.getProviderId());
        exportRequest.setType(request.getType());

        LOGGER.info("Check counting date and counting time is valid?");
        validateForTimeDate(request.getCountingDate(), request.getCountingTime());
        exportRequest.setCountingDate(request.getCountingDate());
        exportRequest.setCountingTime(request.getCountingTime());
        LOGGER.info("Check export date and export time is valid?");
        validateForTimeDate(request.getExportDate(), request.getExportTime());
        exportRequest.setExportDate(request.getExportDate());
        exportRequest.setExportTime(request.getExportTime());
        exportRequest.setImportRequests(list);
        exportRequest.setStatus(ImportStatus.IN_PROGRESS);

        ExportRequest export = exportRequestRepository.save(exportRequest);
        export = autoAssignCountingStaff(exportRequest);
        return mapToResponse(export);
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
        exportRequest.setId(createExportRequestId());
        exportRequest.setExportReason(request.getExportReason());
        if(request.getDepartmentId() != null) {
            exportRequest.setDepartmentId(request.getDepartmentId());
        }
        exportRequest.setReceiverName(request.getReceiverName());
        exportRequest.setReceiverPhone(request.getReceiverPhone());
        exportRequest.setReceiverAddress(request.getReceiverAddress());
        exportRequest.setType(request.getType());

        LOGGER.info("Check counting date and counting time is valid?");
        validateForTimeDate(request.getCountingDate(), request.getCountingTime());
        exportRequest.setCountingDate(request.getCountingDate());
        exportRequest.setCountingTime(request.getCountingTime());

        LOGGER.info("Check export date and export time is valid?");
        validateForTimeDate(request.getExportDate(), request.getExportTime());
        exportRequest.setExportDate(request.getExportDate());
        exportRequest.setExportTime(request.getExportTime());
        exportRequest.setStatus(ImportStatus.IN_PROGRESS);

        ExportRequest export = exportRequestRepository.save(exportRequest);
        return mapToResponse(export);
    }

    public ExportRequestResponse assignStaffToExportRequest(AssignStaffExportRequest request) {
        LOGGER.info("Assigning staff for confirm to export request with ID: " + request.getExportRequestId());
        ExportRequest exportRequest = exportRequestRepository.findById(request.getExportRequestId()).orElseThrow();

        if(exportRequest.getAssignedStaff() != null) {
            LOGGER.info("Return working for pre staff: {}",exportRequest.getAssignedStaff().getEmail());
            StaffPerformance staffPerformance = staffPerformanceRepository.
                    findByExportRequestIdAndAssignedStaff_IdAndExportCounting(exportRequest.getId(),exportRequest.getAssignedStaff().getId(), false);
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
            Configuration configuration = configurationRepository.findAll().getFirst();
            StaffPerformance staffPerformance = new StaffPerformance();
            staffPerformance.setExpectedWorkingTime(configuration.getTimeToAllowConfirm());
            staffPerformance.setDate(exportRequest.getExportDate());
            staffPerformance.setExportRequestId(exportRequest.getId());
            staffPerformance.setAssignedStaff(staff);
            staffPerformanceRepository.save(staffPerformance);
            exportRequest.setAssignedStaff(staff);
            exportRequestRepository.save(exportRequest);
        }

        exportRequest.setStatus(ImportStatus.IN_PROGRESS);
        exportRequestRepository.save(exportRequest);
        return mapToResponse(exportRequest);
    }

    public ExportRequestResponse assignCountingStaff(AssignStaffExportRequest request) {
        LOGGER.info("Assigning staff for counting to export request with ID: " + request.getExportRequestId());
        ExportRequest exportRequest = exportRequestRepository.findById(request.getExportRequestId()).orElseThrow();

        if(exportRequest.getAssignedStaff() != null) {
            LOGGER.info("Return working for pre staff: {}",exportRequest.getAssignedStaff().getEmail());
            StaffPerformance staffPerformance = staffPerformanceRepository.
                    findByExportRequestIdAndAssignedStaff_IdAndExportCounting(exportRequest.getId(),exportRequest.getAssignedStaff().getId(),true);
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
            setTimeForCountingStaffPerformance(staff, exportRequest);
            exportRequest.setCountingStaffId(staff.getId());
        }
        exportRequest.setStatus(ImportStatus.IN_PROGRESS);
        exportRequestRepository.save(exportRequest);
        return mapToResponse(exportRequest);
    }

    public ExportRequestResponse confirmCountedExportRequest(String exportRequestId) {
        LOGGER.info("Confirming counted export request with ID: " + exportRequestId);
        ExportRequest exportRequest = exportRequestRepository.findById(exportRequestId).orElseThrow(
                () -> new NoSuchElementException("Export request not found with ID: " + exportRequestId));
        exportRequest.setStatus(ImportStatus.COUNTED);
        return mapToResponse(exportRequestRepository.save(exportRequest));
    }

    public ExportRequestResponse completeExportRequest(String exportRequestId) {
        LOGGER.info("Completing export request with ID: " + exportRequestId);
        ExportRequest exportRequest = exportRequestRepository.findById(exportRequestId).orElseThrow(
                () -> new NoSuchElementException("Export request not found with ID: " + exportRequestId));

        exportRequest.setStatus(ImportStatus.COMPLETED);
        updateInventoryItemAndLocationAfterExport(exportRequest);
        handleExportItems(exportRequest);
        return mapToResponse(exportRequestRepository.save(exportRequest));
    }

    public ExportRequestResponse updateExportStatus (String exportRequestId, ImportStatus status) {
        LOGGER.info("Updating export request status for export request with ID: " + exportRequestId);
        ExportRequest exportRequest = exportRequestRepository.findById(exportRequestId).orElseThrow(
                () -> new NoSuchElementException("Export request not found with ID: " + exportRequestId));

        if(status == ImportStatus.CANCELLED) {
            LOGGER.info("Updating export request status to CANCELLED");
            if(exportRequest.getStatus() != ImportStatus.NOT_STARTED
                    && exportRequest.getStatus() != ImportStatus.IN_PROGRESS
                    && exportRequest.getStatus() != ImportStatus.COUNTED) {
                throw new IllegalStateException("Cannot cancel export request: Status is not NOT_STARTED");
            }
            LOGGER.info("Return working for pre confirm staff: {}",exportRequest.getAssignedStaff().getEmail());
            StaffPerformance staffPerformance = staffPerformanceRepository.findByExportRequestIdAndAssignedStaff_IdAndExportCounting
                    (exportRequest.getId(),exportRequest.getAssignedStaff().getId(), false);
            if(staffPerformance != null) {
                LOGGER.info("Delete working time for pre staff: {}",exportRequest.getAssignedStaff().getEmail());
                staffPerformanceRepository.delete(staffPerformance);
            }

            for(ExportRequestDetail exportRequestDetail : exportRequest.getExportRequestDetails()) {
                LOGGER.info("remove item in export request detail: {}", exportRequestDetail.getId());
                exportRequestDetail.getInventoryItems()
                        .forEach(inventoryItem -> {
                            LOGGER.info("Update item status to AVAILABLE: {}", inventoryItem.getId());
                            inventoryItem.setStatus(ItemStatus.AVAILABLE);
                            inventoryItem.setExportRequestDetail(null);
                            inventoryItemRepository.save(inventoryItem);
                        });
            }
        }

        exportRequest.setStatus(status);
        return mapToResponse(exportRequestRepository.save(exportRequest));
    }

    public ExportRequestResponse updateExportDateTime(String exportRequestId, LocalDate date, LocalTime time) {
        LOGGER.info("Updating export date and time for export request with ID: " + exportRequestId);
        ExportRequest exportRequest = exportRequestRepository.findById(exportRequestId).orElseThrow(
                () -> new NoSuchElementException("Export request not found with ID: " + exportRequestId));

        validateForTimeDate(date, time);
        exportRequest.setExportDate(date);
        exportRequest.setExportTime(time);

        // reassign confirm staff
        if(exportRequest.getAssignedStaff() != null) {
            LOGGER.info("Return working for pre staff: {}",exportRequest.getAssignedStaff().getEmail());
            StaffPerformance staffPerformance = staffPerformanceRepository.
                    findByExportRequestIdAndAssignedStaff_IdAndExportCounting(exportRequest.getId(),exportRequest.getAssignedStaff().getId(), false);
            if(staffPerformance != null) {
                LOGGER.info("Delete working time for pre staff: {}",exportRequest.getAssignedStaff().getEmail());
                staffPerformanceRepository.delete(staffPerformance);
            }
        }
        // assign new confirm staff
        autoAssignConfirmStaff(exportRequest);
        return mapToResponse(exportRequest);
    }

    private void updateInventoryItemAndLocationAfterExport(ExportRequest exportRequest) {
        LOGGER.info("Updating inventory item after export request");

        List<ExportRequestDetail> exportRequestDetails = exportRequest.getExportRequestDetails();
        for(ExportRequestDetail exportRequestDetail : exportRequestDetails) {
            for(InventoryItem inventoryItem : exportRequestDetail.getInventoryItems()) {
                LOGGER.info("Updating inventory item id: {}", inventoryItem.getId());
                inventoryItem.setStatus(ItemStatus.UNAVAILABLE);
                inventoryItemRepository.save(inventoryItem);

                StoredLocation location = inventoryItem.getStoredLocation();
                if (location != null) {
                    LOGGER.info("Updating stored location id: {}", location.getId());
                    location.setCurrentCapacity(location.getCurrentCapacity() - inventoryItem.getMeasurementValue());
                    location.setFulled(false);
                    if(location.getCurrentCapacity() == 0) {
                        location.setUsed(false);
                    }
                    storedLocationRepository.save(location);
                }
            }
        }
    }

    private void handleExportItems(ExportRequest exportRequest) {
        Map<String, Item> updatedItems = new HashMap<>();
        for (ExportRequestDetail detail : exportRequest.getExportRequestDetails()) {
            for (InventoryItem inventoryItem : detail.getInventoryItems()) {
                Item item = inventoryItem.getItem();
                if (item != null) {
                    item.setTotalMeasurementValue(item.getTotalMeasurementValue() - inventoryItem.getMeasurementValue());
                    item.setQuantity(item.getQuantity() - 1);
                    updatedItems.put(item.getId(), item);
                }
            }
        }
        itemRepository.saveAll(updatedItems.values());
        LOGGER.info("Updated {} exported items", updatedItems.size());
    }

    private void setTimeForCountingStaffPerformance(Account account, ExportRequest exportRequest) {
        int totalMinutes = 0;
        for (ExportRequestDetail detail : exportRequest.getExportRequestDetails()) {
            LOGGER.info("Calculating expected working time for item: " + detail.getItem().getName());
            totalMinutes += detail.getQuantity() * detail.getItem().getCountingMinutes();
        }
        LocalTime expectedWorkingTime = LocalTime.of(0, 0).plusMinutes(totalMinutes);
        StaffPerformance staffPerformance = new StaffPerformance();
        staffPerformance.setExpectedWorkingTime(expectedWorkingTime);
        staffPerformance.setDate(exportRequest.getCountingDate());
        staffPerformance.setExportRequestId(exportRequest.getId());
        staffPerformance.setAssignedStaff(account);
        staffPerformance.setExportCounting(true);
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
            exportRequest.getCountingDate(),
            exportRequest.getCountingTime(),
            exportRequest.getCountingStaffId(),
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
        LOGGER.info("Validating time and date for export request");
        Configuration configuration = configurationRepository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Configuration not found with name: export request"));

        long minutesToAdd = configuration.getCreateRequestTimeAtLeast().getHour() * 60
                + configuration.getCreateRequestTimeAtLeast().getMinute();

        LOGGER.info("Check if date is in the past");
        if(date.isBefore(LocalDate.now())) {
            throw new IllegalStateException("Cannot set time for  export request: Date is in the past");
        }

        LOGGER.info("Check if time set is too early");
        if (date.isEqual(LocalDate.now()) &&
                LocalTime.now()
                        .plusMinutes(minutesToAdd)
                        .isAfter(time)) {
            throw new IllegalStateException("Cannot set time for  export request: Time is too early");
        }
    }

    private ExportRequest autoAssignCountingStaff(ExportRequest exportRequest) {
        ActiveAccountRequest activeAccountRequest = new ActiveAccountRequest();
        activeAccountRequest.setDate(exportRequest.getCountingDate());
        activeAccountRequest.setExportRequestId(exportRequest.getId());

        List<AccountResponse> accountResponse = accountService.getAllActiveStaffsInDate(activeAccountRequest);

        Account account = accountRepository.findById(accountResponse.get(0).getId())
                .orElseThrow(() -> new NoSuchElementException("Account not found with ID: " + accountResponse.get(0).getId()));
        exportRequest.setCountingStaffId(account.getId());
        setTimeForCountingStaffPerformance(account, exportRequest);
        autoAssignConfirmStaff(exportRequest);
        return exportRequestRepository.save(exportRequest);

    }

    private void autoAssignConfirmStaff(ExportRequest exportRequest) {
        LOGGER.info("Auto assigning confirm staff for export request with ID: " + exportRequest.getId());
        ActiveAccountRequest activeAccountRequest = new ActiveAccountRequest();
        activeAccountRequest.setDate(exportRequest.getExportDate());
        Configuration configuration = configurationRepository.findAll().getFirst();
        List<AccountResponse> accountResponses = accountService.getAllActiveStaffsInDate(activeAccountRequest);
        List<AccountResponse> responses = new ArrayList<>();

        for(AccountResponse accountResponse : accountResponses) {
            List<ExportRequest> checkExportRequest = exportRequestRepository.findAllByAssignedStaff_IdAndExportDate(
                    accountResponse.getId(),
                    exportRequest.getExportDate()
            );
            LOGGER.info("Checking export requests size {} ", checkExportRequest.size());

            if (checkExportRequest.isEmpty()) {
                responses.add(accountResponse);
            } else {
                for (ExportRequest exportCheck : checkExportRequest) {
                    if (exportCheck.getExportTime().isAfter(exportCheck.getExportTime().plusMinutes(configuration.getTimeToAllowConfirm().toSecondOfDay() / 60))) {
                        responses.add(accountResponse);
                    }
                }
            }
        }

        Account account = accountRepository.findById(responses.get(0).getId())
                .orElseThrow(() -> new NoSuchElementException("Account not found with ID: " + responses.get(0).getId()));

        exportRequest.setAssignedStaff(account);
        LOGGER.info("Confirm Account is: {}", account.getEmail());
        StaffPerformance staffPerformance = new StaffPerformance();
        staffPerformance.setExpectedWorkingTime(configuration.getTimeToAllowConfirm());
        staffPerformance.setDate(exportRequest.getExportDate());
        staffPerformance.setAssignedStaff(account);
        staffPerformance.setExportCounting(false);
        staffPerformance.setExportRequestId(exportRequest.getId());
        staffPerformanceRepository.save(staffPerformance);
        exportRequestRepository.save(exportRequest);
    }

    private String createExportRequestId() {
        String prefix = "ER";
        LocalDate today = LocalDate.now();

        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

        int todayCount = exportRequestRepository.countByCreatedAtBetween(startOfDay, endOfDay);

        String datePart = today.format(DateTimeFormatter.BASIC_ISO_DATE); // yyyyMMdd
        String sequence = String.format("%03d", todayCount + 1);          // 001, 002, ...

        return String.format("%s-%s-%s", prefix, datePart, sequence);
    }

} 