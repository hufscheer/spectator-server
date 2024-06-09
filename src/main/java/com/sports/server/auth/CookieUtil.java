package com.sports.server.auth;

import jakarta.servlet.http.Cookie;
import org.springframework.stereotype.Component;

@Component
public final class CookieUtil {

    public static Cookie createCookie(final String token, final Long cookieValidTime) {
        Cookie cookie = new Cookie(JwtTokenProvider.ACCESS_TOKEN_HEADER_STRING, token);
        cookie.setMaxAge(Math.toIntExact(cookieValidTime));
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        return cookie;
    }
}
