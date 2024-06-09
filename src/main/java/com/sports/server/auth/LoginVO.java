package com.sports.server.auth;

public record LoginVO(
        String email,
        String password
) {
}
