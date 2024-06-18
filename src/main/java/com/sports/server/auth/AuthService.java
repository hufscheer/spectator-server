package com.sports.server.auth;

import com.sports.server.command.member.domain.Member;
import com.sports.server.command.member.domain.MemberRepository;
import com.sports.server.common.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final MemberRepository memberRepository;
    private final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;

    public JwtResponse managerLogin(final LoginVO loginVO) {
        Member member = memberRepository.findMemberByEmail(loginVO.email());
        if (member == null) {
            throw new NotFoundException(AuthorizationErrorMessages.MEMBER_NOT_FOUND_EXCEPTION);
        }

        if (!passwordEncoder.matches(loginVO.password(), member.getPassword())) {
            throw new NotFoundException(AuthorizationErrorMessages.MEMBER_NOT_FOUND_EXCEPTION);
        }

        if (!member.isManager()) {
            throw new UnauthorizedException(AuthorizationErrorMessages.PERMISSION_DENIED);
        }

        return new JwtResponse(jwtProvider.createAccessToken(member));
    }
}
