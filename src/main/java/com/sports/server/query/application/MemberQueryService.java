package com.sports.server.query.application;

import com.sports.server.auth.exception.AuthorizationErrorMessages;
import com.sports.server.command.member.domain.Member;
import com.sports.server.command.member.domain.MemberRepository;
import com.sports.server.common.exception.NotFoundException;
import com.sports.server.query.dto.response.MemberResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberQueryService {

    private final MemberRepository memberRepository;

    public MemberResponse getMemberInfo(final Member member) {
        return memberRepository.findMemberByEmailWithOrganization(member.getEmail())
                .map(MemberResponse::new)
                .orElseThrow(() -> new NotFoundException(AuthorizationErrorMessages.MEMBER_NOT_FOUND_EXCEPTION));
    }
}
