package com.learningmanagement.demo.repository;



import com.learningmanagement.demo.entity.ForumReply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ForumReplyRepository extends JpaRepository<ForumReply, Long> {
    List<ForumReply> findByPostIdOrderByCreatedAtAsc(Long postId);
    long countByPostId(Long postId);
}
