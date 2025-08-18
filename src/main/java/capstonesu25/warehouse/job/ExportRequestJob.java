package capstonesu25.warehouse.job;

import capstonesu25.warehouse.entity.Configuration;
import capstonesu25.warehouse.entity.ExportRequest;
import capstonesu25.warehouse.entity.ExportRequestDetail;
import capstonesu25.warehouse.enums.RequestStatus;
import capstonesu25.warehouse.repository.ConfigurationRepository;
import capstonesu25.warehouse.repository.ExportRequestDetailRepository;
import capstonesu25.warehouse.repository.ExportRequestRepository;
import capstonesu25.warehouse.repository.InventoryItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

@Component
@RequiredArgsConstructor
@EnableScheduling
public class ExportRequestJob {
    private final ConfigurationRepository configurationRepository;
    private final ExportRequestRepository exportRequestRepository;
    private final ExportRequestDetailRepository exportRequestDetailRepository;
    private final InventoryItemRepository inventoryItemRepository;

    private static final Logger LOGGER = Logger.getLogger(ExportRequestJob.class.getName());
    private LocalDate lastRunDate = null;
    @Scheduled(fixedRate = 60_000, zone = "Asia/Ho_Chi_Minh")
    public void cancelExportRequestJob() {
        Configuration config = configurationRepository.findAll().get(0);
        LocalTime cancelTime = config.getTimeToAllowCancel();
        LocalTime now = LocalTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));

        if (now.isBefore(cancelTime) || today.equals(lastRunDate)) {
            return;
        }

        List<RequestStatus> statuses = List.of(RequestStatus.IN_PROGRESS, RequestStatus.COUNTED
                , RequestStatus.COUNT_CONFIRMED, RequestStatus.WAITING_EXPORT, RequestStatus.NOT_STARTED);
        List<ExportRequest> exportRequests = exportRequestRepository
                .findByExportDateAndStatusIn(today, statuses);

        if (exportRequests.isEmpty()) {
            lastRunDate = today;
            return;
        }

        exportRequests.forEach(order -> {
            order.setStatus(RequestStatus.CANCELLED);
            order.setNote("Tự động hủy do quá hạn xác nhận lúc " + now);
        });

        List<Long> detailIds = exportRequests.stream()
                .filter(Objects::nonNull)
                .flatMap(er -> er.getExportRequestDetails().stream())
                .map(ExportRequestDetail::getId)
                .distinct()
                .toList();

        if (!detailIds.isEmpty()) {
            // 3) Bulk release all inventory items tied to those details
            int released = inventoryItemRepository.releaseByExportDetailIds(detailIds);

            // 4) Keep in-memory collections consistent (optional but clean):
            //    Clear the items list on each detail since owning side is on InventoryItem.
            exportRequests.forEach(er -> er.getExportRequestDetails()
                    .forEach(d -> d.getInventoryItems().clear()));
        }


        exportRequestRepository.saveAll(exportRequests);
        lastRunDate = today;
        LOGGER.info("Đã tự động hủy " + exportRequests.size() + " đơn lúc " + now);
    }

    @Scheduled(cron = "0 1 0 * * *", zone = "Asia/Ho_Chi_Minh") // Run at 00:01 daily
    public void cancelExtendedOrdersPastDueDays() {
        Configuration config = configurationRepository.findAll().get(0);
        int daysAllowed = config.getDaysToAllowExtend();

        LocalDate today = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        LocalDate cancelThreshold = today.minusDays(daysAllowed);

        // Find orders in EXTENDED status where extendedAt ≤ cancelThreshold
        List<ExportRequest> expiredRequest = exportRequestRepository
                .findByStatusAndExtendedDateLessThanEqual(RequestStatus.EXTENDED, cancelThreshold);

        if (expiredRequest.isEmpty()) {
            return;
        }

        expiredRequest.forEach(order -> {
            order.setStatus(RequestStatus.CANCELLED);
            order.setNote("Tự động hủy do đã gia hạn quá " + daysAllowed + " ngày (từ " + order.getExtendedDate() + ")");
        });

        exportRequestRepository.saveAll(expiredRequest);
        LOGGER.info("auto cancel " + expiredRequest.size() + " cause has extended after " + daysAllowed + " days.");
    }
}
