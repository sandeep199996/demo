package com.learningmanagement.demo.service;


import com.learningmanagement.demo.entity.Course;
import com.learningmanagement.demo.entity.Enrollment;
import com.learningmanagement.demo.entity.NotificationType;
import com.learningmanagement.demo.entity.User;
import com.learningmanagement.demo.exception.ResourceNotFoundException;
import com.learningmanagement.demo.repository.CourseRepository;
import com.learningmanagement.demo.repository.EnrollmentRepository;
import com.learningmanagement.demo.repository.LessonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final NotificationService notificationService;
    private final LessonRepository lessonRepository; // Add this

    public List<Enrollment> getEnrollmentsByStudent(Long studentId) {
        return enrollmentRepository.findByStudentId(studentId);
    }

    public List<Enrollment> getEnrollmentsByCourse(Long courseId) {
        return enrollmentRepository.findByCourseId(courseId);
    }

    @Transactional
    public Enrollment enrollStudent(User student, Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        if (enrollmentRepository.existsByStudentIdAndCourseId(student.getId(), courseId)) {
            throw new IllegalArgumentException("Already enrolled");
        }

        Enrollment enrollment = Enrollment.builder()
                .student(student)
                .course(course)
                .enrolledAt(LocalDateTime.now())
                .progressPercentage(0)
                .completed(false)
                .build();

        Enrollment saved = enrollmentRepository.save(enrollment);

        // Send notification
        notificationService.notifyCourseEnrollment(student, course.getTitle());

        return saved;
    }

    @Transactional
    public void updateProgress(Long enrollmentId, int progress) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found"));

        enrollment.setProgressPercentage(progress);
        if (progress >= 100) {
            enrollment.setCompleted(true);
            enrollment.setCompletedAt(LocalDateTime.now());
        }
        enrollmentRepository.save(enrollment);
    }

    @Transactional
    public void unenrollStudent(Long enrollmentId, User student) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found"));

        if (!enrollment.getStudent().getId().equals(student.getId())) {
            throw new IllegalArgumentException("Unauthorized");
        }
        enrollmentRepository.delete(enrollment);
    }

    // Add this new method from Day 10
    @Transactional
    public void updateLessonProgress(Long enrollmentId, Long lessonId) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found"));

        // Calculate progress based on lessons completed
        long totalLessons = lessonRepository.countByCourseId(enrollment.getCourse().getId());

        if (totalLessons > 0) {
            // This is simplified - you could track individual lesson completion
            int currentProgress = enrollment.getProgressPercentage();
            int progressIncrement = (int) (100.0 / totalLessons);

            int newProgress = Math.min(currentProgress + progressIncrement, 100);
            enrollment.setProgressPercentage(newProgress);

            if (newProgress >= 100) {
                enrollment.setCompleted(true);
                enrollment.setCompletedAt(LocalDateTime.now());
            }

            enrollmentRepository.save(enrollment);

            // Send real-time progress update
            notificationService.createNotification(
                    enrollment.getStudent(),
                    "Progress Update",
                    "Your progress in " + enrollment.getCourse().getTitle() + " is now " + newProgress + "%",
                    NotificationType.COURSE_UPDATE,
                    "/courses/" + enrollment.getCourse().getId()
            );
        }
    }
}
