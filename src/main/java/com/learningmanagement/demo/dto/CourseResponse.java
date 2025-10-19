package com.learningmanagement.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.io.Serializable;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseResponse implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private Long id;
    private String title;
    private String description;
    private BigDecimal price;
    private String difficulty;
    private String thumbnailUrl;
    private int duration;
    private boolean published;
    private LocalDateTime createdAt;
    private long forumPostCount;

    // Instructor information (limited to prevent circular references)
    private Long instructorId;
    private String instructorName;

    // Category information
    private Long categoryId;
    private String categoryName;

    // Statistics
    private int enrollmentCount;
    private int lessonCount;
}
