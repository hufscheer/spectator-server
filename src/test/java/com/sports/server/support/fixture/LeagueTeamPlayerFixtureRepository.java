package com.sports.server.support.fixture;

import com.sports.server.command.leagueteam.domain.LeagueTeam;
import com.sports.server.command.leagueteam.domain.LeagueTeamPlayer;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface LeagueTeamPlayerFixtureRepository extends Repository<LeagueTeamPlayer, Long> {
    @Query(
            "SELECT ltp FROM LeagueTeamPlayer ltp "
                    + "WHERE ltp.leagueTeam =:leagueTeam"
    )
    List<LeagueTeamPlayer> findByLeagueTeam(@Param("leagueTeam") LeagueTeam leagueTeam);

    Optional<LeagueTeamPlayer> findById(Long id);
}
