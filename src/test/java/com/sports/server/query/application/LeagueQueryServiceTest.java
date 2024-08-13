package com.sports.server.query.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.sports.server.command.game.domain.GameState;
import com.sports.server.command.member.domain.Member;
import com.sports.server.common.application.EntityUtils;
import com.sports.server.query.dto.response.LeagueResponseWithGames;
import com.sports.server.query.dto.response.LeagueResponseWithInProgressGames;
import com.sports.server.query.dto.response.LeagueResponseWithInProgressGames.GameDetailResponse;
import com.sports.server.query.dto.response.LeagueResponseWithInProgressGames.GameDetailResponse.GameTeamResponse;
import com.sports.server.query.dto.response.LeagueTeamDetailResponse;
import com.sports.server.query.dto.response.LeagueTeamDetailResponse.LeagueTeamPlayerResponse;
import com.sports.server.query.dto.response.LeagueTeamResponse;
import com.sports.server.support.ServiceTest;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
                () -> assertThat(leagueTeam.sizeOfLeagueTeamPlayers()).isEqualTo(4)
        );

    }

    @Nested
    @DisplayName("매니저가 생성한 리그만을 조회할 때")
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
            assertFalse(ids.contains(8L), "다른 매니저가 생성한 리그는 조회 되어서는 안됩니다.");
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
    @DisplayName("리그팀의 상세 정보를 조회할 때")
    class LeagueTeamDetailedQueryTest {
        @Test
        void 정상적으로_조회된다() {
            // given
            Long leagueTeamId = 3L;

            // when
            LeagueTeamDetailResponse leagueTeam = leagueQueryService.findLeagueTeam(leagueTeamId);

            // then
            assertAll(
                    () -> assertThat(leagueTeam.teamName()).isEqualTo("미컴 축구생각"),
                    () -> assertThat(leagueTeam.logoImageUrl()).isEqualTo("이미지이미지"),
                    () -> assertThat(leagueTeam.leagueTeamPlayers()).hasSize(4),
                    () -> {
                        assertThat(leagueTeam.leagueTeamPlayers())
                                .extracting("id", "name", "number")
                                .containsExactlyInAnyOrder(
                                        tuple(1L, "봄동나물진승희", 0),
                                        tuple(2L, "가을전어이동규", 2),
                                        tuple(3L, "겨울붕어빵이현제", 3),
                                        tuple(4L, "여름수박고병룡", 3)
                                );
                    }
            );
        }

        @Test
        void 리그팀_선수가_ㄱㄴㄷ순으로_반환된다() {
            // given
            Long leagueTeamId = 1L;

            // when
            LeagueTeamDetailResponse leagueTeam = leagueQueryService.findLeagueTeam(leagueTeamId);

            // then
            assertAll(
                    () -> {
                        List<String> playerNames = leagueTeam.leagueTeamPlayers()
                                .stream()
                                .map(LeagueTeamPlayerResponse::name)
                                .toList();
                        assertThat(playerNames).isSortedAccordingTo(String.CASE_INSENSITIVE_ORDER);
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
                    .map(LeagueResponseWithGames.GameDetailResponse::id).toList());

            ids.addAll(leagueAndGames.scheduledGames().stream()
                    .map(LeagueResponseWithGames.GameDetailResponse::id).toList());

            ids.addAll(leagueAndGames
                    .finishedGames().stream().map(LeagueResponseWithGames.GameDetailResponse::id).toList());

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
}
