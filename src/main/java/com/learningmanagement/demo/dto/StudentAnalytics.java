package com.learningmanagement.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentAnalytics {
    private Long studentId;
    private String studentName;
    private int totalEnrollments;
    private int completedCourses;
    private int inProgressCourses;
    private double averageProgress;
    private int totalQuizAttempts;
    private double averageQuizScore;
    private int forumPostsCount;
    private int certificatesEarned;
}
