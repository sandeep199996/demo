package com.learningmanagement.demo.controller;



import com.learningmanagement.demo.entity.*;
import com.learningmanagement.demo.security.CustomUserDetails;
import com.learningmanagement.demo .service.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quizzes")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;

    // Get quizzes by course
    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<Quiz>> getQuizzesByCourse(@PathVariable Long courseId) {
        return ResponseEntity.ok(quizService.getQuizzesByCourse(courseId));
    }

    // Get quiz details
    @GetMapping("/{id}")
    public ResponseEntity<Quiz> getQuiz(@PathVariable Long id) {
        return ResponseEntity.ok(quizService.getQuizById(id));
    }

    // Create quiz (instructor only)
    @PostMapping
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<Quiz> createQuiz(
            @RequestBody Quiz quiz,
            @AuthenticationPrincipal CustomUserDetails instructor) {
        Quiz created = quizService.createQuiz(quiz, instructor.getUser());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // Add question to quiz
    @PostMapping("/{quizId}/questions")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<Question> addQuestion(
            @PathVariable Long quizId,
            @RequestBody Question question,
            @AuthenticationPrincipal CustomUserDetails instructor) {
        Question added = quizService.addQuestion(quizId, question, instructor.getUser());
        return ResponseEntity.status(HttpStatus.CREATED).body(added);
    }

    // Start quiz attempt
    @PostMapping("/{quizId}/start")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<QuizAttempt> startQuiz(
            @PathVariable Long quizId,
            @AuthenticationPrincipal CustomUserDetails student) {
        QuizAttempt attempt = quizService.startQuiz(quizId, student.getUser());
        return ResponseEntity.ok(attempt);
    }

    // Submit quiz with auto-grading
    @PostMapping("/attempts/{attemptId}/submit")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<QuizAttempt> submitQuiz(
            @PathVariable Long attemptId,
            @RequestBody List<Answer> answers) {
        QuizAttempt graded = quizService.submitQuiz(attemptId, answers);
        return ResponseEntity.ok(graded);
    }

    // Get student's attempts
    @GetMapping("/my-attempts")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<QuizAttempt>> getMyAttempts(
            @AuthenticationPrincipal CustomUserDetails student) {
        return ResponseEntity.ok(quizService.getStudentAttempts(student.getUser().getId()));
    }
}
