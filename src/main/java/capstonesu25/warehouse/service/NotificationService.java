package capstonesu25.warehouse.service;

import capstonesu25.warehouse.entity.Notification;
import capstonesu25.warehouse.model.notification.NotificationResponse;
import capstonesu25.warehouse.repository.NotificationRepository;
import capstonesu25.warehouse.utils.NotificationUtil;
import capstonesu25.warehouse.entity.Account;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final NotificationUtil notificationUtil;
    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationService.class);

    public NotificationResponse deleteNotification(Long id) {
        LOGGER.info("Deleting notification with id={}", id);
        Notification notification = notificationRepository.findById(id).orElseThrow(() -> new RuntimeException("Notification not found"));
        notificationRepository.deleteById(id);
        return mapToResponse(notification);
    }

    public List<NotificationResponse> getAllNotificationsByAccountId(Long accountId) {
        LOGGER.info("Getting all notifications for accountId={}", accountId);
        return notificationRepository.findAll().stream()
                .filter(n -> n.getReceiver() != null && n.getReceiver().getId().equals(accountId))
                .map(this::mapToResponse)
                .toList();
    }

    public List<NotificationResponse> viewAllNotificationsByAccountId(Long accountId) {
        LOGGER.info("Marking all notifications as viewed for accountId={}", accountId);
        List<Notification> notifications = notificationRepository.findAll().stream()
                .filter(n -> n.getReceiver() != null && n.getReceiver().getId().equals(accountId))
                .toList();
        notifications.forEach(n -> n.setIsViewed(true));
        notificationRepository.saveAll(notifications);
        return notifications.stream().map(this::mapToResponse).toList();
    }

    public NotificationResponse clickNotification(Long id) {
        LOGGER.info("Marking notification {} as clicked", id);
        Notification notification = notificationRepository.findById(id).orElseThrow(() -> new RuntimeException("Notification not found"));
        notification.setIsClicked(true);
        notificationRepository.save(notification);
        return mapToResponse(notification);
    }

    public void handleNotification(String channel, String event, String objectId, String content, List<Account> receivers) {
        LOGGER.info("Handling notification: channel={}, event={}, objectId={}, content={}", channel, event, objectId, content);
        LocalDateTime now = LocalDateTime.now();
        Map<String, Object> payload = new HashMap<>();
        payload.put("objectId", objectId);
        payload.put("content", content);
        payload.put("isViewed", false);
        payload.put("isClicked", false);
        payload.put("eventType", event);
        payload.put("createdDate", now.toString());
        notificationUtil.notify(channel, event, payload);
        for (Account receiver : receivers) {
            Notification notification = Notification.builder()
                .receiver(receiver)
                .objectId(objectId)
                .eventType(event)
                .content(content)
                .isViewed(false)
                .isClicked(false)
                .createdDate(now)
                .build();
            notificationRepository.save(notification);
        }
    }

    private NotificationResponse mapToResponse(Notification notification) {
        NotificationResponse response = new NotificationResponse();
        response.setId(notification.getId());
        response.setReceiverId(notification.getReceiver() != null ? notification.getReceiver().getId() : null);
        response.setObjectId(notification.getObjectId());
        response.setEventType(notification.getEventType());
        response.setContent(notification.getContent());
        response.setCreatedDate(notification.getCreatedDate());
        response.setIsViewed(notification.getIsViewed());
        response.setIsClicked(notification.getIsClicked());
        return response;
    }
}
