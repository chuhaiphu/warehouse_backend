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
    public static final String MANAGER_CHANNEL = "private-notifications-MANAGER";
    public static final String STAFF_CHANNEL = "private-notifications-STAFF-";

    public static final String IMPORT_ORDER_CREATED_EVENT = "import-order-created";
    public static final String IMPORT_ORDER_ASSIGNED_EVENT = "import-order-assigned";
    public static final String IMPORT_ORDER_COUNTED_EVENT = "import-order-counted";
    public static final String IMPORT_ORDER_CONFIRMED_COUNT_EVENT = "import-order-confirmed-count";
    public static final String IMPORT_ORDER_CANCELLED_EVENT = "import-order-cancelled";
    public static final String IMPORT_ORDER_EXTENDED_EVENT = "import-order-extended";
    public static final String IMPORT_ORDER_COMPLETED_EVENT = "import-order-completed";
    public static final String IMPORT_ORDER_READY_TO_STORE_EVENT = "import-order-ready-to-store";
    public static final String IMPORT_ORDER_STORED_EVENT = "import-order-stored";
    public static final String IMPORT_ORDER_COUNT_AGAIN_REQUESTED_EVENT = "import-order-count-again-requested";
    public static final String IMPORT_ORDER_CONFIRMED_COUNT_AGAIN_EVENT = "import-order-confirmed-count-again";

    public static final String EXPORT_REQUEST_CREATED_EVENT = "export-request-created";
    public static final String EXPORT_REQUEST_ASSIGNED_EVENT = "export-request-assigned";
    public static final String EXPORT_REQUEST_COUNTED_EVENT = "export-request-counted";
    public static final String EXPORT_REQUEST_CONFIRMED_EVENT = "export-request-confirmed";
    public static final String EXPORT_REQUEST_CANCELLED_EVENT = "export-request-cancelled";
    public static final String EXPORT_REQUEST_EXTENDED_EVENT = "export-request-extended";
    public static final String EXPORT_REQUEST_COMPLETED_EVENT = "export-request-completed";

    public static final String STOCK_CHECK_CREATED_EVENT = "stock-check-created";
    public static final String STOCK_CHECK_ASSIGNED_EVENT = "stock-check-assigned";
    public static final String STOCK_CHECK_COUNTED_EVENT = "stock-check-counted";
    public static final String STOCK_CHECK_CONFIRMED_EVENT = "stock-check-confirmed";
    public static final String STOCK_CHECK_COMPLETED_EVENT = "stock-check-completed";

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