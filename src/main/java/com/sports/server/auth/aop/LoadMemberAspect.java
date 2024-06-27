package com.sports.server.auth.aop;

import com.sports.server.auth.exception.AuthorizationErrorMessages;
import com.sports.server.command.member.domain.Member;
import com.sports.server.command.member.domain.MemberRepository;
import com.sports.server.common.exception.UnauthorizedException;
import java.lang.reflect.Parameter;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class LoadMemberAspect {
    private static final String PARAMETER_MEMBER = "member";

    private final MemberRepository memberRepository;

    @Around("@annotation(loadMember)")
    public Object authenticate(ProceedingJoinPoint proceedingJoinPoint, LoadMember loadMember)
            throws Throwable {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            throw new UnauthorizedException(AuthorizationErrorMessages.INVALID_COOKIE_EXCEPTION);
        }

        Member member = memberRepository.findMemberByEmail(auth.getName()).orElseThrow(() ->
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