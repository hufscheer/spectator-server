package com.sports.server.auth.filter;

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

    @Value("${cookie.name}")
    public String COOKIE_NAME;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        try {
            Cookie cookie = CookieUtil.getCookie(request, COOKIE_NAME);

            if (cookie != null) {
                authenticate(cookie);
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

    private void authenticate(Cookie cookie) {
        String accessToken = cookie.getValue();

        try {
            jwtUtil.validateToken(accessToken);
        } catch (UnauthorizedException e) {
            return;
        }

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                jwtUtil.getEmail(accessToken),
                null,
                null
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}

