package com.learningmanagement.demo.controller;



import com.learningmanagement.demo.dto.CourseAnalytics;
import com.learningmanagement.demo.dto.InstructorAnalytics;
import com.learningmanagement.demo.dto.StudentAnalytics;
import com.learningmanagement.demo.security.CustomUserDetails;
import com.learningmanagement.demo.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    // Student gets their own analytics
    @GetMapping("/student/my-analytics")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<StudentAnalytics> getMyStudentAnalytics(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        StudentAnalytics analytics = analyticsService.getStudentAnalytics(
                userDetails.getUser().getId()
        );
        return ResponseEntity.ok(analytics);
    }

    // Instructor gets their own analytics
    @GetMapping("/instructor/my-analytics")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<InstructorAnalytics> getMyInstructorAnalytics(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        InstructorAnalytics analytics = analyticsService.getInstructorAnalytics(
                userDetails.getUser().getId()
        );
        return ResponseEntity.ok(analytics);
    }

    // Instructor gets analytics for specific course
    @GetMapping("/course/{courseId}")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<CourseAnalytics> getCourseAnalytics(@PathVariable Long courseId) {
        CourseAnalytics analytics = analyticsService.getCourseAnalytics(courseId);
        return ResponseEntity.ok(analytics);
    }

    // Instructor gets analytics for all their courses
    @GetMapping("/instructor/courses-analytics")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<List<CourseAnalytics>> getMyCoursesAnalytics(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<CourseAnalytics> analytics = analyticsService.getInstructorCoursesAnalytics(
                userDetails.getUser().getId()
        );
        return ResponseEntity.ok(analytics);
    }

    // Admin: Get analytics for any student
    @GetMapping("/admin/student/{studentId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StudentAnalytics> getStudentAnalytics(@PathVariable Long studentId) {
        StudentAnalytics analytics = analyticsService.getStudentAnalytics(studentId);
        return ResponseEntity.ok(analytics);
    }

    // Admin: Get analytics for any instructor
    @GetMapping("/admin/instructor/{instructorId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<InstructorAnalytics> getInstructorAnalytics(@PathVariable Long instructorId) {
        InstructorAnalytics analytics = analyticsService.getInstructorAnalytics(instructorId);
        return ResponseEntity.ok(analytics);
    }
}
