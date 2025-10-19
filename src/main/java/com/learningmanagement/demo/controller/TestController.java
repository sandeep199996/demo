package com.learningmanagement.demo.controller;

import com.learningmanagement.demo.entity.Notification;
import com.learningmanagement.demo.entity.NotificationType;
import com.learningmanagement.demo.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class TestController {

    private final NotificationService notificationService;

    @PostMapping("/broadcast")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> testBroadcast(@RequestBody String message) {
        Notification notification = Notification.builder()
                .title("System Announcement")
                .message(message)
                .type(NotificationType.ANNOUNCEMENT)
                .build();

        notificationService.broadcastNotification(notification);
        return ResponseEntity.ok("Broadcast sent");
    }
}
