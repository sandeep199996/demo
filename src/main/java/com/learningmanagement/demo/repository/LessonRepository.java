package com.learningmanagement.demo.repository;

import com.learningmanagement.demo.entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, Long> {
    List<Lesson> findByCourseIdOrderByOrderNumberAsc(Long courseId);
    long countByCourseId(Long courseId);
}
