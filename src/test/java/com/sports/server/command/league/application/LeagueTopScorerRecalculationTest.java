package com.sports.server.command.league.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.sports.server.command.game.application.GameService;
import com.sports.server.command.game.domain.Game;
import com.sports.server.command.game.domain.GameState;
import com.sports.server.command.league.domain.LeagueTopScorer;
import com.sports.server.command.league.domain.LeagueTopScorerRepository;
import com.sports.server.command.league.domain.SoccerQuarter;
import com.sports.server.command.league.domain.SportType;
import com.sports.server.command.member.domain.Member;
import com.sports.server.command.member.domain.MemberRepository;
import com.sports.server.command.timeline.application.TimelineService;
import com.sports.server.command.timeline.domain.GameProgressTimelineRepository;
import com.sports.server.command.timeline.domain.GameProgressType;
import com.sports.server.command.timeline.dto.TimelineRequest;
import com.sports.server.common.application.EntityUtils;
import com.sports.server.support.ServiceTest;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;

@Sql("/league-top-scorer-fixture.sql")
@DisplayName("경기가 끝나고 취소될 때 득점왕은")
class LeagueTopScorerRecalculationTest extends ServiceTest {

    private static final Long LEAGUE_ID = 1L;
    private static final Long GAME_1 = 1L;
    private static final Long GAME_2 = 2L;
    private static final Long GAME_3 = 3L;
    private static final Long PLAYER_1 = 1L;
    private static final Long PLAYER_2 = 2L;
    private static final Long PLAYER_3 = 3L;
    private static final Long TEAM_A = 1L;
    private static final Long TEAM_B = 2L;
    private static final Long TEAM_C = 3L;

    @Autowired
    private TimelineService timelineService;

    @Autowired
    private GameService gameService;

    @Autowired
    private LeagueTopScorerRepository leagueTopScorerRepository;

    @Autowired
    private GameProgressTimelineRepository gameProgressTimelineRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private EntityUtils entityUtils;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Member manager;

    @BeforeEach
    void setUp() {
        manager = memberRepository.findMemberByEmail("john.doe@example.com").orElseThrow();
    }

    @Test
    void 결승이_아닌_경기가_끝나면_만들어진다() {
        // given
        assertThat(leagueTopScorerRepository.findByLeagueId(LEAGUE_ID)).isEmpty();

        // when
        endGame(GAME_1);

        // then
        List<LeagueTopScorer> topScorers = leagueTopScorerRepository.findByLeagueId(LEAGUE_ID);
        assertThat(topScorers).hasSize(2);
        assertThat(goalCountOf(topScorers, PLAYER_1)).isEqualTo(2);
        assertThat(goalCountOf(topScorers, PLAYER_2)).isEqualTo(1);
        assertThat(rankingOf(topScorers, PLAYER_1)).isEqualTo(1);
        assertThat(rankingOf(topScorers, PLAYER_2)).isEqualTo(2);
    }

    @Test
    void 같은_대회의_다른_경기가_끝나면_득점이_합산된다() {
        // given
        endGame(GAME_1);

        // when
        endGame(GAME_2);

        // then
        List<LeagueTopScorer> topScorers = leagueTopScorerRepository.findByLeagueId(LEAGUE_ID);
        assertThat(topScorers).hasSize(3);
        assertThat(goalCountOf(topScorers, PLAYER_1)).isEqualTo(3);
        assertThat(goalCountOf(topScorers, PLAYER_2)).isEqualTo(1);
        assertThat(goalCountOf(topScorers, PLAYER_3)).isEqualTo(1);
        assertThat(rankingOf(topScorers, PLAYER_2)).isEqualTo(rankingOf(topScorers, PLAYER_3));
    }

    @Test
    void 경기_종료를_취소하면_그_경기의_득점만_빠지고_다시_계산된다() {
        // given
        endGame(GAME_1);
        endGame(GAME_2);

        // when
        cancelGameEnd(GAME_2);

        // then
        List<LeagueTopScorer> topScorers = leagueTopScorerRepository.findByLeagueId(LEAGUE_ID);
        assertThat(topScorers).hasSize(2);
        assertThat(goalCountOf(topScorers, PLAYER_1)).isEqualTo(2);
        assertThat(goalCountOf(topScorers, PLAYER_2)).isEqualTo(1);
        assertThat(topScorers).noneMatch(scorer -> scorer.getPlayer().getId().equals(PLAYER_3));
    }

    @Test
    void 남은_종료_경기가_없으면_경기_종료를_취소했을_때_비워진다() {
        // given
        endGame(GAME_1);
        assertThat(leagueTopScorerRepository.findByLeagueId(LEAGUE_ID)).isNotEmpty();

        // when
        cancelGameEnd(GAME_1);

        // then
        assertThat(leagueTopScorerRepository.findByLeagueId(LEAGUE_ID)).isEmpty();
    }

    @Test
    void 끝나지_않은_경기의_득점은_세지_않는다() {
        // when
        endGame(GAME_1);

        // then 경기2와 경기3은 진행 중이라 선수3의 득점과 선수1의 나머지 득점이 빠진다
        List<LeagueTopScorer> topScorers = leagueTopScorerRepository.findByLeagueId(LEAGUE_ID);
        assertThat(goalCountOf(topScorers, PLAYER_1)).isEqualTo(2);
        assertThat(topScorers).noneMatch(scorer -> scorer.getPlayer().getId().equals(PLAYER_3));
    }

    @Test
    void 선수마다_가장_많이_득점한_팀이_기록된다() {
        // given 선수1: 팀A 2골 (경기1), 팀C 1골 (경기2)
        endGame(GAME_1);

        // when
        endGame(GAME_2);

        // then
        List<LeagueTopScorer> topScorers = leagueTopScorerRepository.findByLeagueId(LEAGUE_ID);
        assertThat(teamIdOf(topScorers, PLAYER_1)).isEqualTo(TEAM_A);
        assertThat(teamIdOf(topScorers, PLAYER_2)).isEqualTo(TEAM_B);
        assertThat(teamIdOf(topScorers, PLAYER_3)).isEqualTo(TEAM_A);
    }

    @Test
    void 득점이_같은_팀이_둘이면_마지막으로_득점한_팀이_기록된다() {
        // given 선수1: 팀A 2골 (경기1), 팀C 1골 (경기2)
        endGame(GAME_1);
        endGame(GAME_2);

        // when 경기3에서 팀C로 1골 더 넣어 팀A 2골, 팀C 2골이 된다
        endGame(GAME_3);

        // then
        List<LeagueTopScorer> topScorers = leagueTopScorerRepository.findByLeagueId(LEAGUE_ID);
        assertThat(goalCountOf(topScorers, PLAYER_1)).isEqualTo(4);
        assertThat(teamIdOf(topScorers, PLAYER_1)).isEqualTo(TEAM_C);
    }

    @Test
    void 종료된_경기를_삭제하면_그_경기의_득점이_빠진다() {
        // given
        endGame(GAME_1);
        endGame(GAME_2);

        // when
        gameService.deleteGame(LEAGUE_ID, GAME_1, manager);

        // then 경기2의 득점만 남는다 (선수1 팀C 1골, 선수3 팀A 1골)
        List<LeagueTopScorer> topScorers = leagueTopScorerRepository.findByLeagueId(LEAGUE_ID);
        assertThat(topScorers).hasSize(2);
        assertThat(goalCountOf(topScorers, PLAYER_1)).isEqualTo(1);
        assertThat(teamIdOf(topScorers, PLAYER_1)).isEqualTo(TEAM_C);
        assertThat(topScorers).noneMatch(scorer -> scorer.getPlayer().getId().equals(PLAYER_2));
    }

    @Test
    void 진행_중인_경기를_삭제해도_득점왕은_그대로다() {
        // given
        endGame(GAME_1);

        // when
        gameService.deleteGame(LEAGUE_ID, GAME_3, manager);

        // then
        List<LeagueTopScorer> topScorers = leagueTopScorerRepository.findByLeagueId(LEAGUE_ID);
        assertThat(topScorers).hasSize(2);
        assertThat(goalCountOf(topScorers, PLAYER_1)).isEqualTo(2);
    }

    @Test
    void 자정에_종료된_경기의_마지막_득점을_지우면_다시_계산된다() {
        // given 경기종료 타임라인 없이 FINISHED 가 된 경기3
        jdbcTemplate.update("UPDATE games SET state = 'FINISHED' WHERE id = ?", GAME_3);
        endGame(GAME_1);
        assertThat(goalCountOf(leagueTopScorerRepository.findByLeagueId(LEAGUE_ID), PLAYER_1)).isEqualTo(3);

        // when 경기3의 마지막 타임라인(선수1의 득점)을 지운다
        Long lastTimelineId = jdbcTemplate.queryForObject(
                "SELECT MAX(id) FROM timelines WHERE game_id = ?", Long.class, GAME_3);
        timelineService.deleteTimeline(manager, GAME_3, lastTimelineId);

        // then
        Game game = entityUtils.getEntity(GAME_3, Game.class);
        assertThat(game.getState()).isEqualTo(GameState.FINISHED);
        assertThat(goalCountOf(leagueTopScorerRepository.findByLeagueId(LEAGUE_ID), PLAYER_1)).isEqualTo(2);
    }

    private void endGame(Long gameId) {
        timelineService.register(manager, gameId, new TimelineRequest.RegisterProgress(
                90, SportType.SOCCER, SoccerQuarter.SECOND_HALF.name(), GameProgressType.GAME_END));
    }

    private void cancelGameEnd(Long gameId) {
        Game game = entityUtils.getEntity(gameId, Game.class);
        Long gameEndTimelineId = gameProgressTimelineRepository.findFirstByGameOrderByIdDesc(game)
                .orElseThrow().getId();
        timelineService.deleteTimeline(manager, gameId, gameEndTimelineId);
    }

    private int goalCountOf(List<LeagueTopScorer> topScorers, Long playerId) {
        return find(topScorers, playerId).getGoalCount();
    }

    private int rankingOf(List<LeagueTopScorer> topScorers, Long playerId) {
        return find(topScorers, playerId).getRanking();
    }

    private Long teamIdOf(List<LeagueTopScorer> topScorers, Long playerId) {
        return find(topScorers, playerId).getTeamId();
    }

    private LeagueTopScorer find(List<LeagueTopScorer> topScorers, Long playerId) {
        return topScorers.stream()
                .filter(scorer -> scorer.getPlayer().getId().equals(playerId))
                .findFirst()
                .orElseThrow(() -> new AssertionError("선수" + playerId + "가 득점왕 목록에 없습니다."));
    }
}
