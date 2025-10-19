package com.learningmanagement.demo.service;

import com.learningmanagement.demo.entity.*;
import com.learningmanagement.demo.exception.ResourceNotFoundException;
import com.learningmanagement.demo.repository.ForumPostRepository;
import com.learningmanagement.demo.repository.ForumReplyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ForumService {

    private final ForumPostRepository postRepository;
    private final ForumReplyRepository replyRepository;
    private final CourseService courseService;
    private final NotificationService notificationService;
    private final SimpMessagingTemplate messagingTemplate;

    // Get posts for a course
    public List<ForumPost> getPostsByCourse(Long courseId) {
        return postRepository.findByCourseIdOrderByPinnedDescCreatedAtDesc(courseId);
    }

    // Get single post with replies
    public ForumPost getPostById(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Forum post not found"));
    }

    // Get replies for a post
    public List<ForumReply> getRepliesByPost(Long postId) {
        return replyRepository.findByPostIdOrderByCreatedAtAsc(postId);
    }

    // Create new forum post
    @Transactional
    public ForumPost createPost(Long courseId, String title, String content, User author) {
        Course course = courseService.getCourseById(courseId);

        ForumPost post = ForumPost.builder()
                .course(course)
                .author(author)
                .title(title)
                .content(content)
                .pinned(false)
                .locked(false)
                .build();

        ForumPost saved = postRepository.save(post);

        // Send real-time notification to course topic
        messagingTemplate.convertAndSend(
                "/topic/course/" + courseId + "/forum",
                saved
        );

        return saved;
    }

    // Create reply with real-time update
    @Transactional
    public ForumReply createReply(Long postId, String content, User author) {
        ForumPost post = getPostById(postId);

        if (post.isLocked()) {
            throw new IllegalArgumentException("This post is locked and cannot accept replies");
        }

        ForumReply reply = ForumReply.builder()
                .post(post)
                .author(author)
                .content(content)
                .build();

        ForumReply saved = replyRepository.save(reply);

        // Send real-time notification
        messagingTemplate.convertAndSend(
                "/topic/post/" + postId + "/replies",
                saved
        );

        // Notify post author about new reply
        if (!post.getAuthor().getId().equals(author.getId())) {
            notificationService.createNotification(
                    post.getAuthor(),
                    "New Reply",
                    author.getFullName() + " replied to your post: " + post.getTitle(),
                    NotificationType.FORUM_REPLY,
                    "/forum/post/" + postId
            );
        }

        return saved;
    }

    // Update post
    @Transactional
    public ForumPost updatePost(Long id, String title, String content, User author) {
        ForumPost post = getPostById(id);

        if (!post.getAuthor().getId().equals(author.getId())) {
            throw new IllegalArgumentException("You can only edit your own posts");
        }

        post.setTitle(title);
        post.setContent(content);

        return postRepository.save(post);
    }

    // Delete post
    @Transactional
    public void deletePost(Long id, User user) {
        ForumPost post = getPostById(id);

        // Only author or course instructor can delete
        boolean isAuthor = post.getAuthor().getId().equals(user.getId());
        boolean isInstructor = post.getCourse().getInstructor().getId().equals(user.getId());

        if (!isAuthor && !isInstructor) {
            throw new IllegalArgumentException("You don't have permission to delete this post");
        }

        postRepository.delete(post);
    }

    // Pin/Unpin post (instructor only)
    @Transactional
    public ForumPost togglePin(Long id, User instructor) {
        ForumPost post = getPostById(id);

        if (!post.getCourse().getInstructor().getId().equals(instructor.getId())) {
            throw new IllegalArgumentException("Only course instructor can pin posts");
        }

        post.setPinned(!post.isPinned());
        return postRepository.save(post);
    }

    // Lock/Unlock post (instructor only)
    @Transactional
    public ForumPost toggleLock(Long id, User instructor) {
        ForumPost post = getPostById(id);

        if (!post.getCourse().getInstructor().getId().equals(instructor.getId())) {
            throw new IllegalArgumentException("Only course instructor can lock posts");
        }

        post.setLocked(!post.isLocked());
        return postRepository.save(post);
    }

    // Get post count for course
    public long getPostCount(Long courseId) {
        return postRepository.countByCourseId(courseId);
    }

    // Get reply count for post
    public long getReplyCount(Long postId) {
        return replyRepository.countByPostId(postId);
    }
}
