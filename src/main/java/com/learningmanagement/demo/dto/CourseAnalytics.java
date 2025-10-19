package com.learningmanagement.demo.dto;



import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseAnalytics {
    private Long courseId;
    private String courseTitle;
    private int enrollmentCount;
    private int completionCount;
    private double completionRate;
    private double averageProgress;
    private int lessonCount;
    private int quizCount;
    private int forumPostCount;
    private double averageQuizScore;
}
