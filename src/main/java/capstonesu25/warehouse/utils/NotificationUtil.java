package capstonesu25.warehouse.utils;

import com.pusher.rest.Pusher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class NotificationUtil {

    private final Pusher pusher;

    public static final String WAREHOUSE_MANAGER_CHANNEL = "private-notifications-WAREHOUSE_MANAGER";
    public static final String DEPARTMENT_CHANNEL = "private-notifications-DEPARTMENT";
    public static final String ACCOUNTING_CHANNEL = "private-notifications-ACCOUNTING";
    public static final String ADMIN_CHANNEL = "private-notifications-ADMIN";

    public static final String IMPORT_ORDER_CREATED_EVENT = "import-order-created";
    public static final String IMPORT_ORDER_COUNTED_EVENT = "import-order-counted";
    public static final String IMPORT_ORDER_CONFIRMED_EVENT = "import-order-confirmed";
    public static final String IMPORT_ORDER_CANCELLED_EVENT = "import-order-cancelled";
    public static final String IMPORT_ORDER_EXTENDED_EVENT = "import-order-extended";
    public static final String IMPORT_ORDER_COMPLETED_EVENT = "import-order-completed";

    public NotificationUtil(Pusher pusher) {
        this.pusher = pusher;
    }

    @Async
    public void notify(String channel, String event, Object notificationData) {
        try {
            pusher.trigger(channel, event, notificationData);
        } catch (Exception e) {
            System.err.println("Error sending Pusher notification: " + e.getMessage());
            e.printStackTrace();
        }
    }
}