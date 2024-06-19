package com.sports.server.auth.application;

import com.sports.server.auth.jwt.JwtProvider;
import com.sports.server.auth.dto.JwtResponse;
import com.sports.server.auth.dto.LoginVO;
import com.sports.server.auth.exception.AuthorizationErrorMessages;
import com.sports.server.command.member.domain.Member;
import com.sports.server.command.member.domain.MemberRepository;
import com.sports.server.common.exception.NotFoundException;
import com.sports.server.common.exception.UnauthorizedException;
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
        if (member == null || !passwordEncoder.matches(loginVO.password(), member.getPassword())) {
            throw new NotFoundException(AuthorizationErrorMessages.MEMBER_NOT_FOUND_EXCEPTION);
        }

        if (!member.isManager()) {
            throw new UnauthorizedException(AuthorizationErrorMessages.PERMISSION_DENIED);
        }

        return new JwtResponse(jwtProvider.createAccessToken(member));
    }

    public void save(final String email) {
        memberRepository.save(new Member(email, passwordEncoder.encode("1234"), true));
    }
}
