package com.sports.server.query.repository;

import com.sports.server.command.league.domain.LeagueTeam;
import org.springframework.data.repository.Repository;

import java.util.List;

public interface LeagueTeamQueryRepository extends Repository<LeagueTeam, Long> {
    List<LeagueTeam> findByLeagueId(Long leagueId);
}
