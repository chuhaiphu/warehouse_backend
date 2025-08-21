package capstonesu25.warehouse.service;

import capstonesu25.warehouse.entity.*;
import capstonesu25.warehouse.enums.*;
import capstonesu25.warehouse.model.stockcheck.AssignStaffStockCheck;
import capstonesu25.warehouse.model.stockcheck.CompleteStockCheckRequest;
import capstonesu25.warehouse.model.stockcheck.StockCheckRequestRequest;
import capstonesu25.warehouse.model.stockcheck.StockCheckRequestResponse;
import capstonesu25.warehouse.model.stockcheck.detail.CheckedStockCheck;
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
import java.util.function.Function;
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

    public List<StockCheckRequestResponse> getStockCheckRequestsByStatus(RequestStatus status) {
        LOGGER.info("Fetching stock check requests by status: {}", status);
        List<StockCheckRequest> stockCheckRequests = stockCheckRequestRepository.findAllByStatus(status);
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
                NotificationUtil.STOCK_CHECK_COUNTED_EVENT + "-" + savedStockCheck.getId(),
                savedStockCheck.getId(),
                "Đơn kiểm kê mã #" + savedStockCheck.getId() + " đã được kiểm đếm",
                accountRepository.findByRole(AccountRole.WAREHOUSE_MANAGER)
            );
        }
        
        if(status == RequestStatus.COUNT_CONFIRMED) {
            LOGGER.info("Sending notification for COUNT_CONFIRMED status");
            notificationService.handleNotification(
                NotificationUtil.DEPARTMENT_CHANNEL,
                NotificationUtil.STOCK_CHECK_CONFIRMED_EVENT + "-" + savedStockCheck.getId(),
                savedStockCheck.getId(),
                "Đơn kiểm kê mã #" + savedStockCheck.getId() + " đã được xác nhận kiểm đếm",
                accountRepository.findByRole(AccountRole.DEPARTMENT)
            );
            
            notificationService.handleNotification(
                NotificationUtil.MANAGER_CHANNEL,
                NotificationUtil.STOCK_CHECK_CONFIRMED_EVENT + "-" + savedStockCheck.getId(),
                savedStockCheck.getId(),
                "Đơn kiểm kê mã #" + savedStockCheck.getId() + " đã được xác nhận kiểm đếm",
                accountRepository.findByRole(AccountRole.MANAGER)
            );
        }
        
        return mapToResponse(savedStockCheck);
    }

    @Transactional
    public List<StockCheckRequestResponse> completeStockCheck(CompleteStockCheckRequest request) {
        if (request == null || request.getStockCheckRequestDetailIds() == null || request.getStockCheckRequestDetailIds().isEmpty()) {
            throw new IllegalArgumentException("stockCheckRequestDetailIds must not be empty");
        }
        LOGGER.info("Completing stock check for details: {}", request.getStockCheckRequestDetailIds());

        // Load & validate details
        List<StockCheckRequestDetail> details = stockCheckRequestDetailRepository.findAllById(request.getStockCheckRequestDetailIds());
        if (details.size() != request.getStockCheckRequestDetailIds().size()) {
            throw new NoSuchElementException("Some StockCheckRequestDetail IDs were not found");
        }

        // Group theo StockCheckRequest
        Map<StockCheckRequest, List<StockCheckRequestDetail>> detailsByRequest =
                details.stream().collect(Collectors.groupingBy(StockCheckRequestDetail::getStockCheckRequest));

        List<StockCheckRequestResponse> responses = new ArrayList<>();

        for (Map.Entry<StockCheckRequest, List<StockCheckRequestDetail>> entry : detailsByRequest.entrySet()) {
            StockCheckRequest stockCheck = entry.getKey();
            List<StockCheckRequestDetail> selectedDetails = entry.getValue();
            String stockCheckId = stockCheck.getId();
            LOGGER.info("Processing stock check {} for {} selected details", stockCheckId, selectedDetails.size());

            // -------- Step 2: Process only selected details --------
            for (StockCheckRequestDetail detail : selectedDetails) {
                detail.setIsChecked(Boolean.TRUE);

                List<String> needIds = Optional.ofNullable(detail.getInventoryItemsId()).orElse(List.of());
                List<CheckedStockCheck> checkedObjs = Optional.ofNullable(detail.getCheckedInventoryItems()).orElse(List.of());

                // Gom theo id để xử lý bội số (nhiều CheckedStockCheck cùng 1 inventoryId)
                Map<String, List<CheckedStockCheck>> checkedById = checkedObjs.stream()
                        .filter(c -> c.getInventoryItemId() != null)
                        .collect(Collectors.groupingBy(CheckedStockCheck::getInventoryItemId));

                // Preload InventoryItem một lần cho tất cả id sẽ đụng tới (need ∪ checked keys)
                Set<String> idsToLoad = new HashSet<>(needIds);
                idsToLoad.addAll(checkedById.keySet());
                Map<String, InventoryItem> itemById = inventoryItemRepository.findAllById(idsToLoad).stream()
                        .collect(Collectors.toMap(InventoryItem::getId, Function.identity()));

                // 1) Cập nhật theo checked: cộng actualQty/actualMv & set status theo CheckedStockCheck
                int actualQty = Optional.ofNullable(detail.getActualQuantity()).orElse(0);
                double actualMv = Optional.ofNullable(detail.getActualMeasurementValue()).orElse(0.0);

                for (Map.Entry<String, List<CheckedStockCheck>> e : checkedById.entrySet()) {
                    String inventoryId = e.getKey();
                    InventoryItem inv = getOrThrow(itemById, inventoryId);
                    Item masterItem = inv.getItem();

                    for (CheckedStockCheck c : e.getValue()) {
                        double mv = (c.getMeasurementValue() != null && Double.compare(c.getMeasurementValue(), 0.0) > 0)
                                ? c.getMeasurementValue()
                                : Optional.ofNullable(inv.getMeasurementValue()).orElse(0.0);

                        actualQty += 1;
                        actualMv += mv;

                        if (c.getStatus() != null) {
                            inv.setStatus(c.getStatus());
                        }

                        if (ItemStatus.NEED_LIQUID.equals(inv.getStatus())) {
                            inv.setNote("Cần thanh lý");
                            inv.setNeedToLiquidate(true);
                            inventoryItemRepository.save(inv);

                            decrementMasterInventory(masterItem, inv.getMeasurementValue());
                        } else {
                            inventoryItemRepository.save(inv);
                        }
                    }
                }

                detail.setActualQuantity(actualQty);
                detail.setActualMeasurementValue(actualMv);

                // 2) Tìm ID thiếu (need - checked) theo multiset
                int matchedCount = 0;
                Map<String, Integer> needFreq = buildFreq(needIds);
                for (Map.Entry<String, List<CheckedStockCheck>> e : checkedById.entrySet()) {
                    String id = e.getKey();
                    int take = Math.min(needFreq.getOrDefault(id, 0), e.getValue().size());
                    if (take > 0) {
                        matchedCount += take;
                        needFreq.put(id, needFreq.get(id) - take);
                    }
                }
                List<String> missingIds = expandRemainder(needFreq);

                // 3) Với ID thiếu → UNAVAILABLE + clear location + trừ tồn
                for (String inventoryId : missingIds) {
                    LOGGER.info("Item {} NOT checked in stock check {}", inventoryId, stockCheckId);
                    InventoryItem inv = getOrThrow(itemById, inventoryId);
                    inv.setStatus(ItemStatus.UNAVAILABLE);
                    inv.setNote("Không thể tìm thấy khi kiểm đếm");
                    inv.setStoredLocation(null);
                    inventoryItemRepository.save(inv);

                    decrementMasterInventory(inv.getItem(), inv.getMeasurementValue());
                }

                // 4) Cập nhật DetailStatus theo số ID khớp (bỏ qua measurement)
                int planCount = needIds.size();
                detail.setStatus(computeDetailStatus(planCount, matchedCount, checkedById.isEmpty()));

                stockCheckRequestDetailRepository.save(detail);
            }

            // -------- Step 3: Finalize request nếu bao phủ hết mọi detail --------
            Set<Long> allDetailIds = stockCheck.getStockCheckRequestDetails().stream()
                    .map(StockCheckRequestDetail::getId).collect(Collectors.toSet());
            Set<Long> selectedIds = selectedDetails.stream()
                    .map(StockCheckRequestDetail::getId).collect(Collectors.toSet());
            boolean allIncluded = selectedIds.containsAll(allDetailIds);

            if (allIncluded) {
                stockCheck.setStatus(RequestStatus.COMPLETED);
                stockCheck.setExpectedCompletedDate(LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh")));
                LOGGER.info("Sending notification for COMPLETED status");
                notificationService.handleNotification(
                        NotificationUtil.MANAGER_CHANNEL,
                        NotificationUtil.STOCK_CHECK_COMPLETED_EVENT + "-" + stockCheck.getId(),
                        stockCheck.getId(),
                        "Đơn kiểm kê mã #" + stockCheck.getId() + " đã hoàn thành",
                        accountRepository.findByRole(AccountRole.MANAGER)
                );
            } else {
                LOGGER.info("Stock check {} not fully covered; keep status {}", stockCheckId, stockCheck.getStatus());
                // stockCheck.setStatus(RequestStatus.IN_PROGRESS); // nếu cần
            }
            stockCheck.setUpdatedDate(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));
            stockCheckRequestRepository.save(stockCheck);

            responses.add(mapToResponse(stockCheck));
        }

        return responses;
    }


    private static Map<String, Integer> buildFreq(List<String> ids) {
        Map<String, Integer> freq = new HashMap<>();
        for (String id : ids) freq.merge(id, 1, Integer::sum);
        return freq;
    }

    private static List<String> expandRemainder(Map<String, Integer> freq) {
        List<String> out = new ArrayList<>();
        for (Map.Entry<String, Integer> e : freq.entrySet()) {
            for (int i = 0; i < e.getValue(); i++) out.add(e.getKey());
        }
        return out;
    }

    private static DetailStatus computeDetailStatus(int planCount, int matchedCount, boolean checkedEmpty) {
        if (planCount == 0 && checkedEmpty) return DetailStatus.NOT_CHECK;
        if (matchedCount == planCount && planCount > 0) return DetailStatus.MATCH;
        if (matchedCount < planCount) return DetailStatus.LACK;
        return DetailStatus.EXCESS; // matched > plan
    }

    private void decrementMasterInventory(Item item, Double mv) {
        double m = Optional.ofNullable(mv).orElse(0.0);
        item.setQuantity(item.getQuantity() - 1);
        item.setTotalMeasurementValue(item.getTotalMeasurementValue() - m);
        itemRepository.save(item);
    }

    private InventoryItem getOrThrow(Map<String, InventoryItem> map, String id) {
        InventoryItem inv = map.get(id);
        if (inv == null) {
            // Phòng khi id nằm trong missing mà không có ở map (do chưa load)
            inv = inventoryItemRepository.findById(id)
                    .orElseThrow(() -> new NoSuchElementException("Inventory item not found with ID: " + id));
            map.put(id, inv);
        }
        return inv;
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
