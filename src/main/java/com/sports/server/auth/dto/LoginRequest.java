package com.sports.server.auth.dto;

public record LoginRequest(
        String email,
        String password
) {
}
