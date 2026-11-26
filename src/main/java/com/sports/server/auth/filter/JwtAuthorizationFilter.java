package com.sports.server.auth.filter;

import com.sports.server.auth.application.AccessTokenRenewer;
import com.sports.server.auth.utils.CookieUtil;
import com.sports.server.auth.utils.JwtUtil;
import com.sports.server.common.exception.UnauthorizedException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final AuthenticationEntryPoint authEntryPoint;
    private final AccessTokenRenewer accessTokenRenewer;

    @Value("${cookie.name}")
    public String COOKIE_NAME;

    @Value("${cookie.valid-time}")
    private long COOKIE_VALID_TIME;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        try {
            Cookie cookie = CookieUtil.getCookie(request, COOKIE_NAME);

            if (cookie != null && authenticate(cookie)) {
                renewIfDue(cookie.getValue(), response);
            }

            chain.doFilter(request, response);
        } catch (UnauthorizedException e) {
            log.debug("JWT 인증 실패: {}", e.getMessage());
            SecurityContextHolder.clearContext();
            authEntryPoint.commence(request, response, new AuthenticationException(e.getMessage()) {
            });
        } catch (AuthenticationException e) {
            log.debug("인증 예외 발생: {}", e.getMessage());
            SecurityContextHolder.clearContext();
            authEntryPoint.commence(request, response, e);
        } catch (Exception e) {
            log.error("JWT 필터에서 예상치 못한 예외 발생", e);
            throw e;
        }
    }

    private boolean authenticate(Cookie cookie) {
        String accessToken = cookie.getValue();

        try {
            jwtUtil.validateToken(accessToken);
        } catch (UnauthorizedException e) {
            return false;
        }

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                jwtUtil.getEmail(accessToken),
                null,
                null
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        return true;
    }

    /** 응답이 커밋되기 전(chain 앞)에 쿠키를 붙인다. 재발급이 실패해도 요청은 그대로 처리한다 */
    private void renewIfDue(String accessToken, HttpServletResponse response) {
        try {
            accessTokenRenewer.renewIfDue(accessToken).ifPresent(renewedToken -> response.addHeader(
                    HttpHeaders.SET_COOKIE,
                    CookieUtil.createCookie(COOKIE_NAME, renewedToken, COOKIE_VALID_TIME).toString()));
        } catch (RuntimeException e) {
            log.warn("토큰 재발급 실패, 기존 토큰으로 계속 처리한다", e);
        }
    }
}

