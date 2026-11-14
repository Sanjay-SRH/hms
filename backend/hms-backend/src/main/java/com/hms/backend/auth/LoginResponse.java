package com.hms.backend.auth;

import java.util.List;

public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiresInSeconds,
        String username,
        List<String> roles,
        List<String> permissions) {
}