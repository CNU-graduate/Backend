package com.abc.behaviortracker.auth.dto;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        long expiresIn,
        TeacherDto teacher
) {}
