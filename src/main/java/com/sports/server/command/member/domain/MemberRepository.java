package com.sports.server.command.member.domain;

import org.springframework.data.repository.Repository;

public interface MemberRepository extends Repository<Member, Long> {
    Member findByEmail(final String email);
}