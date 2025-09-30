package capstonesu25.warehouse.job;

import capstonesu25.warehouse.entity.Configuration;
import capstonesu25.warehouse.entity.StockCheckRequest;
import capstonesu25.warehouse.enums.RequestStatus;
import capstonesu25.warehouse.repository.ConfigurationRepository;
import capstonesu25.warehouse.repository.StockCheckRequestRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
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

    @Scheduled(fixedRate = 60_000, zone = "Asia/Ho_Chi_Minh")
    public void cancelStockCheckJob() {
        Configuration config = configurationRepository.findAll().get(0);
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

}
