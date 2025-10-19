package com.learningmanagement.demo.controller;



import com.learningmanagement.demo.dto.ForumPostRequest;
import com.learningmanagement.demo.dto.ForumReplyRequest;
import com.learningmanagement.demo.entity.ForumPost;
import com.learningmanagement.demo.entity.ForumReply;
import com.learningmanagement.demo.security.CustomUserDetails;
import com.learningmanagement.demo.service.ForumService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/forum")
@RequiredArgsConstructor
public class ForumController {

    private final ForumService forumService;

    // Get all posts for a course
    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<ForumPost>> getCourseForumPosts(@PathVariable Long courseId) {
        return ResponseEntity.ok(forumService.getPostsByCourse(courseId));
    }

    // Get single post with details
    @GetMapping("/post/{id}")
    public ResponseEntity<ForumPost> getPost(@PathVariable Long id) {
        return ResponseEntity.ok(forumService.getPostById(id));
    }

    // Get replies for a post
    @GetMapping("/post/{postId}/replies")
    public ResponseEntity<List<ForumReply>> getReplies(@PathVariable Long postId) {
        return ResponseEntity.ok(forumService.getRepliesByPost(postId));
    }

    // Create new post
    @PostMapping("/course/{courseId}/post")
    public ResponseEntity<ForumPost> createPost(
            @PathVariable Long courseId,
            @Valid @RequestBody ForumPostRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        ForumPost post = forumService.createPost(
                courseId,
                request.getTitle(),
                request.getContent(),
                userDetails.getUser()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(post);
    }

    // Create reply
    @PostMapping("/post/{postId}/reply")
    public ResponseEntity<ForumReply> createReply(
            @PathVariable Long postId,
            @Valid @RequestBody ForumReplyRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        ForumReply reply = forumService.createReply(
                postId,
                request.getContent(),
                userDetails.getUser()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(reply);
    }

    // Update post
    @PutMapping("/post/{id}")
    public ResponseEntity<ForumPost> updatePost(
            @PathVariable Long id,
            @Valid @RequestBody ForumPostRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        ForumPost updated = forumService.updatePost(
                id,
                request.getTitle(),
                request.getContent(),
                userDetails.getUser()
        );

        return ResponseEntity.ok(updated);
    }

    // Delete post
    @DeleteMapping("/post/{id}")
    public ResponseEntity<Void> deletePost(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        forumService.deletePost(id, userDetails.getUser());
        return ResponseEntity.noContent().build();
    }

    // Pin/Unpin post (instructor only)
    @PatchMapping("/post/{id}/pin")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<ForumPost> togglePin(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        ForumPost post = forumService.togglePin(id, userDetails.getUser());
        return ResponseEntity.ok(post);
    }

    // Lock/Unlock post (instructor only)
    @PatchMapping("/post/{id}/lock")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<ForumPost> toggleLock(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        ForumPost post = forumService.toggleLock(id, userDetails.getUser());
        return ResponseEntity.ok(post);
    }

    // WebSocket message handling for real-time replies
    @MessageMapping("/forum/post/{postId}/reply")
    @SendTo("/topic/post/{postId}/replies")
    public ForumReply handleReply(@DestinationVariable Long postId, ForumReply reply) {
        return reply;
    }
}
