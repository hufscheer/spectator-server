package com.sports.server.auth.filter;

import com.sports.server.auth.JwtProvider;
import com.sports.server.auth.utils.CookieUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

@RequiredArgsConstructor
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;

    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        Cookie cookie = CookieUtil.getCookie(request, "HCC_SES").orElse(null);

        if (cookie == null) {
            chain.doFilter(request, response);
            return;
        }

        Authentication authentication = jwtProvider.getAuthentication(
                cookie.getValue());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        chain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
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

