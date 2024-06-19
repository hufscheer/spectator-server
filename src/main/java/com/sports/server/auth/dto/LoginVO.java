package com.sports.server.auth.dto;

public record LoginVO(
        String email,
        String password
) {
}
