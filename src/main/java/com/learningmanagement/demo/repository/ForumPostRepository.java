package com.learningmanagement.demo.repository;




import com.learningmanagement.demo.entity.ForumPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ForumPostRepository extends JpaRepository<ForumPost, Long> {
    List<ForumPost> findByCourseIdOrderByPinnedDescCreatedAtDesc(Long courseId);
    List<ForumPost> findByAuthorIdOrderByCreatedAtDesc(Long authorId);
    long countByCourseId(Long courseId);
}
