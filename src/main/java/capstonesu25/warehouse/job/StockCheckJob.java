package capstonesu25.warehouse.job;

import capstonesu25.warehouse.entity.Configuration;
import capstonesu25.warehouse.entity.InventoryItem;
import capstonesu25.warehouse.entity.Item;
import capstonesu25.warehouse.entity.StockCheckRequest;
import capstonesu25.warehouse.enums.ItemStatus;
import capstonesu25.warehouse.enums.RequestStatus;
import capstonesu25.warehouse.enums.StockCheckType;
import capstonesu25.warehouse.model.stockcheck.StockCheckRequestRequest;
import capstonesu25.warehouse.model.stockcheck.StockCheckRequestResponse;
import capstonesu25.warehouse.model.stockcheck.detail.StockCheckRequestDetailRequest;
import capstonesu25.warehouse.repository.ConfigurationRepository;
import capstonesu25.warehouse.repository.ItemRepository;
import capstonesu25.warehouse.repository.StockCheckRequestRepository;
import capstonesu25.warehouse.service.StockCheckDetailService;
import capstonesu25.warehouse.service.StockCheckService;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@EnableScheduling
public class StockCheckJob {
    private final ConfigurationRepository configurationRepository;
    private final StockCheckRequestRepository stockCheckRequestRepository;
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(StockCheckJob.class);
    private LocalDate lastRunDate = null;
    private final ItemRepository itemRepository;
    private final StockCheckService stockCheckService;
    private final StockCheckDetailService stockCheckDetailService;

    @Scheduled(fixedRate = 60_000, zone = "Asia/Ho_Chi_Minh")
    public void cancelStockCheckJob() {
        Configuration config = configurationRepository.findAll().getFirst();
        LocalTime cancelTime = config.getTimeToAllowCancel();
        LocalTime now = LocalTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));

        if (now.isBefore(cancelTime) || today.equals(lastRunDate)) {
            return;
        }

        LocalDate dayWillBeCancel = today.plusDays(config.getDayWillBeCancelRequest());

        List<RequestStatus> statuses = List.of(RequestStatus.NOT_STARTED,RequestStatus.IN_PROGRESS, RequestStatus.COUNTED
                , RequestStatus.COUNT_CONFIRMED);

        List<StockCheckRequest> stockCheckRequests = stockCheckRequestRepository
                .findAllByStatusIn(statuses);

        List<StockCheckRequest> checkRequests = stockCheckRequests.stream()
                .filter(stockCheckRequest -> stockCheckRequest.getExpectedCompletedDate() != null
                        && !stockCheckRequest.getExpectedCompletedDate().isBefore(dayWillBeCancel))
                .toList();

        if (checkRequests.isEmpty()) {
            lastRunDate = today;
            return;
        }

        checkRequests.forEach(order -> {
            order.setStatus(RequestStatus.CANCELLED);
            order.setNote("Tự động hủy do quá hạn xác nhận lúc " + now);
        });

        stockCheckRequestRepository.saveAll(checkRequests);
        lastRunDate = today;
        logger.info("Đã tự động hủy " + checkRequests.size() + " đơn lúc " + now);
    }

    public void createStockCheckJob  () {
        StockCheckRequestRequest request = new StockCheckRequestRequest();
        request.setStockCheckReason("Kiểm tra định kỳ");
        request.setType(StockCheckType.PERIODIC);
        request.setStartDate(LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh")));
        request.setExpectedCompletedDate(LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh")).plusDays(3));
        request.setCountingDate(LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh")));
        request.setCountingTime(LocalTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));
        request.setNote("Kiểm tra thông số của toàn bộ mặt hàng trong kho");

        StockCheckRequestResponse response = stockCheckService.createStockCheckRequest(request);
        List<StockCheckRequestDetailRequest> detailRequests = new ArrayList<>();

        List<Item> items = itemRepository.findAll();
        for(Item item : items) {
            if(item.getInventoryItems() == null) {
                break;
            }

            List<InventoryItem> inventoryItems = item.getInventoryItems().stream()
                    .filter(inv -> inv.getStatus().equals(ItemStatus.AVAILABLE)).toList();

            double totalMeasurement = inventoryItems.stream()
                    .mapToDouble(InventoryItem::getMeasurementValue)
                    .sum();

            StockCheckRequestDetailRequest detailRequest = new StockCheckRequestDetailRequest();
            detailRequest.setItemId(item.getId());
            detailRequest.setQuantity(inventoryItems.size());
            detailRequest.setMeasurementValue(totalMeasurement);

            detailRequests.add(detailRequest);
        }

        stockCheckDetailService.create(detailRequests, response.getId());
    }

}
