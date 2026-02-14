package com.sports.server.query.repository;

import com.sports.server.command.league.domain.League;
import com.sports.server.command.member.domain.Member;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface LeagueQueryRepository extends Repository<League, Long>, LeagueQueryDynamicRepository {

    Optional<League> findById(Long id);

    @Query(
            "SELECT l FROM League l "
                    + "LEFT JOIN FETCH l.leagueTeams "
                    + "WHERE l.administrator =:member "
                    + "ORDER BY l.startAt desc"
    )
    List<League> findByManager(Member member);

    @Query(
            "SELECT l FROM League l "
                    + "LEFT JOIN FETCH l.leagueTeams "
                    + "WHERE l.administrator =:member"
    )
    List<League> findByManagerToManage(Member member);

    @Query(
            "SELECT l FROM League l "
                    + "JOIN FETCH l.leagueTeams "
                    + "WHERE l.id=:id"
    )
    Optional<League> findByIdWithLeagueTeam(@Param("id") Long id);

    @Query(
            "SELECT YEAR(l.startAt) "
                    + "FROM League l "
                    + "JOIN LeagueStatistics ls ON ls.league = l "
                    + "WHERE l.endAt < :now "
                    + "AND ls.firstWinnerTeam IS NOT NULL "
                    + "ORDER BY l.startAt DESC, l.id DESC"
    )
    List<Integer> findRecentFinishedLeagueYears(
            @Param("now") LocalDateTime now,
            Pageable pageable
    );

    @Query(
            "SELECT new com.sports.server.query.repository.LeagueRecentRecordResult(l.id, l.name, ls.firstWinnerTeam.name) "
                    + "FROM League l "
                    + "JOIN LeagueStatistics ls ON ls.league = l "
                    + "WHERE l.startAt >= :yearStart "
                    + "AND l.startAt < :yearEnd "
                    + "AND l.endAt < :now "
                    + "AND ls.firstWinnerTeam IS NOT NULL "
                    + "ORDER BY l.startAt DESC, l.id DESC"
    )
    List<LeagueRecentRecordResult> findRecentFinishedLeagues(
            @Param("yearStart") LocalDateTime yearStart,
            @Param("yearEnd") LocalDateTime yearEnd,
            @Param("now") LocalDateTime now,
            Pageable pageable
    );
}
