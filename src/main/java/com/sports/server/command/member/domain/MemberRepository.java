package com.sports.server.command.member.domain;

import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

public interface MemberRepository extends Repository<Member, Long> {
    Optional<Member> findMemberByEmail(String email);

    @Query(
            "SELECT m FROM Member m "
                    + "JOIN FETCH m.organization o "
                    + "WHERE m.email = :email"
    )
    Optional<Member> findMemberByEmailWithOrganization(String email);
}
