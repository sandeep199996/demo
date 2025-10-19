package com.learningmanagement.demo.controller;



import com.learningmanagement.demo.entity.Enrollment;
import com.learningmanagement.demo.security.CustomUserDetails;
import com.learningmanagement.demo.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    // Students enroll in a course
    @PostMapping("/enroll/{courseId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Enrollment> enrollCourse(
            @PathVariable Long courseId,
            @AuthenticationPrincipal CustomUserDetails studentDetails) {
        Enrollment enrollment = enrollmentService.enrollStudent(studentDetails.getUser(), courseId);
        return ResponseEntity.status(HttpStatus.CREATED).body(enrollment);
    }

    // Students can unenroll
    @DeleteMapping("/unenroll/{enrollmentId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Void> unenrollCourse(
            @PathVariable Long enrollmentId,
            @AuthenticationPrincipal CustomUserDetails studentDetails) {
        enrollmentService.unenrollStudent(enrollmentId, studentDetails.getUser());
        return ResponseEntity.noContent().build();
    }

    // Students view their enrollments
    @GetMapping("/my-courses")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<Enrollment>> myEnrollments(@AuthenticationPrincipal CustomUserDetails studentDetails) {
        List<Enrollment> enrollments = enrollmentService.getEnrollmentsByStudent(studentDetails.getUser().getId());
        return ResponseEntity.ok(enrollments);
    }

    // Instructors/Admins see enrollments for a course
    @GetMapping("/by-course/{courseId}")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<List<Enrollment>> courseEnrollments(@PathVariable Long courseId) {
        List<Enrollment> enrollments = enrollmentService.getEnrollmentsByCourse(courseId);
        return ResponseEntity.ok(enrollments);
    }

    // Update progress (for future extension)
    @PatchMapping("/progress/{enrollmentId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Void> updateProgress(
            @PathVariable Long enrollmentId,
            @RequestParam int progress) {
        enrollmentService.updateProgress(enrollmentId, progress);
        return ResponseEntity.noContent().build();
    }
}
