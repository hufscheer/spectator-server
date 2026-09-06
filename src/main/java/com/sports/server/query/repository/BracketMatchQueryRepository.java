package com.sports.server.query.repository;

import com.sports.server.command.bracket.domain.BracketMatch;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface BracketMatchQueryRepository extends Repository<BracketMatch, Long> {

    @Query("select distinct bm from BracketMatch bm "
            + "left join fetch bm.team1 "
            + "left join fetch bm.team2 "
            + "left join fetch bm.game g "
            + "left join fetch g.gameTeams gt "
            + "left join fetch gt.team "
            + "where bm.league.id = :leagueId")
    List<BracketMatch> findAllByLeagueIdWithTeamsAndGames(@Param("leagueId") Long leagueId);
}
