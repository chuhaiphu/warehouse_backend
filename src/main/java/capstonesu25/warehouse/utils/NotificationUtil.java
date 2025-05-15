package capstonesu25.warehouse.utils;

import com.pusher.rest.Pusher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class NotificationUtil {

    private final Pusher pusher;

    public static final String WAREHOUSE_MANAGER_CHANNEL = "private-notifications-WAREHOUSE_MANAGER";
    public static final String DEPARTMENT_CHANNEL = "private-notifications-DEPARTMENT";
    public static final String STAFF_CHANNEL = "private-notifications-STAFF";
    public static final String ACCOUNTING_CHANNEL = "private-notifications-ACCOUNTING";
    public static final String ADMIN_CHANNEL = "private-notifications-ADMIN";

    public static final String IMPORT_ORDER_CREATED_EVENT = "import-order-created";
    public static final String IMPORT_ORDER_COUNTED_EVENT = "import-order-counted";
    public static final String IMPORT_ORDER_CONFIRMED_EVENT = "import-order-confirmed";

    public NotificationUtil(Pusher pusher) {
        this.pusher = pusher;
    }

    @Async
    public void notify(String channel, String event, Object notificationData) {
        pusher.trigger(
            channel,
            event,
            notificationData
        );
    }
}