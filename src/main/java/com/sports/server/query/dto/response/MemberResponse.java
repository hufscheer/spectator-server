package com.sports.server.query.dto.response;

import com.sports.server.command.member.domain.Member;

public record MemberResponse(
        String email,
        String nameOfOrganization
) {

    public MemberResponse(Member member) {
        this(
                member.getEmail(),
                member.getOrganization().getName()
        );
    }

}
