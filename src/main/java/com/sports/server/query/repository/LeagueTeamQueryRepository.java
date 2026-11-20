package com.sports.server.query.repository;

import com.sports.server.command.league.domain.LeagueTeam;
import com.sports.server.command.league.domain.LeagueTeamTally;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LeagueTeamQueryRepository extends Repository<LeagueTeam, Long> {
    @Query("SELECT lt FROM LeagueTeam lt WHERE fk(lt.league) = :leagueId")
    List<LeagueTeam> findByLeagueId(@Param("leagueId") Long leagueId);

    // 삭제된 팀은 JOIN 에서 빠진다(순위 후보에서 제외)
    @Query("SELECT new com.sports.server.command.league.domain.LeagueTeamTally(" +
            "lt.id, t.id, t.name, t.logoImageUrl, lt.totalCheerCount, lt.totalTalkCount) " +
            "FROM LeagueTeam lt JOIN lt.team t WHERE fk(lt.league) = :leagueId")
    List<LeagueTeamTally> findTalliesByLeagueId(@Param("leagueId") Long leagueId);
}
