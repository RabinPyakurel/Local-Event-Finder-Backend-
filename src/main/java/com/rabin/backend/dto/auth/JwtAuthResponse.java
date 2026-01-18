package com.rabin.backend.dto.auth;

import java.util.Set;

public record JwtAuthResponse(
        String token,
        String tokenType,
        Long userId,
        String email,
        Set<String> roles
) {}
