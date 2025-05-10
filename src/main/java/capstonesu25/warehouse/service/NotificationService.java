package capstonesu25.warehouse.service;

import capstonesu25.warehouse.entity.Account;
import capstonesu25.warehouse.entity.Notification;
import capstonesu25.warehouse.model.notification.NotificationRequest;
import capstonesu25.warehouse.model.notification.NotificationResponse;
import capstonesu25.warehouse.repository.AccountRepository;
import capstonesu25.warehouse.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final AccountRepository accountRepository;
    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationService.class);

    public NotificationResponse createNotification(NotificationRequest notificationRequest) {
        Account receiver = accountRepository.findById(notificationRequest.getReceiverId())
                .orElseThrow(() -> new RuntimeException("Receiver account not found"));
        Notification notification = mapToEntity(notificationRequest, receiver);
        LOGGER.info("Creating notification for receiverId={}, objectId={}", notification.getReceiver().getId(), notification.getObjectId());
        Notification saved = notificationRepository.save(notification);
        return mapToResponse(saved);
    }

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

    private NotificationResponse mapToResponse(Notification notification) {
        NotificationResponse response = new NotificationResponse();
        response.setId(notification.getId());
        response.setReceiverId(notification.getReceiver() != null ? notification.getReceiver().getId() : null);
        response.setObjectId(notification.getObjectId());
        response.setContent(notification.getContent());
        response.setCreatedDate(notification.getCreatedDate());
        response.setIsViewed(notification.getIsViewed());
        response.setIsClicked(notification.getIsClicked());
        return response;
    }

    private Notification mapToEntity(NotificationRequest notificationRequest, Account receiver) {
        Notification notification = new Notification();
        notification.setReceiver(receiver);
        notification.setObjectId(notificationRequest.getObjectId());
        notification.setContent(notificationRequest.getContent());
        notification.setIsViewed(false);
        notification.setIsClicked(false);
        return notification;
    }
}
