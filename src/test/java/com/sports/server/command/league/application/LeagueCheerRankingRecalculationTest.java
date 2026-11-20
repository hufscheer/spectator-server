package com.sports.server.command.league.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.sports.server.command.game.application.GameService;
import com.sports.server.command.game.application.GameStatusScheduler;
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
import com.sports.server.query.application.LeagueQueryService;
import com.sports.server.query.dto.response.LeagueStatisticsResponse;
import com.sports.server.query.dto.response.LeagueTeamRankingResponse;
import com.sports.server.support.ServiceTest;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;

@Sql("/league-cheer-ranking-fixture.sql")
@DisplayName("경기가 끝나고 취소될 때 응원왕과 댓글왕은")
class LeagueCheerRankingRecalculationTest extends ServiceTest {

    private static final Long LEAGUE_ID = 1L;
    private static final Long GAME_1 = 1L; // 팀A 100, 팀B 50 / 응원톡 A3(차단 1 제외) B1
    private static final Long GAME_2 = 2L; // 팀C 90, 팀D 80 / 응원톡 C2 D2
    private static final Long GAME_3 = 3L; // 결승 팀A 10, 팀C 5 / 응원톡 A5
    private static final Long GAME_4 = 4L; // 팀D 0, 팀E 80

    @Autowired
    private TimelineService timelineService;

    @Autowired
    private GameService gameService;

    @Autowired
    private GameStatusScheduler gameStatusScheduler;

    @Autowired
    private LeagueQueryService leagueQueryService;

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
    void 결승이_아닌_경기가_끝나면_갱신되고_우승팀은_비어_있다() {
        // when
        endGame(GAME_1);

        // then
        LeagueStatisticsResponse response = statistics();
        assertThat(cheers(response.cheerRanking())).containsExactly("1:팀A:100", "2:팀B:50");
        assertThat(talks(response.cheerTalksRanking())).containsExactly("1:팀A:3", "2:팀B:1");
        assertThat(response.mostCheeredTeam().teamName()).isEqualTo("팀A");
        assertThat(response.mostCheeredTeam().cheerCount()).isEqualTo(100);
        assertThat(response.mostCheerTalksTeam().teamName()).isEqualTo("팀A");
        assertThat(response.firstWinnerTeam()).isNull();
        assertThat(response.secondWinnerTeam()).isNull();
    }

    @Test
    void 끝나지_않은_경기의_응원과_차단된_응원톡은_세지_않는다() {
        // when 경기2, 경기3(결승 응원톡 5건)은 진행 중이다
        endGame(GAME_1);

        // then 팀C, 팀D, 팀A 의 결승 몫이 없고 차단된 응원톡도 빠진다
        LeagueStatisticsResponse response = statistics();
        assertThat(cheers(response.cheerRanking())).containsExactly("1:팀A:100", "2:팀B:50");
        assertThat(talks(response.cheerTalksRanking())).containsExactly("1:팀A:3", "2:팀B:1");
    }

    @Test
    void 다른_경기가_끝나면_합산된다() {
        // given
        endGame(GAME_1);

        // when
        endGame(GAME_2);

        // then 팀B 는 4위라 빠진다
        LeagueStatisticsResponse response = statistics();
        assertThat(cheers(response.cheerRanking())).containsExactly("1:팀A:100", "2:팀C:90", "3:팀D:80");
        assertThat(talks(response.cheerTalksRanking()))
                .containsExactly("1:팀A:3", "2:팀C:2", "2:팀D:2");
    }

    @Test
    void 삼위가_동률이면_모두_준다() {
        // given
        endGame(GAME_1);
        endGame(GAME_2);

        // when 팀D 0 + 팀E 80
        endGame(GAME_4);

        // then
        assertThat(cheers(statistics().cheerRanking()))
                .containsExactly("1:팀A:100", "2:팀C:90", "3:팀D:80", "3:팀E:80");
    }

    @Test
    void 일등이_동률이면_다음은_삼위다() {
        // given 경기1 팀B 를 팀A 와 같게
        jdbcTemplate.update("UPDATE game_teams SET cheer_count = 100 WHERE id = 2");
        jdbcTemplate.update("UPDATE game_teams SET cheer_count = 50 WHERE id = 3");

        // when
        endGame(GAME_1);
        endGame(GAME_2);

        // then 팀A 100, 팀B 100, 팀C 50, 팀D 80 -> 1,1,3(팀D 80 이 3위, 팀C 50 은 4위라 제외)
        assertThat(cheers(statistics().cheerRanking()))
                .containsExactly("1:팀A:100", "1:팀B:100", "3:팀D:80");
    }

    @Test
    void 응원이_없는_팀은_빠지고_전부_없으면_비어_있다() {
        // given
        jdbcTemplate.update("UPDATE game_teams SET cheer_count = 0 WHERE game_id = ?", GAME_1);
        jdbcTemplate.update("DELETE FROM cheer_talks");

        // when
        endGame(GAME_1);

        // then
        LeagueStatisticsResponse response = statistics();
        assertThat(response.cheerRanking()).isEmpty();
        assertThat(response.cheerTalksRanking()).isEmpty();
        assertThat(response.mostCheeredTeam()).isNull();
        assertThat(response.mostCheerTalksTeam()).isNull();
    }

    @Test
    void 경기_종료를_취소하면_그_경기_몫이_빠진다() {
        // given
        endGame(GAME_1);
        endGame(GAME_2);

        // when
        cancelGameEnd(GAME_2);

        // then
        LeagueStatisticsResponse response = statistics();
        assertThat(cheers(response.cheerRanking())).containsExactly("1:팀A:100", "2:팀B:50");
        assertThat(talks(response.cheerTalksRanking())).containsExactly("1:팀A:3", "2:팀B:1");
    }

    @Test
    void 종료된_경기를_삭제해도_빠진다() {
        // given
        endGame(GAME_1);
        endGame(GAME_2);

        // when
        gameService.deleteGame(LEAGUE_ID, GAME_1, manager);

        // then
        LeagueStatisticsResponse response = statistics();
        assertThat(cheers(response.cheerRanking())).containsExactly("1:팀C:90", "2:팀D:80");
        assertThat(talks(response.cheerTalksRanking())).containsExactly("1:팀C:2", "1:팀D:2");
    }

    @Test
    void 결승이_끝나면_우승팀이_채워지고_응원도_합산된다() {
        // given
        endGame(GAME_1);
        endGame(GAME_2);

        // when
        endGame(GAME_3);

        // then 팀A 110, 팀C 95, 팀D 80 / 응원톡 팀A 8
        LeagueStatisticsResponse response = statistics();
        assertThat(response.firstWinnerTeam().teamName()).isEqualTo("팀A");
        assertThat(response.secondWinnerTeam().teamName()).isEqualTo("팀C");
        assertThat(cheers(response.cheerRanking())).containsExactly("1:팀A:110", "2:팀C:95", "3:팀D:80");
        assertThat(talks(response.cheerTalksRanking())).startsWith("1:팀A:8");
    }

    @Test
    void 결승_종료를_취소하면_우승팀은_비고_응원_순위는_남은_종료_경기로_유지된다() {
        // given
        endGame(GAME_1);
        endGame(GAME_2);
        endGame(GAME_3);

        // when
        cancelGameEnd(GAME_3);

        // then
        LeagueStatisticsResponse response = statistics();
        assertThat(response.firstWinnerTeam()).isNull();
        assertThat(response.secondWinnerTeam()).isNull();
        assertThat(cheers(response.cheerRanking())).containsExactly("1:팀A:100", "2:팀C:90", "3:팀D:80");
        assertThat(response.mostCheeredTeam().teamName()).isEqualTo("팀A");
    }

    @Test
    void 자정_종료나_수동_갱신처럼_트랜잭션_밖에서_와도_바로_센다() {
        // given 타임라인 없이 종료된 경기(자정 자동 종료와 같은 상태)
        jdbcTemplate.update("UPDATE games SET state = 'FINISHED' WHERE id = ?", GAME_1);

        // when 자정 스케줄러와 /admin/games/statistics/update 가 쓰는 경로
        gameStatusScheduler.manualUpdateLeagueStatisticsForFinalGames(List.of(GAME_1));

        // then
        LeagueStatisticsResponse response = statistics();
        assertThat(cheers(response.cheerRanking())).containsExactly("1:팀A:100", "2:팀B:50");
        assertThat(talks(response.cheerTalksRanking())).containsExactly("1:팀A:3", "2:팀B:1");
        assertThat(response.firstWinnerTeam()).isNull();
    }

    private LeagueStatisticsResponse statistics() {
        return leagueQueryService.findLeagueStatistic(LEAGUE_ID);
    }

    private List<String> cheers(List<LeagueTeamRankingResponse> ranking) {
        return ranking.stream()
                .map(r -> r.ranking() + ":" + r.teamName() + ":" + r.cheerCount())
                .toList();
    }

    private List<String> talks(List<LeagueTeamRankingResponse> ranking) {
        return ranking.stream()
                .map(r -> r.ranking() + ":" + r.teamName() + ":" + r.cheerTalksCount())
                .toList();
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
}
