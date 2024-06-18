package com.sports.server.auth.utils;

import com.sports.server.common.exception.UnauthorizedException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import org.springframework.http.ResponseCookie;

public final class CookieUtil {

    public static ResponseCookie createCookie(final String nameOfCookie, final String token,
                                              final Long cookieValidTime) {
        return ResponseCookie.from(nameOfCookie, token).path("/").sameSite("Strict")
                .secure(true).maxAge(Math.toIntExact(cookieValidTime)).httpOnly(true).build();
    }

    public static Optional<Cookie> getCookie(HttpServletRequest request, String nameOfCookie) {
        Cookie[] cookies = request.getCookies();

        if (cookies == null || cookies.length == 0) {
            throw new UnauthorizedException("제공된 토큰이 유효하지 않습니다. 다시 인증해주세요.");
        }

        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(nameOfCookie)) {
                return Optional.of(cookie);
            }
        }
        throw new UnauthorizedException("제공된 토큰이 유효하지 않습니다. 다시 인증해주세요.");
    }
}
