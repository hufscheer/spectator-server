package com.sports.server.command.leagueteam.domain;

import com.sports.server.command.league.domain.League;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

public interface LeagueTeamRepository extends Repository<LeagueTeam, Long> {
    void save(LeagueTeam leagueTeam);

    Optional<LeagueTeam> findByLeagueAndName(League league, String name);

    @Query(
            "SELECT lt FROM LeagueTeam lt "
                    + "LEFT JOIN FETCH lt.leagueTeamPlayers "
                    + "WHERE lt.id = :id"
    )
    Optional<LeagueTeam> findById(Long id);

    void delete(LeagueTeam leagueTeam);
}
