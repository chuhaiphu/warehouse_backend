package capstonesu25.warehouse.service;

import capstonesu25.warehouse.entity.*;
import capstonesu25.warehouse.enums.AccountRole;
import capstonesu25.warehouse.enums.AccountStatus;
import capstonesu25.warehouse.enums.ItemStatus;
import capstonesu25.warehouse.enums.RequestStatus;
import capstonesu25.warehouse.model.stockcheck.AssignStaffStockCheck;
import capstonesu25.warehouse.model.stockcheck.StockCheckRequestRequest;
import capstonesu25.warehouse.model.stockcheck.StockCheckRequestResponse;
import capstonesu25.warehouse.repository.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class StockCheckService {
    private final StockCheckRequestRepository stockCheckRequestRepository;
    private final ConfigurationRepository configurationRepository;
    private final StaffPerformanceRepository staffPerformanceRepository;
    private final AccountRepository accountRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final ItemRepository itemRepository;
    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(StockCheckService.class);

    public StockCheckRequestResponse getStockCheckRequestById(String id) {
        LOGGER.info("Fetching stock check request with ID: {}", id);
        StockCheckRequest stockCheckRequest = stockCheckRequestRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Stock check request not found with ID: " + id));
        return mapToResponse(stockCheckRequest);
    }

    public List<StockCheckRequestResponse> getAllStockCheckRequests() {
        LOGGER.info("Fetching all stock check requests");
        List<StockCheckRequest> stockCheckRequests = stockCheckRequestRepository.findAll();
        return stockCheckRequests.stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<StockCheckRequestResponse> getAllStockCheckRequestsByStaffId(Long staffId) {
        LOGGER.info("Fetching all stock check requests by staff ID: {}", staffId);
        List<StockCheckRequest> stockCheckRequests = stockCheckRequestRepository.findByAssignedStaff_Id(staffId);
        return stockCheckRequests.stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public StockCheckRequestResponse createStockCheckRequest(StockCheckRequestRequest request) {
        LOGGER.info("Creating stock check request with data: {}", request);
        StockCheckRequest stockCheckRequest = new StockCheckRequest();
        LOGGER.info("Setting stock check request properties");
        String id = createStockCheckID();
        LOGGER.info("ID is : {}", id);
        stockCheckRequest.setId(id);
        stockCheckRequest.setStockCheckReason(request.getStockCheckReason());
        stockCheckRequest.setType(request.getType());
        stockCheckRequest.setNote(request.getNote());
        stockCheckRequest.setStatus(RequestStatus.NOT_STARTED);
        //validate date
        validateForTimeDate(request.getStartDate());
        stockCheckRequest.setStartDate(request.getStartDate());
        validateForTimeDate(request.getExpectedCompletedDate());
        stockCheckRequest.setExpectedCompletedDate(request.getExpectedCompletedDate());
        validateForTimeDate(request.getCountingDate());
        stockCheckRequest.setCountingDate(request.getCountingDate());
        if(request.getCountingDate().equals(LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh")))) {
            stockCheckRequest.setStatus(RequestStatus.IN_PROGRESS);
        }
        stockCheckRequest.setCountingTime(request.getCountingTime());
        stockCheckRequest.setCreatedDate(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));
        stockCheckRequest.setUpdatedDate(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));
        stockCheckRequest.setStockCheckRequestDetails(new ArrayList<>());
        return mapToResponse(stockCheckRequestRepository.save(stockCheckRequest));
    }

    @Transactional
    public StockCheckRequestResponse assignStaffToStockCheck(AssignStaffStockCheck request) {
        LOGGER.info("Assigning staff to stock check request with data: {}", request);
        StockCheckRequest stockCheckRequest = stockCheckRequestRepository.findById(request.getStockCheckId())
                .orElseThrow(() -> new NoSuchElementException("Stock check request not found with ID: " + request.getStockCheckId()));

        if(stockCheckRequest.getAssignedStaff() != null) {
            LOGGER.info("Return working for pre staff: {}",stockCheckRequest.getAssignedStaff().getEmail());
            StaffPerformance staffPerformance = staffPerformanceRepository.
                    findByStockCheckRequestIdAndAssignedStaff_Id(stockCheckRequest.getId(),stockCheckRequest.getAssignedStaff().getId());
            if(staffPerformance != null) {
                LOGGER.info("Delete working time for pre staff: {}",stockCheckRequest.getAssignedStaff().getEmail());
                staffPerformanceRepository.delete(staffPerformance);
            }
        }

        Account account = accountRepository.findById(request.getStaffId())
                .orElseThrow(() -> new NoSuchElementException("Staff not found with ID: " + request.getStaffId()));
        validateAccountForAssignment(account);
        validateForTimeDate(stockCheckRequest.getExpectedCompletedDate());
        setTimeForCountingStaffPerformance(account, stockCheckRequest);
        stockCheckRequest.setAssignedStaff(account);
        stockCheckRequest.setUpdatedDate(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));
        stockCheckRequest.setStatus(RequestStatus.IN_PROGRESS);
        return mapToResponse(stockCheckRequestRepository.save(stockCheckRequest));
    }

    public StockCheckRequestResponse confirmCountedStockCheck(String stockCheckId) {
        LOGGER.info("Confirming counted stock check request with ID: {}", stockCheckId);
        StockCheckRequest stockCheckRequest = stockCheckRequestRepository.findById(stockCheckId)
                .orElseThrow(() -> new NoSuchElementException("Stock check request not found with ID: " + stockCheckId));

        if (stockCheckRequest.getStatus() != RequestStatus.IN_PROGRESS) {
            throw new IllegalStateException("Cannot confirm counted stock check request: Request is not in progress");
        }

        stockCheckRequest.setStatus(RequestStatus.COUNT_CONFIRMED);
        stockCheckRequest.setUpdatedDate(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));
        return mapToResponse(stockCheckRequestRepository.save(stockCheckRequest));
    }

    public StockCheckRequestResponse updateStatus(String stockCheckId, RequestStatus status) {
        LOGGER.info("Updating status of stock check request with ID: {}", stockCheckId);
        StockCheckRequest stockCheckRequest = stockCheckRequestRepository.findById(stockCheckId)
                .orElseThrow(() -> new NoSuchElementException("Stock check request not found with ID: " + stockCheckId));

        if (status == RequestStatus.COMPLETED && stockCheckRequest.getStatus() != RequestStatus.COUNTED) {
            throw new IllegalStateException("Cannot complete stock check request: Request is not counted");
        }

        stockCheckRequest.setStatus(status);
        stockCheckRequest.setUpdatedDate(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));
        return mapToResponse(stockCheckRequestRepository.save(stockCheckRequest));
    }

    @Transactional
    public StockCheckRequestResponse completeStockCheck(String stockCheckId) {
        //check status
        LOGGER.info("Completing stock check request with ID: {}", stockCheckId);
        StockCheckRequest stockCheck = stockCheckRequestRepository.findById(stockCheckId)
                .orElseThrow(() -> new NoSuchElementException("Stock check request not found with ID: " + stockCheckId));
        if(!stockCheck.getStatus().equals(RequestStatus.CONFIRMED)) {
            throw new IllegalStateException("Cannot complete stock check request: Request is not confirmed");
        }
        //check different between checkedList and rq
        List<InventoryItem> inventoryItems = new ArrayList<>();
        List<InventoryItem> needLiquidateItems = new ArrayList<>();
       for(StockCheckRequestDetail detail : stockCheck.getStockCheckRequestDetails()) {
           List<String> requestCheck = detail.getInventoryItemsId();
           List<String> checkedCheck = detail.getCheckedInventoryItemsId();

           requestCheck.removeAll(checkedCheck);
           if(!requestCheck.isEmpty()) {
               for(String inventoryId : requestCheck) {
                   LOGGER.info("Item with ID {} is not checked in stock check request", inventoryId);
                   InventoryItem inventoryItem = inventoryItemRepository.findById(inventoryId)
                           .orElseThrow(() -> new NoSuchElementException("Inventory item not found with ID: " + inventoryId));
                   if(inventoryItem.getStatus().equals(ItemStatus.AVAILABLE)) {
                       inventoryItems.add(inventoryItem);
                   }
               }
           }
           for(String inventoryId : checkedCheck) {
               LOGGER.info("Item with ID {} is checked in stock check request", inventoryId);
               InventoryItem inventoryItem = inventoryItemRepository.findById(inventoryId)
                       .orElseThrow(() -> new NoSuchElementException("Inventory item not found with ID: " + inventoryId));
               if(inventoryItem.getStatus().equals(ItemStatus.NEED_LIQUID)) {
                   needLiquidateItems.add(inventoryItem);
               }
           }
       }

       for(InventoryItem inventoryItem : inventoryItems) {
           LOGGER.info("Updating inventory item {} to not counted", inventoryItem.getId());
           inventoryItem.setStatus(ItemStatus.UNAVAILABLE);
           inventoryItem.setNote("Không thể tìm thấy khi kiểm đếm");
           inventoryItem.setStoredLocation(null);
           inventoryItemRepository.save(inventoryItem);
           Item item = inventoryItem.getItem();
           item.setQuantity(item.getQuantity() - 1);
           item.setMeasurementValue(item.getMeasurementValue() - inventoryItem.getMeasurementValue());
           itemRepository.save(item);
       }

       for(InventoryItem inventoryItem : needLiquidateItems) {
           LOGGER.info("Updating inventory item {} to need liquidate", inventoryItem.getId());
              inventoryItem.setStatus(ItemStatus.NEED_LIQUID);
              inventoryItem.setNote("Cần thanh lý");
              inventoryItem.setNeedToLiquidate(true);
              inventoryItemRepository.save(inventoryItem);
              Item item = inventoryItem.getItem();
              item.setQuantity(item.getQuantity() - 1);
              item.setMeasurementValue(item.getMeasurementValue() - inventoryItem.getMeasurementValue());
              itemRepository.save(item);
       }

        stockCheck.setStatus(RequestStatus.COMPLETED);
        stockCheck.setUpdatedDate(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));
        return mapToResponse(stockCheckRequestRepository.save(stockCheck));
    }

    private void setTimeForCountingStaffPerformance(Account account, StockCheckRequest request) {
        int totalMinutes = 0;
        for (StockCheckRequestDetail detail : request.getStockCheckRequestDetails()) {
            LOGGER.info("Calculating expected working time for item " );
            totalMinutes += detail.getQuantity() * detail.getItem().getCountingMinutes();
        }
        LocalTime expectedWorkingTime = LocalTime.of(0, 0).plusMinutes(totalMinutes);
        StaffPerformance staffPerformance = new StaffPerformance();
        staffPerformance.setExpectedWorkingTime(expectedWorkingTime);
        staffPerformance.setDate(request.getCountingDate());
        staffPerformance.setStockCheckRequestId(request.getId());
        staffPerformance.setAssignedStaff(account);
        staffPerformance.setExportCounting(true);
        staffPerformanceRepository.save(staffPerformance);
        LOGGER.info("Expected working time for counting staff: " + expectedWorkingTime);
    }

    private void validateForTimeDate(LocalDate date) {
        LOGGER.info("Check if date is in the past");
        if (date.isBefore(LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh")))) {
            throw new IllegalStateException("Cannot set time for  export request: Date is in the past");
        }
    }

    private void validateAccountForAssignment(Account account) {
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new IllegalStateException("Cannot assign staff: Account is not active");
        }

        if (account.getRole() != AccountRole.STAFF) {
            throw new IllegalStateException("Cannot assign staff: Account is not a staff member");
        }
    }

    private String createStockCheckID() {
        String prefix = "PK";
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        LOGGER.info("Creating stock check ID for date: {}", today);
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);
        LOGGER.info("Calculating count of stock check requests created today between {} and {}", startOfDay, endOfDay);
        List<StockCheckRequest> existingRequests = stockCheckRequestRepository.findByCreatedDateBetween(startOfDay,endOfDay);
        int todayCount = existingRequests.size();
        LOGGER.info("Count of stock check requests created today: {}", todayCount);
        String datePart = today.format(DateTimeFormatter.BASIC_ISO_DATE);
        String sequence = String.format("%03d", todayCount + 1);

        return String.format("%s-%s-%s", prefix, datePart, sequence);
    }
    private StockCheckRequestResponse mapToResponse(StockCheckRequest request) {
        return new StockCheckRequestResponse(
                request.getId(),
                request.getStockCheckReason(),
                request.getStatus(),
                request.getType(),
                request.getStartDate(),
                request.getExpectedCompletedDate(),
                request.getCountingDate(),
                request.getCountingTime(),
                request.getNote(),
                request.getAssignedStaff() != null ? request.getAssignedStaff().getId() : null,
                request.getStockCheckRequestDetails().isEmpty()
                        ? List.of()
                        : request.getStockCheckRequestDetails().stream()
                        .map(StockCheckRequestDetail::getId)
                        .toList(),
                request.getPaper() != null ? request.getPaper().getId() : null,
                request.getCreatedDate(),
                request.getUpdatedDate(),
                request.getCreatedBy() != null ? request.getCreatedBy() : null,
                request.getUpdatedBy() != null ? request.getUpdatedBy() : null

        );
    }
}
