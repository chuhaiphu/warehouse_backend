package capstonesu25.warehouse.service;

import capstonesu25.warehouse.entity.*;
import capstonesu25.warehouse.enums.DetailStatus;
import capstonesu25.warehouse.enums.ItemStatus;
import capstonesu25.warehouse.model.account.AccountResponse;
import capstonesu25.warehouse.model.account.ActiveAccountRequest;
import capstonesu25.warehouse.model.stockcheck.StockCheckRequestResponse;
import capstonesu25.warehouse.model.stockcheck.detail.CheckedStockCheck;
import capstonesu25.warehouse.model.stockcheck.detail.StockCheckRequestDetailRequest;
import capstonesu25.warehouse.model.stockcheck.detail.StockCheckRequestDetailResponse;
import capstonesu25.warehouse.model.stockcheck.detail.UpdateActualStockCheck;
import capstonesu25.warehouse.repository.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class StockCheckDetailService {
    private final StockCheckRequestRepository stockCheckRequestRepository;
    private final StockCheckRequestDetailRepository stockCheckRequestDetailRepository;
    private final ItemRepository itemRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final AccountService accountService;
    private final AccountRepository accountRepository;
    private final StaffPerformanceRepository staffPerformanceRepository;
    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(StockCheckDetailService.class);


    public List<StockCheckRequestDetailResponse> getAllByStockCheckRequestId(String stockCheckRequestId) {
        StockCheckRequest stockCheckRequest = stockCheckRequestRepository.findById(stockCheckRequestId)
                .orElseThrow(() -> new RuntimeException("Stock check request not found"));
        List<StockCheckRequestDetail> details = stockCheckRequestDetailRepository.findByStockCheckRequest_Id(stockCheckRequestId);
        return details.stream()
                .map(this::mapToResponse)
                .toList();
    }

    public StockCheckRequestDetailResponse getById(Long id) {
        StockCheckRequestDetail detail = stockCheckRequestDetailRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Stock check request detail not found"));
        return mapToResponse(detail);
    }
    @Transactional
    public void create (List<StockCheckRequestDetailRequest> requests, String stockCheckRequestId) {
        StockCheckRequest stockCheckRequest = stockCheckRequestRepository.findById(stockCheckRequestId)
                .orElseThrow(() -> new RuntimeException("Stock check request not found"));
        List<StockCheckRequestDetail> requestDetails = new ArrayList<>();
        for( StockCheckRequestDetailRequest request : requests) {
            StockCheckRequestDetail detail = new StockCheckRequestDetail();
            Item item = itemRepository.findById(request.getItemId())
                    .orElseThrow(() -> new RuntimeException("Item not found"));
            detail.setItem(item);
            detail.setQuantity(request.getQuantity());
            detail.setMeasurementValue(request.getMeasurementValue());
            detail.setStockCheckRequest(stockCheckRequest);
            detail.setActualMeasurementValue(0.0);
            detail.setActualQuantity(0);
            List<InventoryItem> inventoryItems = inventoryItemRepository.findByItem_IdAndStatus(
                    request.getItemId(), ItemStatus.AVAILABLE);
            if(request.getQuantity() == null || request.getQuantity() == 0) {
                detail.setQuantity(inventoryItems.size());
            }
            if(request.getMeasurementValue() == null || request.getMeasurementValue() == 0.0) {
                detail.setMeasurementValue(inventoryItems.stream()
                        .map(InventoryItem::getMeasurementValue)
                        .reduce(0.0, Double::sum));
            }

            detail.setInventoryItemsId(inventoryItems.stream()
                    .map(InventoryItem::getId)
                    .toList());
            requestDetails.add(detail);
        }
        stockCheckRequestDetailRepository.saveAll(requestDetails);

        autoAssignCountingStaff(stockCheckRequest);
    }

    @Transactional
    public StockCheckRequestDetailResponse updateActualQuantity(UpdateActualStockCheck request) {
        LOGGER.info("Updating actual measurement for stockCheckDetailId={}, inventoryItemId={}",
                request.getStockCheckDetailId(), request.getInventoryItemId());

        if (request.getStockCheckDetailId() == null || request.getInventoryItemId() == null) {
            throw new IllegalArgumentException("stockCheckDetailId and inventoryItemId must not be null");
        }

        StockCheckRequestDetail detail = stockCheckRequestDetailRepository.findById(request.getStockCheckDetailId())
                .orElseThrow(() -> new NoSuchElementException("Stock check request detail not found"));

        if (detail.getCheckedInventoryItems() == null) {
            detail.setCheckedInventoryItems(new ArrayList<>());
        }

        boolean alreadyTracked = detail.getCheckedInventoryItems().stream()
                .anyMatch(c -> Objects.equals(c.getInventoryItemId(), request.getInventoryItemId()));
        if (alreadyTracked) {
            throw new IllegalStateException("Inventory item is already being tracked for stock check");
        }

        InventoryItem inventoryItem = inventoryItemRepository.findById(request.getInventoryItemId())
                .orElseThrow(() -> new NoSuchElementException("Inventory item not found"));

        Double mv = request.getActualMeasurementValue();
        if (mv == null || Double.compare(mv, 0.0) <= 0) {
            mv = inventoryItem.getMeasurementValue(); // fallback sang giá trị của item
        }

        ItemStatus status = request.getStatus() != null ? request.getStatus() : ItemStatus.AVAILABLE;

        CheckedStockCheck checked = new CheckedStockCheck();
        checked.setInventoryItemId(request.getInventoryItemId());
        checked.setMeasurementValue(mv);
        checked.setStatus(status);

        detail.getCheckedInventoryItems().add(checked);

        StockCheckRequestDetail saved = stockCheckRequestDetailRepository.save(detail);

        return mapToResponse(saved);
    }


    @Transactional
    public StockCheckRequestDetailResponse resetTrackingForStockCheckRequestDetail(UpdateActualStockCheck request) {
        LOGGER.info("Resetting tracking for stock check detail: detailId={}, inventoryItemId={}",
                request.getStockCheckDetailId(), request.getInventoryItemId());

        if (request.getStockCheckDetailId() == null || request.getInventoryItemId() == null) {
            throw new IllegalArgumentException("stockCheckDetailId and inventoryItemId must not be null");
        }

        StockCheckRequestDetail detail = stockCheckRequestDetailRepository.findById(request.getStockCheckDetailId())
                .orElseThrow(() -> new NoSuchElementException("Stock check request detail not found"));

        if (detail.getCheckedInventoryItems() == null) {
            detail.setCheckedInventoryItems(new ArrayList<>());
        }


        CheckedStockCheck toRemove = detail.getCheckedInventoryItems().stream()
                .filter(c -> Objects.equals(c.getInventoryItemId(), request.getInventoryItemId()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Inventory item is not being tracked for stock check"));

        int actualQty = Optional.ofNullable(detail.getActualQuantity()).orElse(0);
        double actualMv = Optional.ofNullable(detail.getActualMeasurementValue()).orElse(0.0);

        actualQty = Math.max(0, actualQty - 1);
        actualMv = Math.max(0.0, actualMv - Optional.ofNullable(toRemove.getMeasurementValue()).orElse(0.0));

        detail.setActualQuantity(actualQty);
        detail.setActualMeasurementValue(actualMv);

        // 5) Xoá bản ghi theo dõi khỏi danh sách
        detail.getCheckedInventoryItems().remove(toRemove);

        // 6) Tính lại status
        int planQty = Optional.ofNullable(detail.getQuantity()).orElse(0);
        double planMv = Optional.ofNullable(detail.getMeasurementValue()).orElse(0.0);

        // Need-to-check list (planned)
        List<String> needIds = Optional.ofNullable(detail.getInventoryItemsId()).orElse(List.of());

// Checked list (actual)
        List<String> checkedIds = Optional.ofNullable(detail.getCheckedInventoryItems()).orElse(List.of())
                .stream()
                .map(CheckedStockCheck::getInventoryItemId)
                .toList();

        Set<String> needSet = new HashSet<>(needIds);
        long matchedCount = checkedIds.stream().filter(needSet::contains).count();

        int planCount = needIds.size();

        DetailStatus newStatus;
        if (matchedCount == 0) {
            newStatus = null; // chưa có gì kiểm
        } else if (matchedCount > planCount) {
            newStatus = DetailStatus.EXCESS;
        } else if (matchedCount < planCount) {
            newStatus = DetailStatus.LACK;
        } else {
            newStatus = null; // khớp chính xác (nếu bạn có enum MATCHED/OK thì đặt ở đây)
        }
        detail.setStatus(newStatus);

        return mapToResponse(stockCheckRequestDetailRepository.save(detail));
    }


    private void autoAssignCountingStaff(StockCheckRequest stockCheckRequest) {
        ActiveAccountRequest activeAccountRequest = ActiveAccountRequest.builder()
                .date(stockCheckRequest.getCountingDate())
                .stockCheckRequestId(stockCheckRequest.getId())
                .build();

        List<AccountResponse> accountResponse = accountService.getAllActiveStaffsInDate(activeAccountRequest);

        Account account = accountRepository.findById(accountResponse.get(0).getId())
                .orElseThrow(() -> new NoSuchElementException("Account not found with ID: " + accountResponse.get(0).getId()));
        stockCheckRequest.setAssignedStaff(account);
        setTimeForCountingStaffPerformance(account, stockCheckRequest);
        stockCheckRequestRepository.save(stockCheckRequest);

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

    private StockCheckRequestDetailResponse mapToResponse(StockCheckRequestDetail detail) {
        return StockCheckRequestDetailResponse.builder()
                .id(detail.getId())
                .measurementValue(detail.getMeasurementValue())
                .quantity(detail.getQuantity())
                .actualQuantity(detail.getActualQuantity())
                .actualMeasurementValue(detail.getActualMeasurementValue())
                .status(detail.getStatus())
                .isChecked(detail.getIsChecked())
                .stockCheckRequestId(detail.getStockCheckRequest().getId())
                .itemId(detail.getItem().getId())
                .inventoryItemIds(detail.getInventoryItemsId())
                .checkedInventoryItemIds(detail.getCheckedInventoryItems())
                .build();
    }
}
