package com.sports.server.query.repository;

import com.sports.server.command.league.domain.LeagueStatistics;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LeagueStatisticsQueryRepository extends Repository<LeagueStatistics, Long> {

    @Query("""
            SELECT ls FROM LeagueStatistics ls
            LEFT JOIN FETCH ls.firstWinnerTeam
            LEFT JOIN FETCH ls.secondWinnerTeam
            LEFT JOIN FETCH ls.mostCheeredTeam
            LEFT JOIN FETCH ls.mostCheerTalksTeam
            WHERE fk(ls.league) = :leagueId
            """)
    Optional<LeagueStatistics> findByLeagueId(@Param("leagueId") Long leagueId);

    @Query("SELECT ls FROM LeagueStatistics ls JOIN FETCH ls.league " +
            "WHERE fk(ls.firstWinnerTeam) IN :teamIds OR fk(ls.secondWinnerTeam) IN :teamIds")
    List<LeagueStatistics> findTrophiesByTeamIds(@Param("teamIds") List<Long> teamIds);

    @Query("SELECT new com.sports.server.query.repository.LeagueWinnerInfo(cast(fk(ls.league) as long), ls.firstWinnerTeam.name) " +
            "FROM LeagueStatistics ls " +
            "WHERE fk(ls.league) IN :leagueIds AND ls.firstWinnerTeam IS NOT NULL")
    List<LeagueWinnerInfo> findWinnerTeamInfoByLeagueIds(@Param("leagueIds") List<Long> leagueIds);
}
