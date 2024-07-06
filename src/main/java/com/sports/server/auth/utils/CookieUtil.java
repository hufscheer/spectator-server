package com.sports.server.auth.utils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseCookie;

public final class CookieUtil {

    public static ResponseCookie createCookie(final String nameOfCookie, final String token,
                                              final Long cookieValidTime) {
        return ResponseCookie.from(nameOfCookie, token)
                .path("/")
                .sameSite("Strict")
                .secure(true)
                .maxAge(Math.toIntExact(cookieValidTime))
                .httpOnly(true)
                .build();
    }

    public static Cookie getCookie(HttpServletRequest request, String nameOfCookie) {
        Cookie[] cookies = request.getCookies();

        if (cookies == null || cookies.length == 0) {
            return null;
        }

        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(nameOfCookie)) {
                return cookie;
            }
        }
        return null;
    }
}
