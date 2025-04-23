package capstonesu25.warehouse.service;

import capstonesu25.warehouse.entity.*;
import capstonesu25.warehouse.enums.AccountRole;
import capstonesu25.warehouse.enums.AccountStatus;
import capstonesu25.warehouse.enums.ImportStatus;
import capstonesu25.warehouse.model.importorder.ImportOrderCreateRequest;
import capstonesu25.warehouse.model.importorder.ImportOrderResponse;
import capstonesu25.warehouse.model.importorder.ImportOrderUpdateRequest;
import capstonesu25.warehouse.repository.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class ImportOrderService {
    private final ImportOrderRepository importOrderRepository;
    private final ImportRequestRepository importRequestRepository;
    private final AccountRepository accountRepository;
    private final StaffPerformanceRepository staffPerformanceRepository;
    private final ConfigurationRepository configurationRepository;
    private static final Logger LOGGER = LoggerFactory.getLogger(ImportOrderService.class);

    public ImportOrderResponse getImportOrderById(Long id) {
        LOGGER.info("Get import order by id: " + id);
        ImportOrder importOrder = importOrderRepository.findById(id).orElseThrow();
        return mapToResponse(importOrder);
    }

    public Page<ImportOrderResponse> getImportOrdersByImportRequestId(Long id, int page, int limit) {
        LOGGER.info("Get import orders by import request id: " + id);
        Pageable pageable = PageRequest.of(page - 1, limit);
        Page<ImportOrder> importOrders = importOrderRepository.findImportOrdersByImportRequest_Id(id, pageable);
        return importOrders.map(this::mapToResponse);
    }

    public ImportOrderResponse create(ImportOrderCreateRequest request) {
        LOGGER.info("Create new import order");
        
        ImportRequest importRequest = importRequestRepository.findById(request.getImportRequestId())
                .orElseThrow(() -> new NoSuchElementException("ImportRequest not found with ID: " + request.getImportRequestId()));

        ImportOrder importOrder = new ImportOrder();
        importOrder.setImportRequest(importRequest);
        importOrder.setStatus(ImportStatus.NOT_STARTED);
        
        if (request.getAccountId() != null) {
            Account account = accountRepository.findById(request.getAccountId())
                    .orElseThrow(() -> new NoSuchElementException("Account not found with ID: " + request.getAccountId()));
            validateAccountForAssignment(account);
//            updateAccountStatusForImportRequest(account, importOrder);
            importOrder.setAssignedStaff(account);
            importOrder.setStatus(ImportStatus.IN_PROGRESS);
        }
        
        if (request.getNote() != null) {
            importOrder.setNote(request.getNote());
        }
        
        importOrder.setDateReceived(request.getDateReceived());
        importOrder.setTimeReceived(request.getTimeReceived());
        
        // Update import request status to IN_PROGRESS
        importRequest.setStatus(ImportStatus.IN_PROGRESS);
        importRequestRepository.save(importRequest);

        return mapToResponse(importOrderRepository.save(importOrder));
    }

    public ImportOrderResponse update(ImportOrderUpdateRequest request) {
        LOGGER.info("Update import order");
        
        ImportOrder importOrder = importOrderRepository.findById(request.getImportOrderId())
                .orElseThrow(() -> new NoSuchElementException("ImportOrder not found with ID: " + request.getImportOrderId()));

        if (request.getNote() != null) {
            importOrder.setNote(request.getNote());
        }
        
        if (request.getDateReceived() != null) {
            importOrder.setDateReceived(request.getDateReceived());
        }
        
        if (request.getTimeReceived() != null) {
            importOrder.setTimeReceived(request.getTimeReceived());
        }
        
        return mapToResponse(importOrderRepository.save(importOrder));
    }

    private void validateAccountForAssignment(Account account) {
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new IllegalStateException("Cannot assign staff: Account is not active");
        }

        if (account.getRole() != AccountRole.STAFF) {
            throw new IllegalStateException("Cannot assign staff: Account is not a staff member");
        }
    }

    private void setTimeForStaffPerformance(Account account, ImportOrder importOrder) {
        Configuration configuration = configurationRepository.findById(1L)
                .orElseThrow(() -> new NoSuchElementException("Configuration not found with ID: 1"));
        int totalMinutes = 0;
        for (ImportOrderDetail detail : importOrder.getImportOrderDetails()) {
            LOGGER.info("Calculating expected working time for item: " + detail.getItem().getName());
            totalMinutes += detail.getExpectQuantity() * detail.getItem().getCountingMinutes();
        }

        LocalTime expectedWorkingTime = LocalTime.of(0, 0).plusMinutes(totalMinutes);

        StaffPerformance staffPerformance = new StaffPerformance();
        staffPerformance.setExpectedWorkingTime(expectedWorkingTime);
        staffPerformance.setDate(importOrder.getDateReceived());
        staffPerformanceRepository.save(staffPerformance);
    }

    private void updateAccountStatusForImportRequest(Account account, ImportOrder importOrder) {
        LOGGER.info("Update account status to INACTIVE");
        if(importOrder.getAssignedStaff() != null) {
            // If the import order is being reassigned, set the previous staff's status to ACTIVE
            LOGGER.info("Update previous staff status to ACTIVE");
            Account preStaff = importOrder.getAssignedStaff();
            preStaff.setStatus(AccountStatus.ACTIVE);
            accountRepository.save(preStaff);
        }
        account.setStatus(AccountStatus.INACTIVE);
        accountRepository.save(account);
    }

    public void delete(Long id) {
        LOGGER.info("Delete import order");
        ImportOrder importOrder = importOrderRepository.findById(id).orElseThrow();
        importOrderRepository.delete(importOrder);
    }

    public ImportOrderResponse assignStaff(Long importOrderId, Long accountId) {
        LOGGER.info("Assigning staff to import order: " + importOrderId);

        ImportOrder importOrder = importOrderRepository.findById(importOrderId)
                .orElseThrow(() -> new NoSuchElementException("Import Order not found with ID: " + importOrderId));

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new NoSuchElementException("Account not found with ID: " + accountId));
        validateAccountForAssignment(account);
//        updateAccountStatusForImportRequest(account, importOrder);
        importOrder.setAssignedStaff(account);
        importOrder.setStatus(ImportStatus.IN_PROGRESS);
        
        return mapToResponse(importOrderRepository.save(importOrder));
    }

    public Page<ImportOrderResponse> getImportOrdersByStaffId(Long staffId, int page, int limit) {
        LOGGER.info("Get import orders by staff id: " + staffId);
        Pageable pageable = PageRequest.of(page - 1, limit);
        Page<ImportOrder> importOrders = importOrderRepository.findImportOrdersByAssignedStaff_Id(staffId, pageable);
        return importOrders.map(this::mapToResponse);
    }

    public Page<ImportOrderResponse> getAllImportOrders(int page, int limit) {
        LOGGER.info("Get all import orders");
        Pageable pageable = PageRequest.of(page - 1, limit);
        Page<ImportOrder> importOrders = importOrderRepository.findAll(pageable);
        return importOrders.map(this::mapToResponse);
    }

    public ImportOrderResponse cancelImportOrder(Long importOrderId) {
        LOGGER.info("Cancelling import order with ID: " + importOrderId);
        
        ImportOrder importOrder = importOrderRepository.findById(importOrderId)
                .orElseThrow(() -> new NoSuchElementException("ImportOrder not found with ID: " + importOrderId));
                
        // Can only cancel orders that are NOT_STARTED or IN_PROGRESS
        if (importOrder.getStatus() == ImportStatus.COMPLETED || importOrder.getStatus() == ImportStatus.CANCELLED) {
            throw new IllegalStateException("Cannot cancel import order with status: " + importOrder.getStatus());
        }
        
        // Update ordered quantities in import request details
        ImportRequest importRequest = importOrder.getImportRequest();
        for (ImportOrderDetail orderDetail : importOrder.getImportOrderDetails()) {
            importRequest.getDetails().stream()
                    .filter(requestDetail -> requestDetail.getItem().getId().equals(orderDetail.getItem().getId()))
                    .findFirst()
                    .ifPresent(requestDetail -> {
                        requestDetail.setOrderedQuantity(requestDetail.getOrderedQuantity() - orderDetail.getExpectQuantity());
                        importRequestRepository.save(importRequest);
                    });
        }
        
        // If staff was assigned, set them back to ACTIVE
        if (importOrder.getAssignedStaff() != null) {
            Account staff = importOrder.getAssignedStaff();
            staff.setStatus(AccountStatus.ACTIVE);
            accountRepository.save(staff);
            importOrder.setAssignedStaff(null);
        }
        
        importOrder.setStatus(ImportStatus.CANCELLED);
        return mapToResponse(importOrderRepository.save(importOrder));
    }

    private ImportOrderResponse mapToResponse(ImportOrder importOrder) {
        return new ImportOrderResponse(
                importOrder.getId(),
                importOrder.getImportRequest() != null ? importOrder.getImportRequest().getId() : null,
                importOrder.getDateReceived(),
                importOrder.getTimeReceived(),
                importOrder.getNote(),
                importOrder.getStatus() != null ? importOrder.getStatus() : null,
                importOrder.getImportOrderDetails() != null ? importOrder.getImportOrderDetails().stream()
                        .map(ImportOrderDetail::getId).toList() : null,
                importOrder.getCreatedBy(),
                importOrder.getUpdatedBy(),
                importOrder.getCreatedDate(),
                importOrder.getUpdatedDate(),
                importOrder.getPaper() != null ? importOrder.getPaper().getId() : null,
                importOrder.getAssignedStaff() != null ? importOrder.getAssignedStaff().getId() : null);
    }
}
