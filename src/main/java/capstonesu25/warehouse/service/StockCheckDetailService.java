package capstonesu25.warehouse.service;

import capstonesu25.warehouse.entity.*;
import capstonesu25.warehouse.enums.DetailStatus;
import capstonesu25.warehouse.enums.ItemStatus;
import capstonesu25.warehouse.model.account.AccountResponse;
import capstonesu25.warehouse.model.account.ActiveAccountRequest;
import capstonesu25.warehouse.model.stockcheck.StockCheckRequestResponse;
import capstonesu25.warehouse.model.stockcheck.detail.StockCheckRequestDetailRequest;
import capstonesu25.warehouse.model.stockcheck.detail.StockCheckRequestDetailResponse;
import capstonesu25.warehouse.model.stockcheck.detail.UpdateActualStockCheck;
import capstonesu25.warehouse.repository.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

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
            detail.setQuantity(inventoryItems.size());
            detail.setMeasurementValue(inventoryItems.stream()
                    .map(InventoryItem::getMeasurementValue)
                    .reduce(0.0, Double::sum));
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
        LOGGER.info("Updating actual quantity for stock check request detail with ID: {}", request.getStockCheckDetailId());
        StockCheckRequestDetail detail = stockCheckRequestDetailRepository.findById(request.getStockCheckDetailId())
                .orElseThrow(() -> new RuntimeException("Stock check request detail not found"));
        InventoryItem inventoryItem = inventoryItemRepository.findById(request.getInventoryItemId())
                .orElseThrow(() -> new RuntimeException("Inventory item not found"));
        for(String inventoryItemId : detail.getCheckedInventoryItemsId()) {
            if (inventoryItemId.equals(request.getInventoryItemId())) {
                    throw new RuntimeException("Inventory item is already being tracked for stock check");
            }
        }
        boolean flag = false;
        for(String inventoryItemId : detail.getInventoryItemsId()) {
            if (inventoryItemId.equals(request.getInventoryItemId())) {
                detail.setActualQuantity(detail.getActualQuantity() + 1);
                detail.setActualMeasurementValue(detail.getActualMeasurementValue() + inventoryItem.getMeasurementValue());
                detail.getCheckedInventoryItemsId().add(inventoryItem.getId());
                flag = false;
                break;
            }
            flag = true;
        }
        if(flag){
            throw new RuntimeException("Inventory item ID not found in stock check request detail");
        }
        if(detail.getActualMeasurementValue() > detail.getMeasurementValue()) {
            detail.setStatus(DetailStatus.EXCESS);
        } else if(detail.getActualQuantity() == detail.getQuantity()) {
            detail.setStatus(DetailStatus.LACK);
        } else {
            detail.setStatus(DetailStatus.LACK);
        }
        return mapToResponse(stockCheckRequestDetailRepository.save(detail));
    }

    @Transactional
    public StockCheckRequestDetailResponse resetTrackingForStockCheckRequestDetail(UpdateActualStockCheck request) {
        LOGGER.info("Resetting tracking for stock check request detail with ID: {}", request.getStockCheckDetailId());
        StockCheckRequestDetail detail = stockCheckRequestDetailRepository.findById(request.getStockCheckDetailId())
                .orElseThrow(() -> new RuntimeException("Stock check request detail not found"));
        InventoryItem inventoryItem = inventoryItemRepository.findById(request.getInventoryItemId())
                .orElseThrow(() -> new RuntimeException("Inventory item not found"));

        boolean checked = false;
        for (String inventoryItemId : detail.getCheckedInventoryItemsId()) {
            if (inventoryItemId.equals(request.getInventoryItemId())) {
                checked = true;
                break;
            }
        }
        if(!checked) {
            throw new RuntimeException("Inventory item is not being tracked for stock check");
        }

        boolean found = false;
        for (String inventoryItemId : detail.getInventoryItemsId()) {
            if (inventoryItemId.equals(request.getInventoryItemId())) {
                detail.setActualQuantity(detail.getActualQuantity() - 1);
                detail.setActualMeasurementValue(
                        detail.getActualMeasurementValue() - inventoryItem.getMeasurementValue()
                );
                detail.getCheckedInventoryItemsId().remove(inventoryItem.getId());
                found = true;
                break;
            }
        }

        if (!found) {
            throw new RuntimeException("Inventory item ID not found in stock check request detail");
        }


        if(detail.getActualMeasurementValue() > detail.getMeasurementValue()) {
            detail.setStatus(DetailStatus.EXCESS);
        } else if(detail.getActualQuantity() == detail.getQuantity()) {
            detail.setStatus(DetailStatus.LACK);
        } else {
            detail.setStatus(DetailStatus.LACK);
        }

        if(detail.getActualQuantity() == 0.0 && detail.getActualMeasurementValue() == 0.0) {
            detail.setStatus(null);
        }
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
                .stockCheckRequestId(detail.getStockCheckRequest().getId())
                .itemId(detail.getItem().getId())
                .inventoryItemIds(detail.getInventoryItemsId())
                .checkedInventoryItemIds(detail.getCheckedInventoryItemsId())
                .build();
    }
}
