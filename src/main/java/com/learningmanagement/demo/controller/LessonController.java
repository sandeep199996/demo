package com.learningmanagement.demo.controller;


import com.learningmanagement.demo.dto.LessonRequest;
import com.learningmanagement.demo.entity.Lesson;
import com.learningmanagement.demo.security.CustomUserDetails;
import com.learningmanagement.demo.service.LessonService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses/{courseId}/lessons")
@RequiredArgsConstructor
public class LessonController {

    private final LessonService lessonService;

    // Public endpoints

    @GetMapping
    public ResponseEntity<List<Lesson>> getLessonsByCourse(@PathVariable Long courseId) {
        return ResponseEntity.ok(lessonService.getLessonsByCourse(courseId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Lesson> getLessonById(@PathVariable Long id) {
        return ResponseEntity.ok(lessonService.getLessonById(id));
    }

    // Instructor endpoints

    @PostMapping
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<Lesson> createLesson(
            @PathVariable Long courseId,
            @Valid @RequestBody LessonRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Lesson lesson = lessonService.createLesson(courseId, request, userDetails.getUser());
        return ResponseEntity.status(HttpStatus.CREATED).body(lesson);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<Lesson> updateLesson(
            @PathVariable Long id,
            @Valid @RequestBody LessonRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Lesson lesson = lessonService.updateLesson(id, request, userDetails.getUser());
        return ResponseEntity.ok(lesson);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<Void> deleteLesson(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        lessonService.deleteLesson(id, userDetails.getUser());
        return ResponseEntity.noContent().build();
    }
}
