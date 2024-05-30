package com.sports.server.command.member.domain;

import java.util.Optional;
import org.springframework.data.repository.Repository;

public interface MemberRepository extends Repository<Member, Long> {
    Optional<Member> findByEmail(final String email);
}
