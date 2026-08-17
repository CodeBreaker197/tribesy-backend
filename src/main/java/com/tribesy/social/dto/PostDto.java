package com.tribesy.social.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public class PostDto {

    public record CreatePostRequest(
            @NotBlank(message = "Post content cannot be empty")
            @Size(max = 2800, message = "Post content is too long")
            String content
    ) {}

    public record PostResponse(
            Long id,
            String content,
            String authorUsername,
            LocalDateTime createdAt
    ) {}
}