package capstonesu25.warehouse.service;

import capstonesu25.warehouse.entity.Account;
import capstonesu25.warehouse.entity.ImportOrder;
import capstonesu25.warehouse.entity.ImportOrderDetail;
import capstonesu25.warehouse.entity.ImportRequest;
import capstonesu25.warehouse.enums.AccountRole;
import capstonesu25.warehouse.enums.AccountStatus;
import capstonesu25.warehouse.enums.ImportStatus;
import capstonesu25.warehouse.model.importorder.ImportOrderCreateRequest;
import capstonesu25.warehouse.model.importorder.ImportOrderResponse;
import capstonesu25.warehouse.model.importorder.ImportOrderUpdateRequest;
import capstonesu25.warehouse.repository.AccountRepository;
import capstonesu25.warehouse.repository.ImportOrderRepository;
import capstonesu25.warehouse.repository.ImportRequestRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class ImportOrderService {
    private final ImportOrderRepository importOrderRepository;
    private final ImportRequestRepository importRequestRepository;
    private final AccountRepository accountRepository;
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
        LOGGER.info("Update account status to INACTIVE");
        validateAccountForAssignment(account);
        LOGGER.info("Update account status to INACTIVE");
        account.setStatus(AccountStatus.INACTIVE);
        accountRepository.save(account);
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
