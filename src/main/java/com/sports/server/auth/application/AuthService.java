package com.sports.server.auth.application;

import com.sports.server.auth.dto.LoginRequest;
import com.sports.server.auth.dto.LoginResponse;
import com.sports.server.auth.exception.AuthorizationErrorMessages;
import com.sports.server.auth.utils.JwtUtil;
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
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public LoginResponse loginByManager(final LoginRequest loginRequest) {
        Member member = memberRepository.findMemberByEmail(loginRequest.email())
                .filter(m -> passwordEncoder.matches(loginRequest.password(), m.getPassword()))
                .orElseThrow(() -> new NotFoundException(AuthorizationErrorMessages.MEMBER_NOT_FOUND_EXCEPTION));

        if (!member.isAdministrator()) {
            throw new UnauthorizedException(AuthorizationErrorMessages.PERMISSION_DENIED);
        }

        return new LoginResponse(jwtUtil.createAccessToken(member));
    }
}
