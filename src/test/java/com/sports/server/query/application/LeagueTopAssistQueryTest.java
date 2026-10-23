package com.sports.server.query.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.sports.server.command.game.application.GameService;
import com.sports.server.command.game.domain.Game;
import com.sports.server.command.league.domain.SoccerQuarter;
import com.sports.server.command.league.domain.SportType;
import com.sports.server.command.member.domain.Member;
import com.sports.server.command.member.domain.MemberRepository;
import com.sports.server.command.timeline.application.TimelineService;
import com.sports.server.command.timeline.domain.GameProgressTimelineRepository;
import com.sports.server.command.timeline.domain.GameProgressType;
import com.sports.server.command.timeline.dto.TimelineRequest;
import com.sports.server.common.application.EntityUtils;
import com.sports.server.query.dto.response.TopAssistResponse;
import com.sports.server.support.ServiceTest;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;

@Sql("/league-top-assist-fixture.sql")
@DisplayName("대회 도움왕은")
class LeagueTopAssistQueryTest extends ServiceTest {

    private static final Long LEAGUE_ID = 1L;
    private static final Long GAME_1 = 1L;
    private static final Long GAME_2 = 2L;
    private static final Long GAME_3 = 3L;
    private static final Long PLAYER_1 = 1L;
    private static final Long PLAYER_2 = 2L;
    private static final Long PLAYER_3 = 3L;
    private static final Long PLAYER_4 = 4L;
    private static final Long PLAYER_6 = 6L;
    private static final Long TEAM_A = 1L;
    private static final Long TEAM_B = 2L;
    private static final Long TEAM_C = 3L;

    @Autowired
    private LeagueQueryService leagueQueryService;

    @Autowired
    private TimelineService timelineService;

    @Autowired
    private GameService gameService;

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
    void 결승이_아닌_경기가_끝나면_도움왕에_나온다() {
        // given
        assertThat(leagueQueryService.findTop20AssistsByLeagueId(LEAGUE_ID)).isEmpty();

        // when
        endGame(GAME_1);

        // then
        List<TopAssistResponse> assists = leagueQueryService.findTop20AssistsByLeagueId(LEAGUE_ID);
        assertThat(assists).hasSize(2);
        TopAssistResponse first = find(assists, PLAYER_1);
        assertThat(first.assistCount()).isEqualTo(2);
        assertThat(first.ranking()).isEqualTo(1);
        assertThat(first.teamId()).isEqualTo(TEAM_A);
        assertThat(first.teamName()).isEqualTo("팀A");
        assertThat(first.logoImageUrl()).isEqualTo("http://example.com/logo_a.png");
        TopAssistResponse second = find(assists, PLAYER_3);
        assertThat(second.assistCount()).isEqualTo(1);
        assertThat(second.ranking()).isEqualTo(2);
        assertThat(second.teamId()).isEqualTo(TEAM_B);
    }

    @Test
    void 도움_없는_득점은_세지_않는다() {
        // when
        endGame(GAME_1);

        // then 경기1의 득점은 4개지만 도움이 기록된 3개만 센다
        List<TopAssistResponse> assists = leagueQueryService.findTop20AssistsByLeagueId(LEAGUE_ID);
        assertThat(find(assists, PLAYER_1).assistCount()).isEqualTo(2);
        assertThat(assists).noneMatch(assist -> assist.playerId().equals(PLAYER_2));
        assertThat(assists).noneMatch(assist -> assist.playerId().equals(PLAYER_4));
    }

    @Test
    void 같은_대회의_다른_경기가_끝나면_도움이_합산된다() {
        // given
        endGame(GAME_1);

        // when
        endGame(GAME_2);

        // then
        List<TopAssistResponse> assists = leagueQueryService.findTop20AssistsByLeagueId(LEAGUE_ID);
        assertThat(assists).hasSize(3);
        assertThat(find(assists, PLAYER_1).assistCount()).isEqualTo(3);
        assertThat(find(assists, PLAYER_3).assistCount()).isEqualTo(1);
        assertThat(find(assists, PLAYER_6).assistCount()).isEqualTo(1);
        assertThat(find(assists, PLAYER_3).ranking()).isEqualTo(2);
        assertThat(find(assists, PLAYER_6).ranking()).isEqualTo(2);
        assertThat(assists).extracting(TopAssistResponse::playerId)
                .containsExactly(PLAYER_1, PLAYER_3, PLAYER_6);
    }

    @Test
    void 경기_종료를_취소하면_그_경기의_도움이_빠진다() {
        // given
        endGame(GAME_1);
        endGame(GAME_2);

        // when
        cancelGameEnd(GAME_2);

        // then
        List<TopAssistResponse> assists = leagueQueryService.findTop20AssistsByLeagueId(LEAGUE_ID);
        assertThat(assists).hasSize(2);
        assertThat(find(assists, PLAYER_1).assistCount()).isEqualTo(2);
        assertThat(assists).noneMatch(assist -> assist.playerId().equals(PLAYER_6));
    }

    @Test
    void 끝나지_않은_경기의_도움은_세지_않는다() {
        // when
        endGame(GAME_1);

        // then 경기2와 경기3은 진행 중이라 선수6의 도움과 선수1의 나머지 도움이 빠진다
        List<TopAssistResponse> assists = leagueQueryService.findTop20AssistsByLeagueId(LEAGUE_ID);
        assertThat(assists).noneMatch(assist -> assist.playerId().equals(PLAYER_6));
        assertThat(find(assists, PLAYER_1).assistCount()).isEqualTo(2);
    }

    @Test
    void 선수마다_가장_많이_도움한_팀이_나온다() {
        // given 선수1: 팀A 2개 (경기1), 팀C 1개 (경기2)
        endGame(GAME_1);

        // when
        endGame(GAME_2);

        // then
        List<TopAssistResponse> assists = leagueQueryService.findTop20AssistsByLeagueId(LEAGUE_ID);
        assertThat(find(assists, PLAYER_1).teamId()).isEqualTo(TEAM_A);
        assertThat(find(assists, PLAYER_3).teamId()).isEqualTo(TEAM_B);
        assertThat(find(assists, PLAYER_6).teamId()).isEqualTo(TEAM_A);
    }

    @Test
    void 도움이_같은_팀이_둘이면_마지막으로_도움한_팀이_나온다() {
        // given 선수1: 팀A 2개 (경기1), 팀C 1개 (경기2)
        endGame(GAME_1);
        endGame(GAME_2);

        // when 경기3에서 팀C로 1개 더 도움해 팀A 2개, 팀C 2개가 된다
        endGame(GAME_3);

        // then
        List<TopAssistResponse> assists = leagueQueryService.findTop20AssistsByLeagueId(LEAGUE_ID);
        assertThat(find(assists, PLAYER_1).assistCount()).isEqualTo(4);
        assertThat(find(assists, PLAYER_1).teamId()).isEqualTo(TEAM_C);
    }

    @Test
    void 종료된_경기를_삭제하면_그_경기의_도움이_빠진다() {
        // given
        endGame(GAME_1);
        endGame(GAME_2);

        // when
        gameService.deleteGame(LEAGUE_ID, GAME_1, manager);

        // then 경기2의 도움만 남는다 (선수1 팀C 1개, 선수6 팀A 1개)
        List<TopAssistResponse> assists = leagueQueryService.findTop20AssistsByLeagueId(LEAGUE_ID);
        assertThat(find(assists, PLAYER_1).assistCount()).isEqualTo(1);
        assertThat(find(assists, PLAYER_1).teamId()).isEqualTo(TEAM_C);
        assertThat(assists).noneMatch(assist -> assist.playerId().equals(PLAYER_3));
    }

    @Test
    void 팀이_삭제되면_팀_정보가_비어_나온다() {
        // given
        endGame(GAME_1);

        // when
        jdbcTemplate.update("UPDATE teams SET is_deleted = 1 WHERE id = 1");

        // then
        TopAssistResponse assist = find(leagueQueryService.findTop20AssistsByLeagueId(LEAGUE_ID), PLAYER_1);
        assertThat(assist.assistCount()).isEqualTo(2);
        assertThat(assist.teamId()).isNull();
        assertThat(assist.teamName()).isNull();
        assertThat(assist.logoImageUrl()).isNull();
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

    private TopAssistResponse find(List<TopAssistResponse> assists, Long playerId) {
        return assists.stream()
                .filter(assist -> assist.playerId().equals(playerId))
                .findFirst()
                .orElseThrow(() -> new AssertionError("선수" + playerId + "가 도움왕 목록에 없습니다."));
    }
}
