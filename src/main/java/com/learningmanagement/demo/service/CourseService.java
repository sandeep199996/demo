package com.learningmanagement.demo.service;



import com.learningmanagement.demo.dto.CourseRequest;
import com.learningmanagement.demo.dto.CourseResponse;
import com.learningmanagement.demo.entity.Category;
import com.learningmanagement.demo.entity.Course;
import com.learningmanagement.demo.entity.User;
import com.learningmanagement.demo.exception.ResourceNotFoundException;
import com.learningmanagement.demo.repository.CourseRepository;
import com.learningmanagement.demo.repository.EnrollmentRepository;
import com.learningmanagement.demo.repository.ForumPostRepository;
import com.learningmanagement.demo.repository.LessonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Caching;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final CategoryService categoryService;
    private final EnrollmentRepository enrollmentRepository;
    private final LessonRepository lessonRepository;
    private final ForumPostRepository forumPostRepository;


    // Get all published courses
    // Cache all published courses
    @Cacheable(value = "publishedCourses")
    public List<CourseResponse> getAllPublishedCourses() {
        System.out.println("Fetching all published courses from database");
        return courseRepository.findByPublishedTrue()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // Get all courses (admin only)
    public List<CourseResponse> getAllCourses() {
        return courseRepository.findAll()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // Get courses with pagination and sorting
    public Page<CourseResponse> getCoursesPaginated(Pageable pageable) {
        return courseRepository.findAll(pageable)
                .map(this::convertToResponse);
    }

    // Get course by ID
    public Course getCourseById(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));
    }

    // Get course response by ID
    // Cache single course by ID
    @Cacheable(value = "course", key = "#id")
    public CourseResponse getCourseResponseById(Long id) {
        System.out.println("Fetching course from database with id: " + id);
        Course course = getCourseById(id);
        return convertToResponse(course);
    }

    // Get courses by category
    // Cache courses by category - cache name: "courses", key: categoryId
    @Cacheable(value = "courses", key = "#categoryId")
    public List<CourseResponse> getCoursesByCategory(Long categoryId) {
        System.out.println("Fetching courses from database for category: " + categoryId);
        return courseRepository.findByCategoryId(categoryId)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // Get courses by instructor
    public List<CourseResponse> getCoursesByInstructor(Long instructorId) {
        return courseRepository.findByInstructorId(instructorId)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // Search courses by title
    public List<CourseResponse> searchCourses(String keyword) {
        return courseRepository.findByTitleContainingIgnoreCase(keyword)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // Create new course
    // Update cache when creating course, clear all courses cache
    @CacheEvict(value = {"courses", "publishedCourses"}, allEntries = true)
    @Transactional
    public CourseResponse createCourse(CourseRequest request, User instructor) {
        System.out.println("Creating new course and clearing cache");
        Category category = categoryService.getCategoryById(request.getCategoryId());

        Course course = Course.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .instructor(instructor)
                .category(category)
                .price(request.getPrice())
                .difficulty(request.getDifficulty())
                .thumbnailUrl(request.getThumbnailUrl())
                .published(false) // Initially unpublished
                .build();

        Course savedCourse = courseRepository.save(course);
        return convertToResponse(savedCourse);
    }
    // Update specific course in cache and evict related caches
    @Caching(
            put = { @CachePut(value = "course", key = "#id") },
            evict = {
                    @CacheEvict(value = "courses", allEntries = true),
                    @CacheEvict(value = "publishedCourses", allEntries = true)
            }
    )
    // Update course
    @Transactional
    public CourseResponse updateCourse(Long id, CourseRequest request, User instructor) {
        Course course = getCourseById(id);

        // Check if the instructor owns this course
        if (!course.getInstructor().getId().equals(instructor.getId())) {
            throw new IllegalArgumentException("You are not authorized to update this course");
        }

        Category category = categoryService.getCategoryById(request.getCategoryId());

        course.setTitle(request.getTitle());
        course.setDescription(request.getDescription());
        course.setCategory(category);
        course.setPrice(request.getPrice());
        course.setDifficulty(request.getDifficulty());
        course.setThumbnailUrl(request.getThumbnailUrl());

        Course updatedCourse = courseRepository.save(course);
        return convertToResponse(updatedCourse);
    }

    // Publish/unpublish course
    @Transactional
    public CourseResponse togglePublishStatus(Long id, User instructor) {
        Course course = getCourseById(id);

        // Check if the instructor owns this course
        if (!course.getInstructor().getId().equals(instructor.getId())) {
            throw new IllegalArgumentException("You are not authorized to publish/unpublish this course");
        }

        course.setPublished(!course.isPublished());
        Course updatedCourse = courseRepository.save(course);
        return convertToResponse(updatedCourse);
    }

    // Delete course
    // Clear all cache entries when deleting
    @CacheEvict(value = {"course", "courses", "publishedCourses"}, allEntries = true)
    @Transactional
    public void deleteCourse(Long id, User instructor) {
        System.out.println("Deleting course and clearing all caches");
        Course course = getCourseById(id);

        // Check if the instructor owns this course
        if (!course.getInstructor().getId().equals(instructor.getId())) {
            throw new IllegalArgumentException("You are not authorized to delete this course");
        }

        courseRepository.delete(course);
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
