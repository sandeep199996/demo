package com.learningmanagement.demo.controller;



import com.learningmanagement.demo.entity.Notification;
import com.learningmanagement.demo.security.CustomUserDetails;
import com.learningmanagement.demo.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    // Get all notifications for current user
    @GetMapping
    public ResponseEntity<List<Notification>> getMyNotifications(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<Notification> notifications = notificationService
                .getUserNotifications(userDetails.getUser().getId());
        return ResponseEntity.ok(notifications);
    }

    // Get unread notifications
    @GetMapping("/unread")
    public ResponseEntity<List<Notification>> getUnreadNotifications(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<Notification> notifications = notificationService
                .getUnreadNotifications(userDetails.getUser().getId());
        return ResponseEntity.ok(notifications);
    }

    // Get unread count
    @GetMapping("/unread/count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        long count = notificationService.getUnreadCount(userDetails.getUser().getId());
        Map<String, Long> response = new HashMap<>();
        response.put("count", count);
        return ResponseEntity.ok(response);
    }

    // Mark notification as read
    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok().build();
    }

    // Mark all as read
    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        notificationService.markAllAsRead(userDetails.getUser().getId());
        return ResponseEntity.ok().build();
    }

    // WebSocket message handling
    @MessageMapping("/notify")
    @SendTo("/topic/notifications")
    public Notification handleNotification(Notification notification) {
        return notification;
    }
}
