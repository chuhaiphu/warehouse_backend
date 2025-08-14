package capstonesu25.warehouse.service;

import capstonesu25.warehouse.entity.*;
import capstonesu25.warehouse.enums.*;
import capstonesu25.warehouse.model.stockcheck.AssignStaffStockCheck;
import capstonesu25.warehouse.model.stockcheck.CompleteStockCheckRequest;
import capstonesu25.warehouse.model.stockcheck.StockCheckRequestRequest;
import capstonesu25.warehouse.model.stockcheck.StockCheckRequestResponse;
import capstonesu25.warehouse.repository.*;
import capstonesu25.warehouse.utils.NotificationUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StockCheckService {
    private final StockCheckRequestRepository stockCheckRequestRepository;
    private final ConfigurationRepository configurationRepository;
    private final StaffPerformanceRepository staffPerformanceRepository;
    private final AccountRepository accountRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final ItemRepository itemRepository;
    private final StockCheckRequestDetailRepository stockCheckRequestDetailRepository;
    private final NotificationService notificationService;

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
        if(request.getStartDate().equals(LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh")))) {
            stockCheckRequest.setStatus(RequestStatus.IN_PROGRESS);
        }
        stockCheckRequest.setCountingTime(request.getCountingTime());
        stockCheckRequest.setCreatedDate(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));
        stockCheckRequest.setUpdatedDate(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));
        stockCheckRequest.setStockCheckRequestDetails(new ArrayList<>());
        
        StockCheckRequest savedStockCheck = stockCheckRequestRepository.save(stockCheckRequest);
        
        notificationService.handleNotification(
            NotificationUtil.WAREHOUSE_MANAGER_CHANNEL,
            NotificationUtil.STOCK_CHECK_CREATED_EVENT,
            savedStockCheck.getId(),
            "Đơn kiểm kê mã #" + savedStockCheck.getId() + " đã được tạo",
            accountRepository.findByRole(AccountRole.WAREHOUSE_MANAGER)
        );
        
        notificationService.handleNotification(
            NotificationUtil.MANAGER_CHANNEL,
            NotificationUtil.STOCK_CHECK_CREATED_EVENT,
            savedStockCheck.getId(),
            "Đơn kiểm kê mã #" + savedStockCheck.getId() + " đã được tạo",
            accountRepository.findByRole(AccountRole.MANAGER)
        );
        
        return mapToResponse(savedStockCheck);
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
        
        StockCheckRequest savedStockCheck = stockCheckRequestRepository.save(stockCheckRequest);
        
        notificationService.handleNotification(
            NotificationUtil.STAFF_CHANNEL + account.getId(),
            NotificationUtil.STOCK_CHECK_ASSIGNED_EVENT,
            savedStockCheck.getId(),
            "Bạn được phân công cho đơn kiểm kê mã #" + savedStockCheck.getId(),
            List.of(account)
        );
        
        return mapToResponse(savedStockCheck);
    }

    public StockCheckRequestResponse confirmCountedStockCheck(String stockCheckId) {
        LOGGER.info("Confirming counted stock check request with ID: {}", stockCheckId);
        StockCheckRequest stockCheckRequest = stockCheckRequestRepository.findById(stockCheckId)
                .orElseThrow(() -> new NoSuchElementException("Stock check request not found with ID: " + stockCheckId));

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
        
        StockCheckRequest savedStockCheck = stockCheckRequestRepository.save(stockCheckRequest);
        
        if(status == RequestStatus.COUNTED) {
            LOGGER.info("Sending notification for COUNTED status");
            notificationService.handleNotification(
                NotificationUtil.WAREHOUSE_MANAGER_CHANNEL,
                NotificationUtil.STOCK_CHECK_COUNTED_EVENT,
                savedStockCheck.getId(),
                "Đơn kiểm kê mã #" + savedStockCheck.getId() + " đã được kiểm đếm",
                accountRepository.findByRole(AccountRole.WAREHOUSE_MANAGER)
            );
        }
        
        if(status == RequestStatus.COUNT_CONFIRMED) {
            LOGGER.info("Sending notification for COUNT_CONFIRMED status");
            notificationService.handleNotification(
                NotificationUtil.DEPARTMENT_CHANNEL,
                NotificationUtil.STOCK_CHECK_CONFIRMED_EVENT,
                savedStockCheck.getId(),
                "Đơn kiểm kê mã #" + savedStockCheck.getId() + " đã được xác nhận kiểm đếm",
                accountRepository.findByRole(AccountRole.DEPARTMENT)
            );
            
            notificationService.handleNotification(
                NotificationUtil.MANAGER_CHANNEL,
                NotificationUtil.STOCK_CHECK_CONFIRMED_EVENT,
                savedStockCheck.getId(),
                "Đơn kiểm kê mã #" + savedStockCheck.getId() + " đã được xác nhận kiểm đếm",
                accountRepository.findByRole(AccountRole.MANAGER)
            );
        }
        
        return mapToResponse(savedStockCheck);
    }

    @Transactional
    public List<StockCheckRequestResponse> completeStockCheck(CompleteStockCheckRequest request) {
        LOGGER.info("Completing stock check for details: {}", request.getStockCheckRequestDetailIds());
        if (request == null || request.getStockCheckRequestDetailIds() == null || request.getStockCheckRequestDetailIds().isEmpty()) {
            throw new IllegalArgumentException("stockCheckRequestDetailIds must not be empty");
        }

        // Load details
        List<StockCheckRequestDetail> details = stockCheckRequestDetailRepository
                .findAllById(request.getStockCheckRequestDetailIds());

        if (details.size() != request.getStockCheckRequestDetailIds().size()) {
            throw new NoSuchElementException("Some StockCheckRequestDetail IDs were not found");
        }

        // Group selected details by their parent StockCheckRequest
        Map<StockCheckRequest, List<StockCheckRequestDetail>> detailsByRequest = details.stream()
                .collect(Collectors.groupingBy(StockCheckRequestDetail::getStockCheckRequest));

        List<StockCheckRequestResponse> responses = new ArrayList<>();

        for (Map.Entry<StockCheckRequest, List<StockCheckRequestDetail>> entry : detailsByRequest.entrySet()) {
            StockCheckRequest stockCheck = entry.getKey();
            List<StockCheckRequestDetail> selectedDetails = entry.getValue();

            String stockCheckId = stockCheck.getId();
            LOGGER.info("Processing stock check {} for {} selected details", stockCheckId, selectedDetails.size());

            // --- Step 2: Process ONLY the selected details ---
            for (StockCheckRequestDetail detail : selectedDetails) {
                List<String> requestCheck = new ArrayList<>(detail.getInventoryItemsId()); // copy to avoid mutating entity list
                List<String> checkedCheck = detail.getCheckedInventoryItemsId() != null
                        ? detail.getCheckedInventoryItemsId()
                        : Collections.emptyList();

                // Missing (not checked) items
                detail.setIsChecked(Boolean.TRUE);
                requestCheck.removeAll(checkedCheck);
                for (String inventoryId : requestCheck) {
                    LOGGER.info("Item {} NOT checked in stock check {}", inventoryId, stockCheckId);
                    InventoryItem inventoryItem = inventoryItemRepository.findById(inventoryId)
                            .orElseThrow(() -> new NoSuchElementException("Inventory item not found with ID: " + inventoryId));

                    inventoryItem.setStatus(ItemStatus.UNAVAILABLE);
                    inventoryItem.setNote("Không thể tìm thấy khi kiểm đếm");
                    inventoryItem.setStoredLocation(null);
                    inventoryItemRepository.save(inventoryItem);

                    Item item = inventoryItem.getItem();
                    item.setQuantity(item.getQuantity() - 1);
                    item.setTotalMeasurementValue(item.getTotalMeasurementValue() - inventoryItem.getMeasurementValue());
                    itemRepository.save(item);
                }

                // Checked items: mark for liquidation if needed
                for (String inventoryId : checkedCheck) {
                    LOGGER.info("Item {} CHECKED in stock check {}", inventoryId, stockCheckId);
                    InventoryItem inventoryItem = inventoryItemRepository.findById(inventoryId)
                            .orElseThrow(() -> new NoSuchElementException("Inventory item not found with ID: " + inventoryId));

                    if (ItemStatus.NEED_LIQUID.equals(inventoryItem.getStatus())) {
                        inventoryItem.setNote("Cần thanh lý");
                        inventoryItem.setNeedToLiquidate(true);
                        inventoryItemRepository.save(inventoryItem);

                        Item item = inventoryItem.getItem();
                        item.setQuantity(item.getQuantity() - 1);
                        item.setTotalMeasurementValue(item.getTotalMeasurementValue() - inventoryItem.getMeasurementValue());
                        itemRepository.save(item);
                    }
                }
            }

            // --- Step 3: Finalize stock check (only if ALL details of this request are included) ---
            Set<Long> allDetailIdsOfRequest = stockCheck.getStockCheckRequestDetails()
                    .stream().map(StockCheckRequestDetail::getId).collect(Collectors.toSet());
            Set<Long> selectedDetailIds = selectedDetails
                    .stream().map(StockCheckRequestDetail::getId).collect(Collectors.toSet());

            boolean allDetailsIncluded = selectedDetailIds.containsAll(allDetailIdsOfRequest);

            if (allDetailsIncluded) {
                stockCheck.setStatus(RequestStatus.COMPLETED);
                stockCheck.setExpectedCompletedDate(LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh")));
                
                LOGGER.info("Sending notification for COMPLETED status");
                notificationService.handleNotification(
                    NotificationUtil.MANAGER_CHANNEL,
                    NotificationUtil.STOCK_CHECK_COMPLETED_EVENT,
                    stockCheck.getId(),
                    "Đơn kiểm kê mã #" + stockCheck.getId() + " đã hoàn thành",
                    accountRepository.findByRole(AccountRole.MANAGER)
                );
            } else {
                // leave status as-is; optionally set IN_PROGRESS if you have that state
                // stockCheck.setStatus(RequestStatus.IN_PROGRESS);
                LOGGER.info("Stock check {} not fully covered by payload; leaving status as {}", stockCheckId, stockCheck.getStatus());
            }
            stockCheck.setUpdatedDate(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));

            stockCheckRequestRepository.save(stockCheck);
            for(StockCheckRequestDetail detail : stockCheck.getStockCheckRequestDetails()) {
                if(detail.getCheckedInventoryItemsId() == null && detail.getInventoryItemsId() == null) {
                    detail.setStatus(DetailStatus.NOT_CHECK);
                }
                if(detail.getCheckedInventoryItemsId().isEmpty() && detail.getInventoryItemsId().isEmpty()) {
                    detail.setStatus(DetailStatus.NOT_CHECK);
                }
                if(detail.getCheckedInventoryItemsId().size() == detail.getInventoryItemsId().size()) {
                    detail.setStatus(DetailStatus.MATCH);
                }
                if(detail.getCheckedInventoryItemsId().size() < detail.getInventoryItemsId().size()) {
                    detail.setStatus(DetailStatus.LACK);
                }
                stockCheckRequestDetailRepository.save(detail);
            }

            responses.add(mapToResponse(stockCheck));
        }

        return responses;
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
        String datePart = today.format(DateTimeFormatter.BASIC_ISO_DATE);
        
        String todayPrefix = prefix + "-" + datePart + "-";
        List<StockCheckRequest> existingRequests = stockCheckRequestRepository.findByIdStartingWith(todayPrefix);
        int todayCount = existingRequests.size();

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
