package capstonesu25.warehouse.service;

import capstonesu25.warehouse.entity.*;
import capstonesu25.warehouse.enums.*;
import capstonesu25.warehouse.model.account.AccountResponse;
import capstonesu25.warehouse.model.account.ActiveAccountRequest;
import capstonesu25.warehouse.model.importorder.ImportOrderCreateRequest;
import capstonesu25.warehouse.model.importorder.ImportOrderResponse;
import capstonesu25.warehouse.model.importorder.ImportOrderUpdateRequest;
import capstonesu25.warehouse.repository.*;
import capstonesu25.warehouse.utils.NotificationUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class ImportOrderService {
    private final ImportOrderRepository importOrderRepository;
    private final ImportRequestRepository importRequestRepository;
    private final AccountRepository accountRepository;
    private final StaffPerformanceRepository staffPerformanceRepository;
    private final ConfigurationRepository configurationRepository;
    private final ImportRequestDetailRepository  importRequestDetailRepository;
    private final ImportOrderDetailRepository importOrderDetailRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final StoredLocationRepository storedLocationRepository;
    private final ItemRepository itemRepository;
    private final NotificationService notificationService;
    private final AccountService accountService;
    private static final Logger LOGGER = LoggerFactory.getLogger(ImportOrderService.class);

    public ImportOrderResponse getImportOrderById(String id) {
        LOGGER.info("Get import order by id: " + id);
        ImportOrder importOrder = importOrderRepository.findById(id).orElseThrow();
        return mapToResponse(importOrder);
    }

    public List<ImportOrderResponse> getImportOrdersByImportRequestId(String id) {
        LOGGER.info("Get import orders by import request id: " + id);
        List<ImportOrder> importOrders = importOrderRepository.findImportOrdersByImportRequest_Id(id);
        return importOrders.stream().map(this::mapToResponse).toList();
    }

    public ImportOrderResponse create(ImportOrderCreateRequest request) {
        LOGGER.info("Create new import order");
        
        ImportRequest importRequest = importRequestRepository.findById(request.getImportRequestId())
                .orElseThrow(() -> new NoSuchElementException("ImportRequest not found with ID: " + request.getImportRequestId()));
        boolean canBeContinued = false;
        for(ImportRequestDetail detail : importRequest.getDetails()) {
           if(detail.getActualQuantity() == 0) {
               // If no actual imports yet, check against ordered quantity
               if(detail.getOrderedQuantity() < detail.getExpectQuantity()) {
                   canBeContinued = true;
               }
           } else {
               // If there are actual imports, check against actual quantity
               if(detail.getActualQuantity() < detail.getExpectQuantity()) {
                   canBeContinued = true;
               }
           }
        }
        if(!canBeContinued) {
            throw new IllegalStateException("Cannot create import order: All items have been fully imported or planned");
        }

        ImportOrder importOrder = new ImportOrder();
        importOrder.setId(createImportOrderId(importRequest));
        importOrder.setImportRequest(importRequest);
        if(request.getDateReceived() != null && request.getTimeReceived() != null) {
            validateForTimeDate(request.getDateReceived(), request.getTimeReceived());
            importOrder.setDateReceived(request.getDateReceived());
            importOrder.setTimeReceived(request.getTimeReceived());
        }
        importOrder.setStatus(RequestStatus.IN_PROGRESS);
        
        if (request.getAccountId() != null) {
            Account account = accountRepository.findById(request.getAccountId())
                    .orElseThrow(() -> new NoSuchElementException("Account not found with ID: " + request.getAccountId()));
            validateAccountForAssignment(account);
            importOrder.setAssignedStaff(account);
            importOrder.setStatus(RequestStatus.IN_PROGRESS);
        }
        
        if (request.getNote() != null) {
            importOrder.setNote(request.getNote());
        }
        if(request.getDateReceived() != null && request.getTimeReceived() != null) {
            validateForTimeDate(request.getDateReceived(), request.getTimeReceived());
            importOrder.setDateReceived(request.getDateReceived());
            importOrder.setTimeReceived(request.getTimeReceived());
        }
        importRequest.setStatus(RequestStatus.IN_PROGRESS);
        importRequestRepository.save(importRequest);
        ImportOrder order = importOrderRepository.save(importOrder);

        notificationService.handleNotification(
            NotificationUtil.WAREHOUSE_MANAGER_CHANNEL,
            NotificationUtil.IMPORT_ORDER_CREATED_EVENT,
            order.getId(),
            "Đơn nhập mã #" + order.getId() + " đã được tạo",
            accountRepository.findByRole(AccountRole.WAREHOUSE_MANAGER)
        );
        
        return mapToResponse(importOrderRepository.save(order));
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
        int totalMinutes = 0;
        for (ImportOrderDetail detail : importOrder.getImportOrderDetails()) {
            LOGGER.info("Calculating expected working time for item: " + detail.getItem().getName());
            totalMinutes += detail.getExpectQuantity() * detail.getItem().getCountingMinutes();
        }
        LocalTime expectedWorkingTime = LocalTime.of(0, 0).plusMinutes(totalMinutes);
        StaffPerformance staffPerformance = new StaffPerformance();
        staffPerformance.setExpectedWorkingTime(expectedWorkingTime);
        staffPerformance.setDate(importOrder.getDateReceived());
        staffPerformance.setImportOrderId(importOrder.getId());
        staffPerformance.setAssignedStaff(account);
        staffPerformanceRepository.save(staffPerformance);
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

    public void delete(String id) {
        LOGGER.info("Delete import order");
        ImportOrder importOrder = importOrderRepository.findById(id).orElseThrow();
        importOrderRepository.delete(importOrder);
    }

    public ImportOrderResponse assignStaff(String importOrderId, Long accountId) {
        LOGGER.info("Assigning staff to import order: " + importOrderId);

        ImportOrder importOrder = importOrderRepository.findById(importOrderId)
                .orElseThrow(() -> new NoSuchElementException("Import Order not found with ID: " + importOrderId));
        if(importOrder.getAssignedStaff() != null) {
            LOGGER.info("Return working for pre staff: {}",importOrder.getAssignedStaff().getEmail());
            StaffPerformance staffPerformance = staffPerformanceRepository.
                    findByImportOrderIdAndAssignedStaff_Id(importOrderId,importOrder.getAssignedStaff().getId());
            if(staffPerformance != null) {
            LOGGER.info("Delete working time for pre staff: {}",importOrder.getAssignedStaff().getEmail());
                staffPerformanceRepository.delete(staffPerformance);
            }
        }
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new NoSuchElementException("Account not found with ID: " + accountId));
        validateAccountForAssignment(account);
        setTimeForStaffPerformance(account, importOrder);
        importOrder.setAssignedStaff(account);
        importOrder.setStatus(RequestStatus.IN_PROGRESS);
        
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

    public ImportOrderResponse cancelImportOrder(String importOrderId) {
        LOGGER.info("Cancelling import order with ID: " + importOrderId);
        
        ImportOrder importOrder = importOrderRepository.findById(importOrderId)
                .orElseThrow(() -> new NoSuchElementException("ImportOrder not found with ID: " + importOrderId));
                
        // Can only cancel orders that are NOT_STARTED or IN_PROGRESS
        if (importOrder.getStatus() == RequestStatus.COMPLETED || importOrder.getStatus() == RequestStatus.CANCELLED) {
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
        
        importOrder.setStatus(RequestStatus.CANCELLED);
        notificationService.handleNotification(
            NotificationUtil.DEPARTMENT_CHANNEL,
            NotificationUtil.IMPORT_ORDER_CANCELLED_EVENT,
            importOrderId,
            "Đơn nhập mã #" + importOrderId + " đã bị hủy",
            accountRepository.findByRole(AccountRole.DEPARTMENT)
        );
        notificationService.handleNotification(
            NotificationUtil.WAREHOUSE_MANAGER_CHANNEL,
            NotificationUtil.IMPORT_ORDER_CANCELLED_EVENT,
            importOrderId,
            "Đơn nhập mã #" + importOrderId + " đã bị hủy",
            accountRepository.findByRole(AccountRole.WAREHOUSE_MANAGER)
        );
        return mapToResponse(importOrderRepository.save(importOrder));
    }

    public ImportOrderResponse completeImportOrder(String importOrderId) {
        LOGGER.info("Completing import order with ID: " + importOrderId);
        ImportOrder importOrder = importOrderRepository.findById(importOrderId)
                .orElseThrow(() -> new NoSuchElementException("ImportOrder not found with ID: " + importOrderId));
        importOrder.setStatus(RequestStatus.COMPLETED);
        updateImportRequest(importOrder);
        updateImportOrder(importOrder);
        autoFillLocationForImport(importOrder);
        handleImportItems(importOrder);
        notificationService.handleNotification(
            NotificationUtil.DEPARTMENT_CHANNEL,
            NotificationUtil.IMPORT_ORDER_COMPLETED_EVENT,
            importOrderId,
            "Đơn nhập mã #" + importOrderId + " đã hoàn tất",
            accountRepository.findByRole(AccountRole.DEPARTMENT)
        );
        return mapToResponse(importOrderRepository.save(importOrder));
    }

    public ImportOrderResponse extendImportOrder(String importOrderId, LocalDate extendedDate, LocalTime extendedTime, String extendedReason) {
        LOGGER.info("Extending import order with ID: " + importOrderId);

        ImportOrder importOrder = importOrderRepository.findById(importOrderId)
                .orElseThrow(() -> new NoSuchElementException("ImportOrder not found with ID: " + importOrderId));

        if(importOrder.isExtended()) {
            throw new IllegalStateException("Import order has already been extended");
        }

        if (importOrder.getStatus() != RequestStatus.IN_PROGRESS
                && importOrder.getStatus() != RequestStatus.EXTENDED) {
            throw new IllegalStateException("Cannot extend import order with status: " + importOrder.getStatus());
        }

        Configuration configuration = configurationRepository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Configuration not found"));
        importOrder.setStatus(RequestStatus.EXTENDED);
        importOrder.setExtended(true);
        if(extendedDate == null) {
             extendedDate = importOrder.getDateReceived().plusDays(configuration.getMaxAllowedDaysForExtend());
        }
        importOrder.setExtendedDate(extendedDate);
        if(extendedTime == null) {
            extendedTime = importOrder.getTimeReceived();
        }
        importOrder.setExtendedTime(extendedTime);
        importOrder.setExtendedReason(extendedReason);

        LOGGER.info("Auto assign staff after extended");
        ActiveAccountRequest activeAccountRequest = new ActiveAccountRequest();
        activeAccountRequest.setDate(extendedDate);
        activeAccountRequest.setImportOrderId(importOrder.getId());
        List<AccountResponse> accountResponse = accountService.getAllActiveStaffsInDate(activeAccountRequest);
        Account account = accountRepository.findById(accountResponse.get(0).getId())
                .orElseThrow(() -> new NoSuchElementException("Account not found with ID: " + accountResponse.get(0).getId()));

        importOrder.setAssignedStaff(account);
        setTimeForStaffPerformance(importOrder.getAssignedStaff(), importOrder);
        notificationService.handleNotification(
            NotificationUtil.DEPARTMENT_CHANNEL,
            NotificationUtil.IMPORT_ORDER_EXTENDED_EVENT,
            importOrderId,
            "Đơn nhập mã #" + importOrderId + " đã được gia hạn",
            accountRepository.findByRole(AccountRole.DEPARTMENT)
        );
        return mapToResponse(importOrderRepository.save(importOrder));
    }

    private void updateImportRequest( ImportOrder importOrder) {
        LOGGER.info("Updating import request after paper creation");

        ImportRequest importRequest = importOrder.getImportRequest();
        List<ImportRequestDetail> importRequestDetails = importRequest.getDetails();
        for (ImportRequestDetail detail : importRequestDetails) {
            for(ImportOrderDetail importOrderDetail : importOrder.getImportOrderDetails()) {
                if (detail.getItem().getId().equals(importOrderDetail.getItem().getId())) {
                    detail.setActualQuantity(detail.getActualQuantity() + importOrderDetail.getActualQuantity());
                    if(detail.getActualQuantity() == detail.getExpectQuantity()) {
                        detail.setStatus(DetailStatus.MATCH);
                    } else if(detail.getActualQuantity() > detail.getExpectQuantity()) {
                        detail.setStatus(DetailStatus.EXCESS);
                    } else {
                        detail.setStatus(DetailStatus.LACK);
                    }
                    importRequestDetailRepository.save(detail);
                }
            }
        }

        boolean allCompleted = true;
        for (ImportRequestDetail detail : importRequestDetails) {
            if (detail.getActualQuantity() < detail.getExpectQuantity()) {
                allCompleted = false;
                break;
            }
        }

        if (allCompleted) {
            importRequest.setStatus(RequestStatus.COMPLETED);
            importRequestRepository.save(importRequest);
        }
    }

    //Update import Order
    private void updateImportOrder (ImportOrder importOrder) {
        LOGGER.info("Updating import order after completed");
        List<ImportOrderDetail> importOrderDetails = importOrder.getImportOrderDetails();
        for(ImportOrderDetail importOrderDetail : importOrderDetails) {
            LOGGER.info("Create inventory item for import order detail id: {}", importOrderDetail.getId());
            createInventoryItem(importOrderDetail);
            LOGGER.info("Update status for import order detail id: {}", importOrderDetail.getId());
            if(importOrderDetail.getActualQuantity() == importOrderDetail.getExpectQuantity()) {
                importOrderDetail.setStatus(DetailStatus.MATCH);
            } else if(importOrderDetail.getActualQuantity() > importOrderDetail.getExpectQuantity()) {
                importOrderDetail.setStatus(DetailStatus.EXCESS);
            } else {
                importOrderDetail.setStatus(DetailStatus.LACK);
            }
            importOrderDetailRepository.save(importOrderDetail);
        }

        importOrder.setUpdatedDate(LocalDateTime.now());
        importOrderRepository.save(importOrder);
    }

    private void autoFillLocationForImport(ImportOrder importOrder) {
        LOGGER.info("Auto fill location");

        List<ImportOrderDetail> importOrderDetails = importOrder.getImportOrderDetails();
        for(ImportOrderDetail importOrderDetail : importOrderDetails) {
            List<InventoryItem> inventoryItemList = importOrderDetail.getItem().getInventoryItems();
            //get the stored location
            List<StoredLocation> storedLocationList = storedLocationRepository
                    .findByItem_IdAndIsFulledFalseOrderByZoneAscFloorAscRowAscBatchAsc(importOrderDetail.getItem().getId());
            for (InventoryItem inventoryItem : inventoryItemList) {
                for (StoredLocation storedLocation : storedLocationList) {
                    if (storedLocation.getCurrentCapacity() + inventoryItem.getMeasurementValue() <= storedLocation.getMaximumCapacityForItem()) {
                        inventoryItem.setStoredLocation(storedLocation);
                        double newCapacity = storedLocation.getCurrentCapacity() + inventoryItem.getMeasurementValue();
                        storedLocation.setCurrentCapacity(newCapacity);

                        storedLocation.setUsed(true);

                        boolean isNowFull = (storedLocation.getMaximumCapacityForItem() - newCapacity) < inventoryItem.getMeasurementValue();
                        storedLocation.setFulled(isNowFull);

                        storedLocationRepository.save(storedLocation);
                        break;
                    }
                }

                inventoryItem.setStatus(ItemStatus.AVAILABLE);
                inventoryItemRepository.save(inventoryItem);
            }

        }

    }

    private void createInventoryItem(ImportOrderDetail importOrderDetail) {
        LOGGER.info("Creating inventory item for import order detail id: {}", importOrderDetail.getId());
        for(int i = 0; i < importOrderDetail.getActualQuantity(); i++) {
            InventoryItem inventoryItem = new InventoryItem();
            inventoryItem.setId(createInventoryItemId(importOrderDetail, i));
            inventoryItem.setImportOrderDetail(importOrderDetail);
            inventoryItem.setItem(importOrderDetail.getItem());
            inventoryItem.setImportedDate(LocalDateTime.of(importOrderDetail.getImportOrder().getDateReceived(),
                    importOrderDetail.getImportOrder().getTimeReceived()));
            inventoryItem.setMeasurementValue(importOrderDetail.getItem().getMeasurementValue());
            inventoryItem.setStatus(ItemStatus.AVAILABLE);
            inventoryItem.setUpdatedDate(LocalDateTime.now());
            if(importOrderDetail.getItem().getDaysUntilDue() != null) {
                inventoryItem.setExpiredDate(inventoryItem.getImportedDate().plusDays(importOrderDetail.getItem().getDaysUntilDue()));
            }
            inventoryItemRepository.save(inventoryItem);
        }
    }



    private void handleImportItems(ImportOrder importOrder) {
        LOGGER.info("Handling import items for import order id: {}", importOrder.getId());
        Map<String, Item> updatedItems = new HashMap<>();

        for (ImportOrderDetail detail : importOrder.getImportOrderDetails()) {
            for (InventoryItem inventoryItem : detail.getInventoryItems()) {
                Item item = inventoryItem.getItem();
                if (item != null) {
                    item.setTotalMeasurementValue(item.getTotalMeasurementValue() + inventoryItem.getMeasurementValue());
                    item.setQuantity(item.getQuantity() + 1);
                    updatedItems.put(item.getId(), item);
                }
            }
        }

        itemRepository.saveAll(updatedItems.values());
        LOGGER.info("Updated {} imported items", updatedItems.size());
    }

    private ImportOrderResponse mapToResponse(ImportOrder importOrder) {
        return new ImportOrderResponse(
                importOrder.getId(),
                importOrder.getImportRequest() != null ? importOrder.getImportRequest().getId() : null,
                importOrder.getDateReceived(),
                importOrder.getTimeReceived(),
                importOrder.isExtended(),
                importOrder.getExtendedDate(),
                importOrder.getExtendedTime(),
                importOrder.getExtendedReason(),
                importOrder.getNote(),
                importOrder.getStatus() != null ? importOrder.getStatus() : null,
                importOrder.getImportOrderDetails() != null ? importOrder.getImportOrderDetails().stream()
                        .map(ImportOrderDetail::getId).toList() : null,
                importOrder.getActualDateReceived(),
                importOrder.getActualTimeReceived(),
                importOrder.getCreatedBy(),
                importOrder.getUpdatedBy(),
                importOrder.getCreatedDate(),
                importOrder.getUpdatedDate(),
                importOrder.getPaper() != null ? importOrder.getPaper().getId() : null,
                importOrder.getAssignedStaff() != null ? importOrder.getAssignedStaff().getId() : null);
    }

    private String createImportOrderId (ImportRequest importRequest) {
        int size = importRequest.getImportOrders().size();
        return  "DN-" + importRequest.getId() + "-" + (size + 1);
    }

    private String createInventoryItemId(ImportOrderDetail importOrderDetail, int index) {
        return "ITM-" +importOrderDetail.getItem().getId()+"-"+ importOrderDetail.getImportOrder().getId() + "-" +
                + (index + 1);
    }
}
