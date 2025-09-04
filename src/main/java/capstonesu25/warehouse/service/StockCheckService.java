package capstonesu25.warehouse.service;

import capstonesu25.warehouse.annotation.transactionLog.TransactionLoggable;
import capstonesu25.warehouse.entity.*;
import capstonesu25.warehouse.enums.*;
import capstonesu25.warehouse.model.exportrequest.exportrequestdetail.ExportRequestDetailRequest;
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
    private final StaffPerformanceRepository staffPerformanceRepository;
    private final AccountRepository accountRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final ItemRepository itemRepository;
    private final StockCheckRequestDetailRepository stockCheckRequestDetailRepository;
    private final NotificationService notificationService;
    private final ExportRequestRepository exportRequestRepository;
    private final ExportRequestDetailRepository exportRequestDetailRepository;

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
    @TransactionLoggable(type = "STOCK_CHECK", action = "CREATE", objectIdSource = "stockCheckId")
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
    @TransactionLoggable(type = "STOCK_CHECK", action = "ASSIGN_STAFF", objectIdSource = "stockCheckId")
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

    @TransactionLoggable(type = "STOCK_CHECK", action = "CONFIRM_COUNTED", objectIdSource = "stockCheckId")
    public StockCheckRequestResponse confirmCountedStockCheck(String stockCheckId) {
        LOGGER.info("Confirming counted stock check request with ID: {}", stockCheckId);
        StockCheckRequest stockCheckRequest = stockCheckRequestRepository.findById(stockCheckId)
                .orElseThrow(() -> new NoSuchElementException("Stock check request not found with ID: " + stockCheckId));

        stockCheckRequest.setStatus(RequestStatus.COUNT_CONFIRMED);
        stockCheckRequest.setUpdatedDate(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));
        return mapToResponse(stockCheckRequestRepository.save(stockCheckRequest));
    }

    @TransactionLoggable(type = "STOCK_CHECK", action = "UPDATE_STATUS", objectIdSource = "stockCheckId")
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
    @TransactionLoggable(type = "STOCK_CHECK", action = "COMPLETE", objectIdSource = "stockCheckId")
    public List<StockCheckRequestResponse> completeStockCheck(CompleteStockCheckRequest request) {
        // -------- Validate input --------
        if (request == null || request.getStockCheckRequestDetailIds() == null || request.getStockCheckRequestDetailIds().isEmpty()) {
            throw new IllegalArgumentException("stockCheckRequestDetailIds must not be empty");
        }
        LOGGER.info("Completing stock check for details: {}", request.getStockCheckRequestDetailIds());

        // -------- Load & validate details --------
        List<StockCheckRequestDetail> details =
                stockCheckRequestDetailRepository.findAllById(request.getStockCheckRequestDetailIds());

        if (details.size() != request.getStockCheckRequestDetailIds().size()) {
            throw new NoSuchElementException("Some StockCheckRequestDetail IDs were not found");
        }

        // Nhóm theo StockCheckRequest
        Map<StockCheckRequest, List<StockCheckRequestDetail>> detailsByRequest =
                details.stream().collect(Collectors.groupingBy(StockCheckRequestDetail::getStockCheckRequest));

        List<StockCheckRequestResponse> responses = new ArrayList<>();

        // -------- Xử lý theo từng StockCheckRequest --------
        for (Map.Entry<StockCheckRequest, List<StockCheckRequestDetail>> entry : detailsByRequest.entrySet()) {
            StockCheckRequest stockCheck = entry.getKey();
            List<StockCheckRequestDetail> selectedDetails = entry.getValue();
            String stockCheckId = stockCheck.getId();

            LOGGER.info("Processing stock check {} for {} selected details", stockCheckId, selectedDetails.size());

            // Hai danh sách tổng hợp cho request hiện tại
            List<String> unavailableInventoryItemIds = new ArrayList<>();
            List<String> needLiquidateInventoryItemIds = new ArrayList<>();

            // -------- Step 1: Process only selected details --------
            for (StockCheckRequestDetail detail : selectedDetails) {
                // đánh dấu đã kiểm
                detail.setIsChecked(Boolean.TRUE);

                // danh sách item cần có theo kế hoạch (plan)
                List<String> needIds = Optional.ofNullable(detail.getInventoryItemsId()).orElse(List.of());

                // danh sách item thực tế được check (có thể có ghi chú, status, mv)
                List<CheckedStockCheck> checkedObjs = Optional.ofNullable(detail.getCheckedInventoryItems()).orElse(List.of());

                // gom các checked theo inventoryId
                Map<String, List<CheckedStockCheck>> checkedById = checkedObjs.stream()
                        .filter(c -> c.getInventoryItemId() != null)
                        .collect(Collectors.groupingBy(CheckedStockCheck::getInventoryItemId));

                // preload inventory items (need ∪ checked)
                Set<String> idsToLoad = new HashSet<>(needIds);
                idsToLoad.addAll(checkedById.keySet());

                Map<String, InventoryItem> itemById = inventoryItemRepository.findAllById(idsToLoad).stream()
                        .collect(Collectors.toMap(InventoryItem::getId, Function.identity()));

                // cộng dồn actualQuantity & actualMeasurementValue theo những cái được check
                int actualQty = Optional.ofNullable(detail.getActualQuantity()).orElse(0);
                double actualMv = Optional.ofNullable(detail.getActualMeasurementValue()).orElse(0.0);

                // --- 1a) Áp các thay đổi từ CheckedStockCheck vào InventoryItem ---
                for (Map.Entry<String, List<CheckedStockCheck>> e : checkedById.entrySet()) {
                    String inventoryId = e.getKey();
                    InventoryItem inv = getOrThrow(itemById, inventoryId);
                    Item masterItem = inv.getItem();

                    for (CheckedStockCheck c : e.getValue()) {
                        // mv lấy từ checked nếu > 0, ngược lại fallback về mv của inventory hiện tại
                        double mv = (c.getMeasurementValue() != null && Double.compare(c.getMeasurementValue(), 0.0) > 0)
                                ? c.getMeasurementValue()
                                : Optional.ofNullable(inv.getMeasurementValue()).orElse(0.0);

                        // cộng dồn thực tế
                        actualQty += 1;
                        actualMv += mv;

                        // cập nhật ghi chú / trạng thái theo checked
                        if (c.getNote() != null) {
                            inv.setNote(c.getNote());
                        }
                        if (c.getStatus() != null) {
                            inv.setStatus(c.getStatus());
                        }

                        // nếu chuyển NEED_LIQUID -> set cờ, lưu lại và trừ Master Inventory
                        if (ItemStatus.NEED_LIQUID.equals(inv.getStatus())) {
                            inv.setNote("Cần thanh lý");
                            inv.setNeedToLiquidate(true);
                            inventoryItemRepository.save(inv);

                            needLiquidateInventoryItemIds.add(inv.getId());

                            decrementMasterInventory(masterItem, inv.getMeasurementValue());
                        } else {
                            // không phải NEED_LIQUID thì chỉ cần lưu lại thay đổi
                            inventoryItemRepository.save(inv);
                        }
                    }
                }

                // ghi nhận lại số liệu thực tế
                detail.setActualQuantity(actualQty);
                detail.setActualMeasurementValue(actualMv);

                // --- 1b) Tính các ID thiếu (need - checked theo multiset) ---
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

                // --- 1c) Gắn UNAVAILABLE cho các ID thiếu + clear location + trừ tồn ---
                for (String inventoryId : missingIds) {
                    LOGGER.info("Item {} NOT checked in stock check {}", inventoryId, stockCheckId);

                    InventoryItem inv = getOrThrow(itemById, inventoryId);
                    inv.setStatus(ItemStatus.UNAVAILABLE);
                    inv.setNote("Không thể tìm thấy khi kiểm đếm");
                    inv.setStoredLocation(null);
                    inventoryItemRepository.save(inv);

                    unavailableInventoryItemIds.add(inventoryId);

                    decrementMasterInventory(inv.getItem(), inv.getMeasurementValue());
                }

                // --- 1d) Cập nhật trạng thái detail dựa trên số lượng khớp ---
                int planCount = needIds.size();
                detail.setStatus(computeDetailStatus(planCount, matchedCount, checkedById.isEmpty()));
                stockCheckRequestDetailRepository.save(detail);
            }

            // -------- Step 2: Finalize StockCheckRequest --------
            stockCheck.setUpdatedDate(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));
            stockCheck.setStatus(RequestStatus.COMPLETED);
            stockCheckRequestRepository.save(stockCheck);

            LOGGER.info("Sending notification for COMPLETED status");
            notificationService.handleNotification(
                    NotificationUtil.MANAGER_CHANNEL,
                    NotificationUtil.STOCK_CHECK_COMPLETED_EVENT + "-" + stockCheck.getId(),
                    stockCheck.getId(),
                    "Đơn kiểm kê mã #" + stockCheck.getId() + " đã hoàn thành",
                    accountRepository.findByRole(AccountRole.MANAGER)
            );

            // -------- Step 3: Map response (bổ sung 2 list) --------
            StockCheckRequestResponse resp = mapToResponse(stockCheck);
            // Giả sử DTO có 2 setter dưới; nếu chưa có, thêm vào DTO:
            //   List<String> unavailableInventoryItemIds;
            //   List<String> needLiquidateInventoryItemIds;
            createExportForUnavailableItems(unavailableInventoryItemIds.stream().distinct().toList(), stockCheck);
            createExportForNeedToLiquidItems(needLiquidateInventoryItemIds.stream().distinct().toList(),stockCheck);

            responses.add(resp);
        }

        return responses;
    }

    private void createExportForUnavailableItems(List<String> unAvailableInvItemIds, StockCheckRequest stockCheckRequest) {
        List<InventoryItem> unavailableItems =
                inventoryItemRepository.findAllById(unAvailableInvItemIds);

        Map<Item, List<InventoryItem>> unavailableGroupedByItem = unavailableItems.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(InventoryItem::getItem));

        Map<Item, List<String>> unavailableIdsByItem = unavailableItems.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(
                        InventoryItem::getItem,
                        Collectors.mapping(InventoryItem::getId, Collectors.toList())
                ));

        ExportRequest newExportRequest = new ExportRequest();
        String id = createExportRequestId();
        newExportRequest.setId(id);
        newExportRequest.setExportReason("Xuất thanh lý do kiểm kê");
        newExportRequest.setReceiverName("Phòng thanh lý");
        newExportRequest.setReceiverAddress("Phòng thanh lý");
        newExportRequest.setType(ExportType.LIQUIDATION);
        newExportRequest.setCountingDate(LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh")));
        newExportRequest.setCountingTime(LocalTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));
        newExportRequest.setExportDate(LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh")));
        newExportRequest.setNote("Tự động tạo do kiểm kê");
        newExportRequest.setStatus(RequestStatus.COMPLETED);
        newExportRequest.setAssignedStaff(stockCheckRequest.getAssignedStaff());
        newExportRequest = exportRequestRepository.save(newExportRequest);

        for (Map.Entry<Item, List<InventoryItem>> entry : unavailableGroupedByItem.entrySet()) {
            Item item = entry.getKey();
            List<InventoryItem> invItems = entry.getValue();

            ExportRequestDetail detail = new ExportRequestDetail();
            detail.setExportRequest(newExportRequest);
            detail.setItem(item);
            detail.setQuantity(invItems.size());
            detail.setMeasurementValue(
                    invItems.stream()
                            .mapToDouble(inv -> Optional.ofNullable(inv.getMeasurementValue()).orElse(0.0))
                            .sum()
            );
            detail.setStatus(DetailStatus.MATCH);

            detail.setInventoryItems(invItems);

            exportRequestDetailRepository.save(detail);

            for (InventoryItem inv : invItems) {
                inv.setExportRequestDetail(detail);
                inventoryItemRepository.save(inv);
            }
        }
    }


    private void createExportForNeedToLiquidItems(List<String> needToLiquidInvItemIds, StockCheckRequest stockCheckRequest) {
        List<InventoryItem> needToLiquidInvItems =
                inventoryItemRepository.findAllById(needToLiquidInvItemIds);

        Map<Item, List<InventoryItem>> needLiquidGroupedByItem = needToLiquidInvItems.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(InventoryItem::getItem));

        Map<Item, List<String>> unavailableIdsByItem = needToLiquidInvItems.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(
                        InventoryItem::getItem,
                        Collectors.mapping(InventoryItem::getId, Collectors.toList())
                ));

        ExportRequest newExportRequest = new ExportRequest();
        String id = createExportRequestId();
        newExportRequest.setId(id);
        newExportRequest.setExportReason("Xuất thanh lý do kiểm kê");
        newExportRequest.setReceiverName("Phòng thanh lý");
        newExportRequest.setReceiverAddress("Phòng thanh lý");
        newExportRequest.setType(ExportType.LIQUIDATION);
        newExportRequest.setCountingDate(LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh")));
        newExportRequest.setCountingTime(LocalTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));
        newExportRequest.setExportDate(LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh")));
        newExportRequest.setNote("Tự động tạo do kiểm kê");
        newExportRequest.setStatus(RequestStatus.WAITING_EXPORT);
        newExportRequest.setAssignedStaff(stockCheckRequest.getAssignedStaff());
        newExportRequest = exportRequestRepository.save(newExportRequest);

        for (Map.Entry<Item, List<InventoryItem>> entry : needLiquidGroupedByItem.entrySet()) {
            Item item = entry.getKey();
            List<InventoryItem> invItems = entry.getValue();

            ExportRequestDetail detail = new ExportRequestDetail();
            detail.setExportRequest(newExportRequest);
            detail.setItem(item);
            detail.setQuantity(invItems.size());
            detail.setMeasurementValue(
                    invItems.stream()
                            .mapToDouble(inv -> Optional.ofNullable(inv.getMeasurementValue()).orElse(0.0))
                            .sum()
            );
            detail.setStatus(DetailStatus.MATCH);

            detail.setInventoryItems(invItems);

            exportRequestDetailRepository.save(detail);

            for (InventoryItem inv : invItems) {
                inv.setExportRequestDetail(detail);
                inventoryItemRepository.save(inv);
            }
        }
    }
    private String createExportRequestId() {
        String prefix = "PX";
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        String datePart = today.format(DateTimeFormatter.BASIC_ISO_DATE);

        String todayPrefix = prefix + "-" + datePart + "-";
        List<ExportRequest> existingRequests = exportRequestRepository.findByIdStartingWith(todayPrefix);
        int todayCount = existingRequests.size();

        String sequence = String.format("%03d", todayCount + 1);

        return String.format("%s-%s-%s", prefix, datePart, sequence);
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
