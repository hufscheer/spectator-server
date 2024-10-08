package com.sports.server.query.repository;

import com.sports.server.command.league.domain.League;
import com.sports.server.command.member.domain.Member;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface LeagueQueryRepository extends Repository<League, Long>, LeagueQueryDynamicRepository {

    Optional<League> findById(Long id);

    @Query(
            "SELECT l FROM League l "
                    + "LEFT JOIN FETCH l.leagueTeams "
                    + "WHERE l.manager =:member "
                    + "ORDER BY l.startAt desc"
    )
    List<League> findByManager(Member member);

    @Query(
            "SELECT l FROM League l "
                    + "LEFT JOIN FETCH l.leagueTeams "
                    + "WHERE l.manager =:member"
    )
    List<League> findByManagerToManage(Member member);

    @Query(
            "SELECT l FROM League l "
                    + "JOIN FETCH l.leagueTeams "
                    + "WHERE l.id=:id"
    )
    Optional<League> findByIdWithLeagueTeam(@Param("id") Long id);
}
