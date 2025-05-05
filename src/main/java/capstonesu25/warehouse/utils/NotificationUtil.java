package capstonesu25.warehouse.utils;

import com.pusher.rest.Pusher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class NotificationUtil {

    private final Pusher pusher;

    public static final String WAREHOUSE_MANAGER_CHANNEL = "notifications-WAREHOUSE_MANAGER";
    public static final String IMPORT_ORDER_EVENT = "import-order-created";

    public NotificationUtil(Pusher pusher) {
        this.pusher = pusher;
    }

    @Async
    public void notifyWarehouseManagers(Object notificationData) {
        pusher.trigger(
            WAREHOUSE_MANAGER_CHANNEL,
            IMPORT_ORDER_EVENT,
            notificationData
        );
    }
}