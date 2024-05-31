package com.sports.server.query.auth;

import com.sports.server.command.member.domain.Member;
import com.sports.server.command.member.domain.MemberRepository;
import com.sports.server.common.exception.NotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public String login(final LoginVO loginVO) {
        Member member = findMember(loginVO.email());
        if (!passwordEncoder.matches(loginVO.password(), member.getPassword())) {
            throw new NotFoundException("존재하지 않는 회원입니다.");
        }
        return jwtTokenProvider.createAccessToken(member);
    }

    private Member findMember(final String email) {
        return memberRepository.findByEmail(email).orElseThrow(() -> new NotFoundException("존재하지 않는 회원입니다."));
    }
}
