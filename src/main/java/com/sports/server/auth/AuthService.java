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
            throw new NotFoundException("존재하지 않는 사용자입니다.");
        }

        if (!passwordEncoder.matches(loginVO.password(), member.getPassword())) {
            throw new NotFoundException("유효하지 않은 사용자입니다.");
        }

        if (!member.isManager()) {
            throw new IllegalStateException("권한이 없습니다.");
        }

        return new JwtResponse(jwtProvider.createAccessToken(member));
    }
}
