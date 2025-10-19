package com.learningmanagement.demo.controller;


import com.learningmanagement.demo.dto.StudentAnalytics;
import com.learningmanagement.demo.dto.InstructorAnalytics;
import com.learningmanagement.demo.dto.CourseAnalytics;
import com.learningmanagement.demo.entity.Enrollment;
import com.learningmanagement.demo.entity.Notification;
import com.learningmanagement.demo.entity.User;
import com.learningmanagement.demo.security.CustomUserDetails;
import com.learningmanagement.demo.service.AnalyticsService;
import com.learningmanagement.demo.service.EnrollmentService;
import com.learningmanagement.demo.service.NotificationService;
import com.learningmanagement.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final EnrollmentService enrollmentService;
    private final NotificationService notificationService; // Add this
    private final AnalyticsService analyticsService; // Add this

    @GetMapping("/profile")
    public ResponseEntity<User> getCurrentUser(@AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userService.findByEmail(userDetails.getUsername());
        return ResponseEntity.ok(user);
    }

    @PutMapping("/profile")
    public ResponseEntity<User> updateProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody User userUpdateRequest
    ) {
        User currentUser = userService.findByEmail(userDetails.getUsername());
        User updatedUser = userService.updateUser(currentUser.getId(), userUpdateRequest);
        return ResponseEntity.ok(updatedUser);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        User user = userService.findById(id);
        return ResponseEntity.ok(user);
    }

    // Enhanced Student Dashboard with analytics
    @GetMapping("/student/dashboard")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Map<String, Object>> getEnhancedStudentDashboard(
            @AuthenticationPrincipal CustomUserDetails studentDetails) {

        Long studentId = studentDetails.getUser().getId();
        Map<String, Object> dashboard = new HashMap<>();

        // Basic enrollment info
        List<Enrollment> enrollments = enrollmentService.getEnrollmentsByStudent(studentId);
        dashboard.put("enrollments", enrollments);

        // Analytics
        StudentAnalytics analytics = analyticsService.getStudentAnalytics(studentId);
        dashboard.put("analytics", analytics);

        // Recent notifications
        List<Notification> notifications = notificationService.getUnreadNotifications(studentId);
        dashboard.put("recentNotifications", notifications);

        // Progress summary
        Map<String, Object> progress = new HashMap<>();
        progress.put("totalCourses", enrollments.size());
        progress.put("completed", analytics.getCompletedCourses());
        progress.put("inProgress", analytics.getInProgressCourses());
        progress.put("averageProgress", analytics.getAverageProgress());
        dashboard.put("progress", progress);

        return ResponseEntity.ok(dashboard);
    }

    // Enhanced Instructor Dashboard with analytics
    @GetMapping("/instructor/dashboard")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<Map<String, Object>> getEnhancedInstructorDashboard(
            @AuthenticationPrincipal CustomUserDetails instructorDetails) {

        Long instructorId = instructorDetails.getUser().getId();
        Map<String, Object> dashboard = new HashMap<>();

        // Instructor analytics
        InstructorAnalytics analytics = analyticsService.getInstructorAnalytics(instructorId);
        dashboard.put("analytics", analytics);

        // Course analytics list
        List<CourseAnalytics> coursesAnalytics = analyticsService
                .getInstructorCoursesAnalytics(instructorId);
        dashboard.put("coursesAnalytics", coursesAnalytics);

        // Recent activity
        dashboard.put("totalCourses", analytics.getTotalCourses());
        dashboard.put("totalStudents", analytics.getTotalStudents());
        dashboard.put("averageProgress", analytics.getAverageStudentProgress());

        return ResponseEntity.ok(dashboard);
    }

    // Admin endpoints
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @DeleteMapping("/admin/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}