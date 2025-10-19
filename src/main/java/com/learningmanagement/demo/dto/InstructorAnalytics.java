package com.learningmanagement.demo.dto;



import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InstructorAnalytics {
    private Long instructorId;
    private String instructorName;
    private int totalCourses;
    private int publishedCourses;
    private int totalStudents;
    private int totalLessons;
    private int totalQuizzes;
    private double averageStudentProgress;
    private int forumPostsInCourses;
}
