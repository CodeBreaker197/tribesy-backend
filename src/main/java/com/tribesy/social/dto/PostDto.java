package com.tribesy.social.dto;

import java.time.LocalDateTime;

public class PostDto {

    public record CreatePostRequest(String content) {}

    public record PostResponse(
            Long id,
            String content,
            String authorUsername,
            LocalDateTime createdAt
    ) {}
}