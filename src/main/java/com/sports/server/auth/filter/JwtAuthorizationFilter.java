package com.sports.server.auth.filter;

import com.sports.server.auth.details.MemberDetails;
import com.sports.server.auth.exception.AuthorizationErrorMessages;
import com.sports.server.auth.utils.CookieUtil;
import com.sports.server.auth.utils.JwtUtil;
import com.sports.server.command.member.domain.Member;
import com.sports.server.common.exception.UnauthorizedException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

@RequiredArgsConstructor
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Value("${cookie.name}")
    public String COOKIE_NAME;

    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        CookieUtil.getCookie(request, "HCC_SES")
                .ifPresentOrElse(
                        cookie -> {
                            jwtUtil.validateToken(cookie.getValue());

                            // 임시로 Authentication 객체 등록
                            MemberDetails memberDetails = new MemberDetails(new Member("temp", "temp"));
                            Authentication authentication = new UsernamePasswordAuthenticationToken(
                                    memberDetails,
                                    null,
                                    memberDetails.getAuthorities()
                            );
                            SecurityContextHolder.getContext().setAuthentication(authentication);
                        },
                        () -> {
                            throw new UnauthorizedException(AuthorizationErrorMessages.PERMISSION_DENIED);
                        }
                );

        chain.doFilter(request, response);
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

