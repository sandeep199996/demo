package com.learningmanagement.demo.controller;


import com.learningmanagement.demo.dto.PlatformStatistics;
import com.learningmanagement.demo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final LessonRepository lessonRepository;
    private final QuizRepository quizRepository;
    private final ForumPostRepository forumPostRepository;

    @GetMapping("/platform")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PlatformStatistics> getPlatformStatistics() {
        PlatformStatistics stats = PlatformStatistics.builder()
                .totalCourses(courseRepository.count())
                .totalUsers(userRepository.count())
                .totalEnrollments(enrollmentRepository.count())
                .totalLessons(lessonRepository.count())
                .totalQuizzes(quizRepository.count())
                .totalForumPosts(forumPostRepository.count())
                .build();

        return ResponseEntity.ok(stats);
    }
}
