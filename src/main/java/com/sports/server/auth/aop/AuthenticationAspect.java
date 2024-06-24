package com.sports.server.auth.aop;

import com.sports.server.auth.exception.AuthorizationErrorMessages;
import com.sports.server.auth.utils.CookieUtil;
import com.sports.server.auth.utils.JwtUtil;
import com.sports.server.command.member.domain.Member;
import com.sports.server.command.member.domain.MemberRepository;
import com.sports.server.common.exception.UnauthorizedException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Parameter;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
@RequiredArgsConstructor
public class AuthenticationAspect {
    private static final String PARAMETER_MEMBER = "member";

    private final MemberRepository memberRepository;
    private final JwtUtil jwtUtil;

    @Around("@annotation(authentication)")
    public Object authenticate(ProceedingJoinPoint proceedingJoinPoint, Authentication authentication)
            throws Throwable {

        ServletRequestAttributes requestAttributes =
                (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = requestAttributes.getRequest();

        Cookie cookie = CookieUtil.getCookie(request, "HCC_SES").orElseThrow(() ->
                new UnauthorizedException(AuthorizationErrorMessages.PERMISSION_DENIED));
        String accessToken = cookie.getValue();

        jwtUtil.validateToken(accessToken);
        String email = jwtUtil.getEmail(accessToken);
        Member member = memberRepository.findMemberByEmail(email).orElseThrow(() ->
                new UnauthorizedException(AuthorizationErrorMessages.MEMBER_NOT_FOUND_EXCEPTION));

        Object[] modifiedArgs = modifyArgsWithMember(member, proceedingJoinPoint.getArgs(),
                ((MethodSignature) proceedingJoinPoint.getSignature()).getMethod().getParameters());

        return proceedingJoinPoint.proceed(modifiedArgs);
    }

    private Object[] modifyArgsWithMember(Member member, Object[] args, Parameter[] parameters) {
        for (int i = 0; i < parameters.length; i++) {
            if (PARAMETER_MEMBER.equals(parameters[i].getName())) {
                args[i] = member;
            }
        }
        return args;
    }
}