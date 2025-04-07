package capstonesu25.warehouse.service;

import capstonesu25.warehouse.entity.Account;
import capstonesu25.warehouse.entity.ImportOrder;
import capstonesu25.warehouse.entity.ImportOrderDetail;
import capstonesu25.warehouse.entity.ImportRequest;
import capstonesu25.warehouse.model.importorder.ImportOrderRequest;
import capstonesu25.warehouse.model.importorder.ImportOrderResponse;
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
        Page<ImportOrder> importOrders = importOrderRepository.findImportOrdersByImportRequest_Id(id,pageable);
        return importOrders.map(this::mapToResponse);
    }

    public ImportOrderResponse save (ImportOrderRequest request) {
        LOGGER.info("Create import order");
        ImportOrder importOrder;
        if(request.getImportOrderId() != null) {
             LOGGER.info("Update import order");
             importOrder = importOrderRepository.findById(request.getImportOrderId())
                     .orElseThrow(() -> new NoSuchElementException("ImportRequest not found with ID: " + request.getImportRequestId()));
             importOrder.setStatus(request.getStatus());
        }else {
             LOGGER.info("Create import order");
             importOrder = new ImportOrder();
        }
        ImportRequest importRequest = importRequestRepository.findById
                (request.getImportRequestId()).orElseThrow();
        importOrder.setImportRequest(importRequest);
        if(request.getAccountId() != null) {
            importOrder.setAssignedWareHouseKeeper(accountRepository.findById
                    (request.getAccountId()).orElseThrow());
        }
        if (request.getNote() != null){
            importOrder.setNote(request.getNote());
        }
        importOrder.setDateReceived(request.getDateReceived());
        importOrder.setTimeReceived(request.getTimeReceived());
        return mapToResponse(importOrderRepository.save(importOrder));
    }

    public void delete(Long id) {
        LOGGER.info("Delete import order");
        ImportOrder importOrder = importOrderRepository.findById(id).orElseThrow();
        importOrderRepository.delete(importOrder);
    }

    public ImportOrderResponse assignWarehouseKeeper(Long importOrderId, Long accountId) {
        LOGGER.info("Assigning warehouse keeper to import order: " + importOrderId);
        
        ImportOrder importOrder = importOrderRepository.findById(importOrderId)
                .orElseThrow(() -> new NoSuchElementException("Import Order not found with ID: " + importOrderId));
        
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new NoSuchElementException("Account not found with ID: " + accountId));
        
        importOrder.setAssignedWareHouseKeeper(account);
        return mapToResponse(importOrderRepository.save(importOrder));
    }

    private ImportOrderResponse mapToResponse(ImportOrder importOrder) {
        return new ImportOrderResponse(
                importOrder.getId(),
                importOrder.getImportRequest() != null? importOrder.getImportRequest().getId() : null,
                importOrder.getDateReceived(),
                importOrder.getTimeReceived(),
                importOrder.getNote(),
                importOrder.getStatus() != null? importOrder.getStatus() : null,
                importOrder.getImportOrderDetails() != null? importOrder.getImportOrderDetails().stream()
                        .map(ImportOrderDetail::getId).toList() : null,
                importOrder.getCreatedBy(),
                importOrder.getUpdatedBy(),
                importOrder.getCreatedDate(),
                importOrder.getUpdatedDate(),
                importOrder.getPaper() != null? importOrder.getPaper().getId() : null,
                importOrder.getAssignedWareHouseKeeper() != null?
                    importOrder.getAssignedWareHouseKeeper().getId() : null
                );
    }
}
