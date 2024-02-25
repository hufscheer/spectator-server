package com.sports.server.query.repository;

import com.sports.server.command.leagueteam.LeagueTeam;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeagueTeamQueryRepository extends JpaRepository<LeagueTeam, Long> {
    List<LeagueTeam> findByLeagueId(Long leagueId);
}
