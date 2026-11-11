package com.sports.server.auth.resolver;

import com.sports.server.command.member.domain.Member;
import com.sports.server.command.member.domain.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.core.ResolvableType;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.Optional;

/**
 * 공개 경로에서 로그인이 선택인 컨트롤러용 {@code Optional<Member>} 파라미터를 채운다. 인증이 없으면 비어 있는 값을 준다.
 */
@RequiredArgsConstructor
public class OptionalAuthMemberResolver implements HandlerMethodArgumentResolver {

    private final MemberRepository memberRepository;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return Optional.class.equals(parameter.getParameterType())
                && Member.class.equals(ResolvableType.forMethodParameter(parameter).getGeneric(0).resolve());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth instanceof AnonymousAuthenticationToken || !auth.isAuthenticated()) {
            return Optional.empty();
        }
        return memberRepository.findMemberByEmailWithOrganization(auth.getName());
    }
}
