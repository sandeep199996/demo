package com.learningmanagement.demo.repository;

import com.learningmanagement.demo.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long>,
        JpaSpecificationExecutor<Course> {
    List<Course> findByCategoryId(Long categoryId);
    List<Course> findByInstructorId(Long instructorId);
    List<Course> findByTitleContainingIgnoreCase(String keyword);
    List<Course> findByPublishedTrue();

    @Query("SELECT c FROM Course c WHERE c.published = true ORDER BY c.createdAt DESC")
    List<Course> findRecentPublishedCourses();
    // Top-rated courses (placeholder - can be enhanced with ratings system)
    @Query("SELECT c FROM Course c WHERE c.published = true ORDER BY c.id DESC")
    List<Course> findTopRatedCourses();
}
