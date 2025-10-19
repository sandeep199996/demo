package com.learningmanagement.demo.dto;


import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LessonRequest {

    @NotBlank(message = "Lesson title is required")
    @Size(max = 200, message = "Title cannot exceed 200 characters")
    private String title;

    @Size(max = 5000, message = "Content cannot exceed 5000 characters")
    private String content;

    private String videoUrl;

    @NotNull(message = "Order number is required")
    @Min(value = 1, message = "Order number must be at least 1")
    private Integer orderNumber;

    @Min(value = 0, message = "Duration must be positive")
    private Integer duration;
}
