package com.sports.server.auth;

import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public final class CookieUtil {

    public static ResponseCookie createCookie(final String token, final Long cookieValidTime) {
        return ResponseCookie.from(JwtTokenProvider.ACCESS_TOKEN_HEADER_STRING, token).path("/").sameSite("Strict")
                .secure(true).maxAge(Math.toIntExact(cookieValidTime)).httpOnly(true).build();
    }
}
