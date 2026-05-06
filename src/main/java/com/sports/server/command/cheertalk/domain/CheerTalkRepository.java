package com.sports.server.command.cheertalk.domain;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface CheerTalkRepository extends Repository<CheerTalk, Long> {
    void save(CheerTalk cheerTalk);

    @Query("SELECT COUNT(ct) FROM CheerTalk ct " +
           "JOIN GameTeam gt ON ct.gameTeamId = gt.id " +
           "JOIN gt.game g " +
           "WHERE gt.team.id = :teamId AND g.league.id = :leagueId AND ct.blockStatus = com.sports.server.command.cheertalk.domain.CheerTalkBlockStatus.ACTIVE")
    Long countCheerTalksByTeamIdAndLeagueId(@Param("teamId") Long teamId, @Param("leagueId") Long leagueId);

    @Query("SELECT COUNT(ct) FROM CheerTalk ct " +
           "WHERE ct.gameTeamId IN :gameTeamIds AND ct.isAiSeed = true")
    long countAiSeedsByGameTeamIds(@Param("gameTeamIds") List<Long> gameTeamIds);

    @Query("SELECT CASE WHEN COUNT(ct) > 0 THEN true ELSE false END FROM CheerTalk ct " +
           "WHERE ct.gameTeamId IN :gameTeamIds AND ct.isAiSeed = false AND ct.createdAt > :since")
    boolean existsUserCheerTalkAfter(@Param("gameTeamIds") List<Long> gameTeamIds,
                                     @Param("since") LocalDateTime since);

    @Query("SELECT ct FROM CheerTalk ct " +
           "WHERE ct.gameTeamId IN :gameTeamIds AND ct.isAiSeed = true " +
           "ORDER BY ct.createdAt DESC LIMIT 1")
    Optional<CheerTalk> findLastAiSeed(@Param("gameTeamIds") List<Long> gameTeamIds);
}
