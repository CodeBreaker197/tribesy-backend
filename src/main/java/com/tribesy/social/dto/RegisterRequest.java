package com.tribesy.social.dto;

import com.tribesy.social.entity.Role;

public record RegisterRequest(
        String username,
        String email,
        String password,
        String avatarUrl,
        String bio,
        Role role
) {}