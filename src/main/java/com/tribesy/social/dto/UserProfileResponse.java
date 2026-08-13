package com.tribesy.social.dto;

import java.time.OffsetDateTime;

public record UserProfileResponse(
        Long id,
        String username,
        String email,
        String avatarUrl,
        String bio,
        OffsetDateTime createdAt
) {}