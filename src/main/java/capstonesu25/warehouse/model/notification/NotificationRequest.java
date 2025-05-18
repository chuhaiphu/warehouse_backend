package capstonesu25.warehouse.model.notification;

import capstonesu25.warehouse.entity.Account;
import capstonesu25.warehouse.entity.Notification;
import lombok.Data;

@Data
public class NotificationRequest {
    private Long receiverId;
    private String objectId;
    private String content;

    public Notification toEntity(Account receiver) {
        Notification notification = new Notification();
        notification.setReceiver(receiver);
        notification.setObjectId(this.objectId);
        notification.setContent(this.content);
        notification.setIsViewed(false);
        notification.setIsClicked(false);
        return notification;
    }
}
