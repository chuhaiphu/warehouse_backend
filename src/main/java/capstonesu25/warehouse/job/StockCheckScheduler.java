package capstonesu25.warehouse.job;

import capstonesu25.warehouse.entity.Configuration;
import capstonesu25.warehouse.repository.ConfigurationRepository;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
@RequiredArgsConstructor
public class StockCheckScheduler  implements SchedulingConfigurer {
    private final ConfigurationRepository configurationRepository;
    private final StockCheckJob stockCheckJob;

    @Override
    public void configureTasks(ScheduledTaskRegistrar registrar) {
        registrar.addTriggerTask(
                // the task to run
                stockCheckJob::createStockCheckJob,
                // dynamic trigger
                triggerContext -> {
                    // get "every N months" from DB
                    int every = configurationRepository.findAll().stream()
                            .findFirst()
                            .map(Configuration::getPeriodicCreatingStockCheck)
                            .orElse(4); // default 4 months

                    ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));

                    // candidate: next 1st of month at 00:00
                    ZonedDateTime next = now.plusMonths(1)
                            .withDayOfMonth(1)
                            .withHour(0).withMinute(0).withSecond(0).withNano(0);

                    // skip months until it's divisible by "every"
                    while (((next.getMonthValue() - 1) % every) != 0) {
                        next = next.plusMonths(1)
                                .withDayOfMonth(1)
                                .withHour(0).withMinute(0).withSecond(0).withNano(0);
                    }

                    return Date.from(next.toInstant()).toInstant();
                }
        );
    }
}
