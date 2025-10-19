package com.learningmanagement.demo.dto;



import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlatformStatistics {
    private long totalCourses;
    private long totalUsers;
    private long totalEnrollments;
    private long totalLessons;
    private long totalQuizzes;
    private long totalForumPosts;
}
