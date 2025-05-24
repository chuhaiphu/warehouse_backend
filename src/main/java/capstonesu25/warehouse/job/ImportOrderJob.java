package capstonesu25.warehouse.job;

import capstonesu25.warehouse.entity.Configuration;
import capstonesu25.warehouse.entity.ImportOrder;
import capstonesu25.warehouse.enums.ImportStatus;
import capstonesu25.warehouse.repository.ConfigurationRepository;
import capstonesu25.warehouse.repository.ImportOrderRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@EnableScheduling
public class ImportOrderJob {
    private final ConfigurationRepository configurationRepository;
    private final ImportOrderRepository importOrderRepository;
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(ImportOrderJob.class);

    private LocalDate lastRunDate = null;

    @Scheduled(fixedRate = 60_000, zone = "Asia/Ho_Chi_Minh")
    public void cancelImportOrderJob() {
        Configuration config = configurationRepository.findAll().get(0);
        LocalTime cancelTime = config.getTimeToAllowCancel();
        LocalTime now = LocalTime.now();
        LocalDate today = LocalDate.now();

        if (now.isBefore(cancelTime) || today.equals(lastRunDate)) {
            return;
        }

        List<ImportOrder> importOrders = importOrderRepository
                .findByDateReceivedAndStatus(today, ImportStatus.IN_PROGRESS);

        if (importOrders.isEmpty()) {
            lastRunDate = today;
            return;
        }

        importOrders.forEach(order -> {
            order.setStatus(ImportStatus.CANCELLED);
            order.setNote("Tự động hủy do quá hạn xác nhận lúc " + now);
        });

        importOrderRepository.saveAll(importOrders);
        lastRunDate = today;
        logger.info("Đã tự động hủy " + importOrders.size() + " đơn lúc " + now);
    }

    // Run at 00:01 every day
    @Scheduled(cron = "0 1 0 * * *", zone = "Asia/Ho_Chi_Minh") // Run at 00:01 daily
    public void cancelExtendedOrdersPastDueDays() {
        Configuration config = configurationRepository.findAll().get(0);
        int daysAllowed = config.getDaysToAllowExtend();

        LocalDate today = LocalDate.now();
        LocalDate cancelThreshold = today.minusDays(daysAllowed);

        // Find orders in EXTENDED status where extendedAt ≤ cancelThreshold
        List<ImportOrder> expiredOrders = importOrderRepository
                .findByStatusAndExtendedDateLessThanEqual(ImportStatus.EXTENDED, cancelThreshold);

        if (expiredOrders.isEmpty()) {
            return;
        }

        expiredOrders.forEach(order -> {
            order.setStatus(ImportStatus.CANCELLED);
            order.setNote("Tự động hủy do đã gia hạn quá " + daysAllowed + " ngày (từ " + order.getExtendedDate() + ")");
        });

        importOrderRepository.saveAll(expiredOrders);
        logger.info("auto cancel " + expiredOrders.size() + " cause has extended after " + daysAllowed + " days.");
    }




}
