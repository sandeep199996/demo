package com.learningmanagement.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ForumReplyRequest {

    @NotBlank(message = "Reply content is required")
    @Size(min = 5, max = 5000, message = "Reply must be between 5 and 5000 characters")
    private String content;
}
