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
import java.util.Map;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final AuthenticationEntryPoint authEntryPoint;
    private final static Map<Pattern, String> authenticatedEndpointPatterns = Map.of(
            Pattern.compile("/leagues/\\d+/teams"), "POST",
        Pattern.compile("/leagues"), "POST"
    );

    @Value("${cookie.name}")
    public String COOKIE_NAME;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        try {
            Cookie cookie = CookieUtil.getCookie(request, COOKIE_NAME);

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
        String method = request.getMethod();

        if (path.contains("/manager/login")) {
            return true;
        } else if (path.contains("/manager")) {
            return false;
        } else {
            for (Map.Entry<Pattern, String> entry : authenticatedEndpointPatterns.entrySet()) {
                if (entry.getKey().matcher(path).matches() && entry.getValue().equalsIgnoreCase(method)) {
                    return false;
                }
            }
            return true;
        }
    }
}

