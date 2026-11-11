package com.sports.server.command.bracket.domain;

import com.sports.server.command.game.domain.Game;
import com.sports.server.command.league.domain.Round;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BracketMatchRepository extends JpaRepository<BracketMatch, Long> {

    @Query("SELECT bm FROM BracketMatch bm "
            + "LEFT JOIN FETCH bm.team1 LEFT JOIN FETCH bm.team2 LEFT JOIN FETCH bm.game "
            + "WHERE bm.league.id = :leagueId")
    List<BracketMatch> findAllByLeagueId(@Param("leagueId") Long leagueId);

    Optional<BracketMatch> findByGame(Game game);

    boolean existsByLeagueIdAndGameIsNotNull(Long leagueId);

    boolean existsByLeagueIdAndRoundAndGameIsNotNull(Long leagueId, Round round);

    // 파생 delete 는 flush 시점(insert 이후)에 실행되어 재배치 시 유니크 제약에 걸리므로 즉시 실행되는 bulk delete 사용
    @Modifying
    @Query("DELETE FROM BracketMatch bm WHERE bm.league.id = :leagueId")
    void deleteAllByLeagueId(@Param("leagueId") Long leagueId);
}
