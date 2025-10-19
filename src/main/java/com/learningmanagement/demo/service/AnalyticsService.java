package com.learningmanagement.demo.service;



import com.learningmanagement.demo.dto.CourseAnalytics;
import com.learningmanagement.demo.dto.InstructorAnalytics;
import com.learningmanagement.demo.dto.StudentAnalytics;
import com.learningmanagement.demo.entity.Course;
import com.learningmanagement.demo.entity.Enrollment;
import com.learningmanagement.demo.entity.User;
import com.learningmanagement.demo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final LessonRepository lessonRepository;
    private final QuizRepository quizRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final ForumPostRepository forumPostRepository;

    // Get student analytics
    public StudentAnalytics getStudentAnalytics(Long studentId) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        List<Enrollment> enrollments = enrollmentRepository.findByStudentId(studentId);

        int totalEnrollments = enrollments.size();
        int completedCourses = (int) enrollments.stream()
                .filter(Enrollment::isCompleted)
                .count();
        int inProgressCourses = totalEnrollments - completedCourses;

        double averageProgress = enrollments.stream()
                .mapToInt(Enrollment::getProgressPercentage)
                .average()
                .orElse(0.0);

        int totalQuizAttempts = (int) quizAttemptRepository.findByStudentId(studentId).size();

        double averageQuizScore = quizAttemptRepository.findByStudentId(studentId).stream()
                .filter(attempt -> attempt.isCompleted())
                .mapToInt(attempt -> (attempt.getScore() * 100) / attempt.getTotalMarks())
                .average()
                .orElse(0.0);

        int forumPostsCount = (int) forumPostRepository.findByAuthorIdOrderByCreatedAtDesc(studentId).size();

        return StudentAnalytics.builder()
                .studentId(studentId)
                .studentName(student.getFullName())
                .totalEnrollments(totalEnrollments)
                .completedCourses(completedCourses)
                .inProgressCourses(inProgressCourses)
                .averageProgress(averageProgress)
                .totalQuizAttempts(totalQuizAttempts)
                .averageQuizScore(averageQuizScore)
                .forumPostsCount(forumPostsCount)
                .certificatesEarned(completedCourses) // Simplified
                .build();
    }

    // Get instructor analytics
    public InstructorAnalytics getInstructorAnalytics(Long instructorId) {
        User instructor = userRepository.findById(instructorId)
                .orElseThrow(() -> new RuntimeException("Instructor not found"));

        List<Course> courses = courseRepository.findByInstructorId(instructorId);

        int totalCourses = courses.size();
        int publishedCourses = (int) courses.stream()
                .filter(Course::isPublished)
                .count();

        int totalStudents = courses.stream()
                .mapToInt(course -> (int) enrollmentRepository.countByCourseId(course.getId()))
                .sum();

        int totalLessons = courses.stream()
                .mapToInt(course -> (int) lessonRepository.countByCourseId(course.getId()))
                .sum();

        int totalQuizzes = courses.stream()
                .mapToInt(course -> (int) quizRepository.findByCourseId(course.getId()).size())
                .sum();

        double averageStudentProgress = courses.stream()
                .flatMap(course -> enrollmentRepository.findByCourseId(course.getId()).stream())
                .mapToInt(Enrollment::getProgressPercentage)
                .average()
                .orElse(0.0);

        int forumPostsInCourses = courses.stream()
                .mapToInt(course -> (int) forumPostRepository.countByCourseId(course.getId()))
                .sum();

        return InstructorAnalytics.builder()
                .instructorId(instructorId)
                .instructorName(instructor.getFullName())
                .totalCourses(totalCourses)
                .publishedCourses(publishedCourses)
                .totalStudents(totalStudents)
                .totalLessons(totalLessons)
                .totalQuizzes(totalQuizzes)
                .averageStudentProgress(averageStudentProgress)
                .forumPostsInCourses(forumPostsInCourses)
                .build();
    }

    // Get course analytics
    public CourseAnalytics getCourseAnalytics(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        List<Enrollment> enrollments = enrollmentRepository.findByCourseId(courseId);

        int enrollmentCount = enrollments.size();
        int completionCount = (int) enrollments.stream()
                .filter(Enrollment::isCompleted)
                .count();

        double completionRate = enrollmentCount > 0
                ? (completionCount * 100.0) / enrollmentCount
                : 0.0;

        double averageProgress = enrollments.stream()
                .mapToInt(Enrollment::getProgressPercentage)
                .average()
                .orElse(0.0);

        int lessonCount = (int) lessonRepository.countByCourseId(courseId);
        int quizCount = quizRepository.findByCourseId(courseId).size();
        int forumPostCount = (int) forumPostRepository.countByCourseId(courseId);

        double averageQuizScore = quizRepository.findByCourseId(courseId).stream()
                .flatMap(quiz -> quizAttemptRepository.findByQuizId(quiz.getId()).stream())
                .filter(attempt -> attempt.isCompleted())
                .mapToInt(attempt -> (attempt.getScore() * 100) / attempt.getTotalMarks())
                .average()
                .orElse(0.0);

        return CourseAnalytics.builder()
                .courseId(courseId)
                .courseTitle(course.getTitle())
                .enrollmentCount(enrollmentCount)
                .completionCount(completionCount)
                .completionRate(completionRate)
                .averageProgress(averageProgress)
                .lessonCount(lessonCount)
                .quizCount(quizCount)
                .forumPostCount(forumPostCount)
                .averageQuizScore(averageQuizScore)
                .build();
    }

    // Get all instructor's courses analytics
    public List<CourseAnalytics> getInstructorCoursesAnalytics(Long instructorId) {
        return courseRepository.findByInstructorId(instructorId).stream()
                .map(course -> getCourseAnalytics(course.getId()))
                .collect(Collectors.toList());
    }
}
