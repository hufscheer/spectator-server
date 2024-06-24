package com.sports.server.auth.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.sports.server.auth.exception.AuthorizationErrorMessages;
import com.sports.server.common.exception.UnauthorizedException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseCookie;

@ExtendWith(MockitoExtension.class)
public class CookieUtilTest {

    private static final String COOKIE_NAME = "testCookie";
    private static final String TOKEN = "testToken";
    private static final long COOKIE_VALID_TIME = 3600L;

    @Test
    public void 정상적으로_쿠키가_생성된다() {
        ResponseCookie cookie = CookieUtil.createCookie(COOKIE_NAME, TOKEN, COOKIE_VALID_TIME);

        assertNotNull(cookie);
        assertEquals(COOKIE_NAME, cookie.getName());
        assertEquals(TOKEN, cookie.getValue());
        assertEquals("/", cookie.getPath());
        assertEquals("Strict", cookie.getSameSite());
        assertTrue(cookie.isSecure());
        assertEquals(Math.toIntExact(COOKIE_VALID_TIME), cookie.getMaxAge().getSeconds());
        assertTrue(cookie.isHttpOnly());
    }

    @Test
    public void 쿠키가_존재하는_경우_성공적으로_꺼내온다() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        Cookie[] cookies = {new Cookie(COOKIE_NAME, TOKEN)};
        when(request.getCookies()).thenReturn(cookies);

        Optional<Cookie> cookie = CookieUtil.getCookie(request, COOKIE_NAME);

        assertTrue(cookie.isPresent());
        assertEquals(COOKIE_NAME, cookie.get().getName());
        assertEquals(TOKEN, cookie.get().getValue());
    }

    @Test
    public void 쿠키가_존재하지_않는_경우_예외를_던진다() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getCookies()).thenReturn(null);

        UnauthorizedException thrown = assertThrows(UnauthorizedException.class, () -> {
            CookieUtil.getCookie(request, COOKIE_NAME);
        });

        assertEquals(AuthorizationErrorMessages.INVALID_COOKIE_EXCEPTION, thrown.getMessage());
    }

    @Test
    public void 요청에_해당하는_쿠키가_없는_경우_예외를_던진다() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        Cookie[] cookies = {new Cookie("anotherCookie", TOKEN)};
        when(request.getCookies()).thenReturn(cookies);

        UnauthorizedException thrown = assertThrows(UnauthorizedException.class, () -> {
            CookieUtil.getCookie(request, COOKIE_NAME);
        });

        assertEquals(AuthorizationErrorMessages.INVALID_TOKEN_EXCEPTION, thrown.getMessage());
    }
}
