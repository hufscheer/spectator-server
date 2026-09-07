package com.sports.server.command.timeline.domain;

import com.sports.server.command.game.domain.Game;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TimelineRepository extends Repository<Timeline, Long> {
    void save(Timeline timeline);

    Optional<Timeline> findById(Long id);

    Optional<Timeline> findFirstByGameOrderByIdDesc(Game game);

    List<Timeline> findByGameAndIdGreaterThanOrderByIdAsc(Game game, Long id);

    void delete(Timeline timeline);

    @Modifying
    @Query("DELETE FROM Timeline t WHERE t.game = :game")
    void deleteByGame(@Param("game") Game game);

    /**
     * 라인업 선수를 가리키는 기록이 하나라도 있는지 본다.
     * <p>
     * 기록 종류마다 선수를 담는 컬럼이 달라 네 개를 모두 확인해야 한다. 도메인의
     * {@code getRelatedLineupPlayers} 는 교체 삭제 판정용이라 어시스트를 일부러 빼므로 여기서는 쓸 수 없다.
     * <p>
     * {@code EXISTS} 대신 {@code COUNT} 를 쓴다. MySQL 의 {@code EXISTS} 는 0/1 을 BIGINT 로 돌려줘
     * boolean 으로 못 받는다. H2 에서는 통과하고 MySQL 에서만 깨진다.
     */
    @Query(value = """
            SELECT COUNT(*) FROM timelines
            WHERE scorer_id = :lineupPlayerId
               OR assist_lineup_player_id = :lineupPlayerId
               OR origin_lineup_player_id = :lineupPlayerId
               OR replaced_lineup_player_id = :lineupPlayerId
            """, nativeQuery = true)
    long countReferencing(@Param("lineupPlayerId") Long lineupPlayerId);
}
