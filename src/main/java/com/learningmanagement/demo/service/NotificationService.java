package com.learningmanagement.demo.service;



import com.learningmanagement.demo.entity.Notification;
import com.learningmanagement.demo.entity.NotificationType;
import com.learningmanagement.demo.entity.User;
import com.learningmanagement.demo.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public Notification createNotification(User user, String title, String message,
                                           NotificationType type, String link) {
        Notification notification = Notification.builder()
                .user(user)
                .title(title)
                .message(message)
                .type(type)
                .link(link)
                .read(false)
                .build();

        Notification saved = notificationRepository.save(notification);

        // Send real-time notification via WebSocket
        sendRealtimeNotification(user.getId(), saved);

        return saved;
    }

    public void sendRealtimeNotification(Long userId, Notification notification) {
        // Send to specific user's queue
        messagingTemplate.convertAndSendToUser(
                userId.toString(),
                "/queue/notifications",
                notification
        );
    }

    public void broadcastNotification(Notification notification) {
        // Broadcast to all connected users
        messagingTemplate.convertAndSend("/topic/notifications", notification);
    }

    public List<Notification> getUserNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<Notification> getUnreadNotifications(Long userId) {
        return notificationRepository.findByUserIdAndReadFalseOrderByCreatedAtDesc(userId);
    }

    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndReadFalse(userId);
    }

    @Transactional
    public void markAsRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            notification.setRead(true);
            notificationRepository.save(notification);
        });
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        List<Notification> unread = getUnreadNotifications(userId);
        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
    }

    // Helper methods for common notification scenarios

    public void notifyCourseEnrollment(User student, String courseName) {
        createNotification(
                student,
                "Course Enrollment",
                "You have successfully enrolled in " + courseName,
                NotificationType.COURSE_ENROLLMENT,
                "/courses"
        );
    }

    public void notifyNewLesson(User student, String courseName, String lessonTitle) {
        createNotification(
                student,
                "New Lesson Available",
                "A new lesson '" + lessonTitle + "' is available in " + courseName,
                NotificationType.NEW_LESSON,
                "/courses"
        );
    }

    public void notifyQuizGraded(User student, String quizTitle, int score) {
        createNotification(
                student,
                "Quiz Graded",
                "Your quiz '" + quizTitle + "' has been graded. Score: " + score + "%",
                NotificationType.QUIZ_GRADED,
                "/quizzes"
        );
    }
}
