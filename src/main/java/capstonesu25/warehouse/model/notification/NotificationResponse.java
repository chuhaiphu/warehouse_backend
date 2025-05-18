package capstonesu25.warehouse.model.notification;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class NotificationResponse {
    private Long id;
    private Long receiverId;
    private String objectId;
    private String content;
    private LocalDateTime createdDate;
    private Boolean isViewed;
    private Boolean isClicked;
}
