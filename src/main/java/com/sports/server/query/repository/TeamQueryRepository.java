package com.sports.server.query.repository;

import com.sports.server.command.team.domain.Team;
import com.sports.server.command.team.domain.TeamPlayer;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TeamQueryRepository extends Repository<Team, Long> {
    List<Team> findAll();

    /**
     * 대진표가 가리키는 팀을 소프트 삭제 여부와 상관없이 읽는다. 네이티브 쿼리라 {@code @SQLRestriction} 을 타지 않는다.
     * 같은 영속성 컨텍스트에서 대진표를 조회하기 전에 불러, 지워진 팀 참조가 이 인스턴스로 풀리게 한다.
     */
    @Query(value = """
            SELECT t.* FROM teams t WHERE t.id IN (
                SELECT bm.team1_id FROM bracket_matches bm WHERE bm.league_id = :leagueId
                UNION
                SELECT bm.team2_id FROM bracket_matches bm WHERE bm.league_id = :leagueId
                UNION
                SELECT gt.team_id FROM game_teams gt
                JOIN bracket_matches bm ON bm.game_id = gt.game_id
                WHERE bm.league_id = :leagueId
            )
            """, nativeQuery = true)
    List<Team> findAllInBracketIncludingDeleted(@Param("leagueId") Long leagueId);

    @Query("SELECT tp FROM TeamPlayer tp JOIN FETCH tp.player WHERE fk(tp.team) = :teamId")
    List<TeamPlayer> findTeamPlayersByTeamId(@Param("teamId") Long teamId);
}
