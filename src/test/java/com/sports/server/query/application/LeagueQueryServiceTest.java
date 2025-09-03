package com.sports.server.query.application;

import static com.sports.server.query.application.LeagueQueryService.leagueProgressOrderMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.sports.server.command.game.domain.GameState;
import com.sports.server.command.member.domain.Member;
import com.sports.server.common.application.EntityUtils;
import com.sports.server.query.dto.response.*;
import com.sports.server.query.dto.response.LeagueResponseWithGames.GameDetail;
import com.sports.server.query.dto.response.LeagueResponseWithInProgressGames.GameDetailResponse;
import com.sports.server.query.dto.response.LeagueResponseWithInProgressGames.GameDetailResponse.GameTeamResponse;
import com.sports.server.query.dto.request.LeagueQueryRequestDto;
import com.sports.server.common.dto.PageRequestDto;
import com.sports.server.support.ServiceTest;

import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

@Sql(scripts = "/league-fixture.sql")
public class LeagueQueryServiceTest extends ServiceTest {

    @Autowired
    LeagueQueryService leagueQueryService;

    @Autowired
    EntityUtils entityUtils;

    @Test
    void 리그에_해당하는_리그팀을_모두_조회한다() {
        // given
        Long leagueId = 1L;

        // when
        List<LeagueTeamResponse> leagueTeams = leagueQueryService.findTeamsByLeagueRound(leagueId, null);
        LeagueTeamResponse leagueTeam = leagueTeams.stream().filter(team -> team.leagueTeamId().equals(3L)).findFirst()
                .orElse(null);

        // then
        assertAll(
                () -> assertThat(leagueTeam.teamName()).isEqualTo("미컴 축구생각"),
                () -> assertThat(leagueTeam.sizeOfTeamPlayers()).isEqualTo(2)
        );

    }

    @Test
    void 리그_통계를_정상적으로_조회한다() {
        // given
        Long leagueId = 1L;

        // when
        LeagueStatisticsResponse response = leagueQueryService.findLeagueStatistic(leagueId);

        // then
        assertAll(
                () -> assertThat(response.firstWinnerTeam()).isNotNull(),
                () -> assertThat(response.secondWinnerTeam()).isNotNull(),
                () -> assertThat(response.mostCheeredTeam()).isNotNull(),
                () -> assertThat(response.mostCheerTalksTeam()).isNotNull()
        );
    }

    @Nested
    @DisplayName("매니저가 생성한 리그만을 조회할 때(홈화면)")
    class LeaguesByManagerTest {

        private Member manager;
        private List<LeagueResponseWithInProgressGames> response;

        @BeforeEach
        void setUp() {
            manager = entityUtils.getEntity(1L, Member.class);
            response = leagueQueryService.findLeaguesByManager(manager);
        }

        private Optional<LeagueResponseWithInProgressGames> getFirstLeagueById(Long leagueId) {
            return response.stream()
                    .filter(league -> league.id().equals(leagueId))
                    .findFirst();
        }

        @Test
        void 다른_매니저가_생성한_리그는_조회되지_않는다() {
            // then
            List<Long> ids = response.stream()
                    .map(LeagueResponseWithInProgressGames::id)
                    .toList();
            assertFalse(ids.contains(3L), "다른 매니저가 생성한 리그는 조회 되어서는 안됩니다.");
        }

        @Test
        void 진행중이_아닌_경기가_반환되어서는_안된다() {
            // given
            Long leagueId = 1L;
            Long nonPlayingGameId = 2L;

            // then
            LeagueResponseWithInProgressGames firstLeague = getFirstLeagueById(leagueId)
                    .orElseThrow(() -> new AssertionError("리그가 존재하지 않습니다."));

            List<Long> ids = firstLeague.inProgressGames()
                    .stream()
                    .map(GameDetailResponse::id)
                    .toList();

            assertFalse(ids.contains(nonPlayingGameId), "진행 중이 아닌 경기가 반환되어서는 안됩니다.");
        }

        @Test
        void 다른_리그에_속한_경기가_반환되어서는_안된다() {
            // given
            Long leagueId = 1L;
            Long otherLeagueGameId = 4L;

            // then
            LeagueResponseWithInProgressGames firstLeague = getFirstLeagueById(leagueId)
                    .orElseThrow(() -> new AssertionError("리그가 존재하지 않습니다."));

            List<Long> ids = firstLeague.inProgressGames()
                    .stream()
                    .map(GameDetailResponse::id)
                    .toList();

            assertFalse(ids.contains(otherLeagueGameId), "다른 리그에 속한 경기가 반환되어서는 안됩니다.");
        }

        @Test
        void 경기에_알맞는_게임팀이_반환된다() {
            // given
            Long leagueId = 1L;
            Long gameId = 1L;

            // then
            LeagueResponseWithInProgressGames firstLeague = getFirstLeagueById(leagueId)
                    .orElseThrow(() -> new AssertionError("리그가 존재하지 않습니다."));

            List<Long> idsOfGameTeamsOfFirstGame = firstLeague.inProgressGames()
                    .stream()
                    .filter(game -> game.id().equals(gameId))
                    .findFirst()
                    .orElseThrow(() -> new AssertionError("게임이 존재하지 않습니다."))
                    .gameTeams()
                    .stream()
                    .map(GameTeamResponse::gameTeamId)
                    .toList();

            assertThat(idsOfGameTeamsOfFirstGame).isEqualTo(List.of(1L, 2L));
        }
    }

    @Nested
    @DisplayName("매니저가 생성한 리그만을 조회할 때(대회 관리 화면)")
    class LeaguesByManagerToManageTest {

        private Member manager;
        private List<LeagueResponseToManage> response;

        @BeforeEach
        void setUp() {
            manager = entityUtils.getEntity(1L, Member.class);
            response = leagueQueryService.findLeaguesByManagerToManage(manager);
        }

        @Test
        void 다른_매니저가_생성한_리그는_조회되지_않는다() {
            // then
            List<Long> ids = response.stream()
                    .map(LeagueResponseToManage::id)
                    .toList();
            assertFalse(ids.contains(3L), "다른 매니저가 생성한 리그는 조회 되어서는 안됩니다.");
        }

        @Test
        void 리그가_진행중_시작전_종료_순으로_조회된다() {
            // given
            Comparator<String> comparator = Comparator.comparingInt(leagueProgressOrderMap::get);

            // then
            assertAll(
                    () -> {
                        List<String> gameProgresses = response
                                .stream()
                                .map(LeagueResponseToManage::leagueProgress)
                                .toList();
                        assertThat(gameProgresses).isSortedAccordingTo(comparator);
                    }
            );
        }
    }

    @Nested
    @DisplayName("리그와 리그의 경기들을 조회할 때")
    class findLeagueAndGamesTest {

        private Long leagueId;

        @BeforeEach
        void setUp() {
            this.leagueId = 1L;
        }

        @Test
        void 다른_리그에_속한_경기가_반환되어서는_안된다() {
            // given
            Long leagueId = 1L;
            Long otherLeagueGameId = 4L;

            // when
            LeagueResponseWithGames leagueAndGames = leagueQueryService.findLeagueAndGames(leagueId);
            List<Long> ids = new ArrayList<>();
            ids.addAll(leagueAndGames.playingGames().stream()
                    .map(GameDetail::id).toList());

            ids.addAll(leagueAndGames.scheduledGames().stream()
                    .map(GameDetail::id).toList());

            ids.addAll(leagueAndGames
                    .finishedGames().stream().map(GameDetail::id).toList());

            // then
            assertFalse(ids.contains(otherLeagueGameId), "다른 리그에 속한 경기가 반환되어서는 안됩니다.");
        }

        @Test
        void playingGames에는_진행_중인_경기만_반환된다() {
            // when
            LeagueResponseWithGames leagueWithGames = leagueQueryService.findLeagueAndGames(leagueId);

            // then
            leagueWithGames.playingGames().stream()
                    .forEach(g -> assertThat(g.state()).isEqualTo(GameState.PLAYING.name()));
        }

        @Test
        void scheduledGames에는_예정된_경기만_반환된다() {
            // when
            LeagueResponseWithGames leagueWithGames = leagueQueryService.findLeagueAndGames(leagueId);

            // then
            leagueWithGames.scheduledGames().stream()
                    .forEach(g -> assertThat(g.state()).isEqualTo(GameState.SCHEDULED.name()));
        }

        @Test
        void finishedGames에는_종료된_경기만_반환된다() {
            // when
            LeagueResponseWithGames leagueWithGames = leagueQueryService.findLeagueAndGames(leagueId);

            // then
            leagueWithGames.finishedGames().stream()
                    .forEach(g -> assertThat(g.state()).isEqualTo(GameState.FINISHED.name()));
        }
    }

    @Test
    void 필터링_조건으로_리그를_조회한다() {
        // given
        LeagueQueryRequestDto requestDto = new LeagueQueryRequestDto(null, null);
        PageRequestDto pageRequestDto = new PageRequestDto(null, 10);

        // when
        List<LeagueResponse> leagues = leagueQueryService.findLeagues(requestDto, pageRequestDto);

        // then
        assertAll(
                () -> assertThat(leagues).isNotNull(),
                () -> leagues.forEach(league -> 
                    assertThat(league.name()).isNotNull()
                )
        );
    }

    @Test
    void 리그_상세정보를_조회한다() {
        // given
        Long leagueId = 1L;

        // when
        LeagueDetailResponse response = leagueQueryService.findLeagueDetail(leagueId);

        // then
        assertAll(
                () -> assertThat(response.name()).isEqualTo("종료된 축구대회 1"),
                () -> assertThat(response.leagueTeamCount()).isEqualTo(3),
                () -> assertThat(response.maxRound()).isEqualTo(8),
                () -> assertThat(response.inProgressRound()).isEqualTo(8)
        );
    }

    @Test
    void 리그팀의_선수들을_조회한다() {
        // given
        Long leagueTeamId = 3L;

        // when
        List<PlayerResponse> players = leagueQueryService.findPlayersByLeagueTeam(leagueTeamId);

        // then
        assertAll(
                () -> assertThat(players).hasSize(2),
                () -> {
                    List<String> playerNames = players.stream()
                            .map(PlayerResponse::name)
                            .toList();
                    assertThat(playerNames).containsExactlyInAnyOrder("진승희", "이동규");
                }
        );
    }

    @Test
    void 리그의_상위_득점자를_조회한다() {
        // given
        Long leagueId = 1L;

        // when
        List<TopScorerResponse> scorers = leagueQueryService.findTop20ScorersByLeagueId(leagueId);

        // then
        assertAll(
                () -> assertThat(scorers).hasSize(3),
                () -> assertThat(scorers.get(0).playerName()).isEqualTo("진승희"),
                () -> assertThat(scorers.get(0).goalCount()).isEqualTo(5),
                () -> assertThat(scorers.get(1).playerName()).isEqualTo("이동규"),
                () -> assertThat(scorers.get(1).goalCount()).isEqualTo(3)
        );
    }

    @Test
    void 연도별_상위_득점자를_조회한다() {
        // given
        Integer year = 2025;
        Integer size = 10;

        // when
        List<TopScorerResponse> scorers = leagueQueryService.findTopScorersByYear(year, size);

        // then
        assertAll(
                () -> assertThat(scorers).isNotEmpty(),
                () -> scorers.forEach(scorer -> {
                    assertThat(scorer.playerName()).isNotNull();
                    assertThat(scorer.goalCount()).isGreaterThan(0);
                })
        );
    }
}
