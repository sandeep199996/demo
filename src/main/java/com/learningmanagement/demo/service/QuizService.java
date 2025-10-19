package com.learningmanagement.demo.service;



import com.learningmanagement.demo.entity.*;
import com.learningmanagement.demo.exception.ResourceNotFoundException;
import com.learningmanagement.demo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QuizService {

    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final QuizAttemptRepository attemptRepository;
    private final CourseService courseService;
    private final NotificationService notificationService; //

    public List<Quiz> getQuizzesByCourse(Long courseId) {
        return quizRepository.findByCourseIdAndPublishedTrue(courseId);
    }

    public Quiz getQuizById(Long id) {
        return quizRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found"));
    }

    @Transactional
    public Quiz createQuiz(Quiz quiz, User instructor) {
        Course course = courseService.getCourseById(quiz.getCourse().getId());

        if (!course.getInstructor().getId().equals(instructor.getId())) {
            throw new IllegalArgumentException("Unauthorized to create quiz for this course");
        }

        return quizRepository.save(quiz);
    }

    @Transactional
    public Question addQuestion(Long quizId, Question question, User instructor) {
        Quiz quiz = getQuizById(quizId);

        if (!quiz.getCourse().getInstructor().getId().equals(instructor.getId())) {
            throw new IllegalArgumentException("Unauthorized");
        }

        question.setQuiz(quiz);
        return questionRepository.save(question);
    }

    @Transactional
    public QuizAttempt startQuiz(Long quizId, User student) {
        Quiz quiz = getQuizById(quizId);

        // Check for incomplete attempts
        attemptRepository.findByQuizIdAndStudentIdAndCompletedFalse(quizId, student.getId())
                .ifPresent(attempt -> {
                    throw new IllegalArgumentException("You have an incomplete attempt for this quiz");
                });

        QuizAttempt attempt = QuizAttempt.builder()
                .quiz(quiz)
                .student(student)
                .startedAt(LocalDateTime.now())
                .totalMarks(quiz.getTotalMarks())
                .completed(false)
                .build();

        return attemptRepository.save(attempt);
    }

    @Transactional
    public QuizAttempt submitQuiz(Long attemptId, List<Answer> answers) {
        QuizAttempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new ResourceNotFoundException("Attempt not found"));

        if (attempt.isCompleted()) {
            throw new IllegalArgumentException("Quiz already submitted");
        }

        // Auto-grade the quiz
        int totalScore = 0;

        for (Answer answer : answers) {
            Question question = answer.getQuestion();
            boolean isCorrect = false;
            int marksObtained = 0;

            // Grade based on question type
            if (question.getType() == QuestionType.MULTIPLE_CHOICE ||
                    question.getType() == QuestionType.TRUE_FALSE) {

                if (answer.getSelectedOption() != null && answer.getSelectedOption().isCorrect()) {
                    isCorrect = true;
                    marksObtained = question.getMarks();
                }
            } else if (question.getType() == QuestionType.SHORT_ANSWER) {
                // Simple string match (case-insensitive)
                if (answer.getAnswerText() != null &&
                        answer.getAnswerText().trim().equalsIgnoreCase(question.getCorrectAnswer().trim())) {
                    isCorrect = true;
                    marksObtained = question.getMarks();
                }
            }

            answer.setCorrect(isCorrect);
            answer.setMarksObtained(marksObtained);
            answer.setAttempt(attempt);
            totalScore += marksObtained;
        }

        attempt.setAnswers(new HashSet<>(answers));
        attempt.setScore(totalScore);
        attempt.setSubmittedAt(LocalDateTime.now());
        attempt.setCompleted(true);

        // Check if passed
        int percentage = (totalScore * 100) / attempt.getTotalMarks();
        attempt.setPassed(percentage >= attempt.getQuiz().getPassingScore());

        // Save the graded attempt
        QuizAttempt graded = attemptRepository.save(attempt);

        // Send notification (Day 7 addition)
        notificationService.notifyQuizGraded(
                graded.getStudent(),
                graded.getQuiz().getTitle(),
                percentage
        );

        // Return the graded attempt
        return graded;
    }


    public List<QuizAttempt> getStudentAttempts(Long studentId) {
        return attemptRepository.findByStudentId(studentId);
    }
}
