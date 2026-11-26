package com.sports.server.auth.application;

import com.sports.server.auth.utils.JwtUtil;
import com.sports.server.command.member.domain.Member;
import com.sports.server.command.member.domain.MemberRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 활동 중인 매니저의 로그인이 경기 도중 끊기지 않게, 만료가 가까운 토큰을 새 토큰으로 바꿔 준다.
 * 로그인과 같은 조건(관리자 계정)을 다시 확인하고, 처음 로그인 시각은 그대로 넘긴다.
 */
@Component
@RequiredArgsConstructor
public class AccessTokenRenewer {

    private final JwtUtil jwtUtil;
    private final MemberRepository memberRepository;

    /** 검증을 통과한 토큰만 넘긴다. 재발급할 때가 아니면 비어 있다 */
    public Optional<String> renewIfDue(String accessToken) {
        Optional<JwtUtil.Renewal> renewal = jwtUtil.planRenewal(accessToken);
        if (renewal.isEmpty()) {
            return Optional.empty();
        }

        return memberRepository.findMemberByEmail(jwtUtil.getEmail(accessToken))
                .filter(Member::isAdministrator)
                .map(member -> jwtUtil.createAccessToken(
                        member, renewal.get().authTime(), renewal.get().expiresAt()));
    }
}
