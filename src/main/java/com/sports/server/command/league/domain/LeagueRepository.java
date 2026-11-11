package com.sports.server.command.league.domain;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;


public interface LeagueRepository extends Repository<League, Integer> {
    League save(League league);

    void delete(League league);

    // 삭제된 대회는 @SQLRestriction 으로 빠진다
    @Query("SELECT COUNT(l) > 0 FROM League l WHERE l.administrator.id = :memberId AND l.organization.id = :organizationId")
    boolean existsByAdministratorAndOrganization(@Param("memberId") Long memberId,
                                                 @Param("organizationId") Long organizationId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT l FROM League l WHERE l.id = :id")
    Optional<League> findByIdForUpdate(@Param("id") Long id);
}
