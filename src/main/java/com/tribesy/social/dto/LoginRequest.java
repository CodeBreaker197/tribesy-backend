package com.tribesy.social.dto;

public record LoginRequest(
        String username,
        String password
) {}