package com.sports.server.common.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.util.UUID;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;

public final class VisitorIdResolver {

    public static final String COOKIE_NAME = "HCC_VID";

    private static final Duration COOKIE_MAX_AGE = Duration.ofDays(7);
    private static final String COOKIE_PATH = "/";
    private static final String SAME_SITE = "Lax";

    private VisitorIdResolver() {
    }

    public static String resolveOrIssue(HttpServletRequest request, HttpServletResponse response) {
        String existing = readCookie(request);
        if (existing != null) {
            return existing;
        }
        String issued = UUID.randomUUID().toString();
        response.addHeader(HttpHeaders.SET_COOKIE, buildCookie(issued, request.isSecure()));
        return issued;
    }

    private static String readCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (COOKIE_NAME.equals(cookie.getName()) && isUsable(cookie.getValue())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    private static boolean isUsable(String value) {
        return value != null && !value.isBlank();
    }

    private static String buildCookie(String value, boolean secure) {
        return ResponseCookie.from(COOKIE_NAME, value)
                .httpOnly(true)
                .secure(secure)
                .sameSite(SAME_SITE)
                .path(COOKIE_PATH)
                .maxAge(COOKIE_MAX_AGE)
                .build()
                .toString();
    }
}
