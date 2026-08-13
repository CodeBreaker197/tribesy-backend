package com.tribesy.social.dto;

public record UpdateProfileRequest(
        String avatarUrl,
        String bio
) {}