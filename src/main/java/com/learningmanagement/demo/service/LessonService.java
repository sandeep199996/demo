package com.learningmanagement.demo.service;



import com.learningmanagement.demo.dto.LessonRequest;
import com.learningmanagement.demo.entity.Course;
import com.learningmanagement.demo.entity.Lesson;
import com.learningmanagement.demo.entity.User;
import com.learningmanagement.demo.exception.ResourceNotFoundException;
import com.learningmanagement.demo.repository.LessonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LessonService {

    private final LessonRepository lessonRepository;
    private final CourseService courseService;

    // Get all lessons for a course
    public List<Lesson> getLessonsByCourse(Long courseId) {
        return lessonRepository.findByCourseIdOrderByOrderNumberAsc(courseId);
    }

    // Get lesson by ID
    public Lesson getLessonById(Long id) {
        return lessonRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found with id: " + id));
    }

    // Create new lesson
    @Transactional
    public Lesson createLesson(Long courseId, LessonRequest request, User instructor) {
        Course course = courseService.getCourseById(courseId);

        // Verify instructor owns the course
        if (!course.getInstructor().getId().equals(instructor.getId())) {
            throw new IllegalArgumentException("You are not authorized to add lessons to this course");
        }

        Lesson lesson = Lesson.builder()
                .course(course)
                .title(request.getTitle())
                .content(request.getContent())
                .videoUrl(request.getVideoUrl())
                .orderNumber(request.getOrderNumber())
                .duration(request.getDuration() != null ? request.getDuration() : 0)
                .build();

        return lessonRepository.save(lesson);
    }

    // Update lesson
    @Transactional
    public Lesson updateLesson(Long id, LessonRequest request, User instructor) {
        Lesson lesson = getLessonById(id);

        // Verify instructor owns the course
        if (!lesson.getCourse().getInstructor().getId().equals(instructor.getId())) {
            throw new IllegalArgumentException("You are not authorized to update this lesson");
        }

        lesson.setTitle(request.getTitle());
        lesson.setContent(request.getContent());
        lesson.setVideoUrl(request.getVideoUrl());
        lesson.setOrderNumber(request.getOrderNumber());
        lesson.setDuration(request.getDuration() != null ? request.getDuration() : lesson.getDuration());

        return lessonRepository.save(lesson);
    }

    // Delete lesson
    @Transactional
    public void deleteLesson(Long id, User instructor) {
        Lesson lesson = getLessonById(id);

        // Verify instructor owns the course
        if (!lesson.getCourse().getInstructor().getId().equals(instructor.getId())) {
            throw new IllegalArgumentException("You are not authorized to delete this lesson");
        }

        lessonRepository.delete(lesson);
    }
}
