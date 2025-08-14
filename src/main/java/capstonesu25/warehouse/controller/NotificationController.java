package capstonesu25.warehouse.controller;

import capstonesu25.warehouse.service.NotificationService;
import capstonesu25.warehouse.utils.ResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notification")
@RequiredArgsConstructor
@Validated
public class NotificationController {
    private final NotificationService notificationService;

    @Operation(summary = "Delete notification by id")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteNotification(@PathVariable Long id) {
        return ResponseUtil.getObject(
                notificationService.deleteNotification(id),
                HttpStatus.OK,
                "Notification deleted successfully"
        );
    }

    @Operation(summary = "Get all notifications by accountId")
    @GetMapping()
    public ResponseEntity<?> getAllNotificationsByAccountId(@RequestParam Long accountId) {
        return ResponseUtil.getCollection(
                notificationService.getAllNotificationsByAccountId(accountId),
                HttpStatus.OK,
                "Fetch all notifications successfully",
                null
        );
    }

    @Operation(summary = "Mark all notifications as viewed by accountId")
    @PutMapping("/view-all")
    public ResponseEntity<?> viewAllNotificationsByAccountId(@RequestParam Long accountId) {
        return ResponseUtil.getCollection(
                notificationService.viewAllNotificationsByAccountId(accountId),
                HttpStatus.OK,
                "All notifications marked as viewed",
                null
        );
    }

    @Operation(summary = "Click notification by id")
    @PutMapping("/click")
    public ResponseEntity<?> clickNotification(@RequestParam Long id) {
        return ResponseUtil.getObject(
                notificationService.clickNotification(id),
                HttpStatus.OK,
                "Notification marked as clicked"
        );
    }

    @Operation(summary = "Delete all notifications by receiverId")
    @DeleteMapping("/receiver/{receiverId}")
    public ResponseEntity<?> deleteAllNotificationsByReceiverId(@PathVariable Long receiverId) {
        notificationService.deleteAllNotificationsByReceiverId(receiverId);
        return ResponseUtil.getObject(
                null,
                HttpStatus.OK,
                "All notifications deleted successfully for receiver"
        );
    }
}
