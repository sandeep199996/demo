package com.learningmanagement.demo.service;


import com.learningmanagement.demo.dto.CourseResponse;
import com.learningmanagement.demo.entity.Course;
import com.learningmanagement.demo.repository.CourseRepository;
import com.learningmanagement.demo.repository.EnrollmentRepository;
import com.learningmanagement.demo.repository.ForumPostRepository;
import com.learningmanagement.demo.repository.LessonRepository;
import com.learningmanagement.demo.specification.CourseSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final LessonRepository lessonRepository;
    private final ForumPostRepository forumPostRepository;

    public Page<CourseResponse> searchCourses(
            String keyword,
            Long categoryId,
            String difficulty,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Boolean published,
            Pageable pageable) {

        Specification<Course> spec = CourseSpecification.filterCourses(
                keyword, categoryId, difficulty, minPrice, maxPrice, published
        );

        return courseRepository.findAll(spec, pageable)
                .map(this::convertToResponse);
    }

    public List<CourseResponse> getRecommendedCourses(Long userId) {
        // Simple recommendation: return recent published courses
        // Can be enhanced with ML algorithms
        return courseRepository.findRecentPublishedCourses()
                .stream()
                .limit(10)
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public List<CourseResponse> getTrendingCourses() {
        // Return top rated courses
        return courseRepository.findTopRatedCourses()
                .stream()
                .limit(10)
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // Convert Course entity to CourseResponse DTO
    private CourseResponse convertToResponse(Course course) {
        return CourseResponse.builder()
                .id(course.getId())
                .title(course.getTitle())
                .description(course.getDescription())
                .price(course.getPrice())
                .difficulty(course.getDifficulty())
                .thumbnailUrl(course.getThumbnailUrl())
                .duration(course.getDuration())
                .published(course.isPublished())
                .createdAt(course.getCreatedAt())
                .instructorId(course.getInstructor().getId())
                .instructorName(course.getInstructor().getFullName())
                .categoryId(course.getCategory() != null ? course.getCategory().getId() : null)
                .categoryName(course.getCategory() != null ? course.getCategory().getName() : null)
                .enrollmentCount((int) enrollmentRepository.countByCourseId(course.getId()))
                .lessonCount((int) lessonRepository.countByCourseId(course.getId()))
                .forumPostCount(forumPostRepository.countByCourseId(course.getId()))
                .build();
    }
}
