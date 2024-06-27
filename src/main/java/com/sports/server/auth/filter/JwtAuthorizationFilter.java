package com.sports.server.auth.filter;

import com.sports.server.auth.exception.AuthorizationErrorMessages;
import com.sports.server.auth.utils.CookieUtil;
import com.sports.server.auth.utils.JwtUtil;
import com.sports.server.common.exception.UnauthorizedException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.web.filter.OncePerRequestFilter;

@RequiredArgsConstructor
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final AuthenticationEntryPoint authEntryPoint;

    @Value("${cookie.name}")
    public String COOKIE_NAME;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        try {
            Cookie cookie = CookieUtil.getCookie(request, "HCC_SES");

            if (cookie == null) {
                throw new UnauthorizedException(AuthorizationErrorMessages.PERMISSION_DENIED);
            }

            String accessToken = cookie.getValue();
            jwtUtil.validateToken(accessToken);
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    jwtUtil.getEmail(accessToken),
                    null,
                    null
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            chain.doFilter(request, response);
        } catch (Exception e) {
            SecurityContextHolder.clearContext();
            authEntryPoint.commence(request, response, new AuthenticationException(e.getMessage()) {
            });
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        if (path.contains("/manager/login")) {
            return true;
        } else if (path.contains("/manager")) {
            return false;
        } else {
            return true;
        }
    }
}

