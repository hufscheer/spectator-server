package com.sports.server.command.game.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.when;

import com.sports.server.command.game.domain.Game;
import com.sports.server.command.game.domain.GameResult;
import com.sports.server.command.game.domain.GameState;
import com.sports.server.command.game.domain.GameTeam;
import com.sports.server.command.game.domain.LineupPlayerState;
import com.sports.server.command.game.dto.GameRequest;
import com.sports.server.command.league.domain.League;
import com.sports.server.command.league.domain.Round;
import com.sports.server.command.member.domain.Member;
import com.sports.server.command.team.domain.TeamPlayer;
import com.sports.server.common.application.EntityUtils;
import com.sports.server.common.exception.CustomException;
import com.sports.server.common.exception.NotFoundException;
import com.sports.server.common.exception.UnauthorizedException;
import com.sports.server.support.ServiceTest;
import com.sports.server.support.fixture.GameFixtureRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

@Sql("/game-fixture.sql")
public class GameServiceTest extends ServiceTest {

    @Autowired
    private GameService gameService;

    @Autowired
    private EntityUtils entityUtils;

    @Autowired
    private GameFixtureRepository gameFixtureRepository;

    @MockBean
    private Clock clock;

    private GameRequest.Register requestDto;
    private String nameOfGame;
    private GameRequest.TeamLineupRequest team1;
    private GameRequest.TeamLineupRequest team2;

    @BeforeEach
    void setUp() {
        this.nameOfGame = "경기 이름";

        List<GameRequest.LineupPlayerRequest> team1LineupPlayers = List.of(
                new GameRequest.LineupPlayerRequest(1L, LineupPlayerState.STARTER, true),
                new GameRequest.LineupPlayerRequest(2L, LineupPlayerState.STARTER, false)
        );
        team1 = new GameRequest.TeamLineupRequest(1L, team1LineupPlayers);

        List<GameRequest.LineupPlayerRequest> team2Players = List.of(
                new GameRequest.LineupPlayerRequest(6L, LineupPlayerState.STARTER, true),
                new GameRequest.LineupPlayerRequest(7L, LineupPlayerState.CANDIDATE, false)
        );
        team2 = new GameRequest.TeamLineupRequest(2L, team2Players);

        this.requestDto = new GameRequest.Register(nameOfGame, 16, "전반전", "SCHEDULED", LocalDateTime.now(), null, team1, team2);
        LocalDateTime fixedNow = LocalDateTime.of(2024, 11, 26, 0, 0, 0, 0);
        Clock fixedClock = Clock.fixed(fixedNow.atZone(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());

        when(clock.instant()).thenReturn(fixedClock.instant());
        when(clock.getZone()).thenReturn(fixedClock.getZone());
    }

    @Nested
    @DisplayName("게임을 저장할 때")
    @Transactional
    class registerGameTest {
        @Test
        void 정상적으로_게임팀이_저장된다() {
            // given
            Member manager = entityUtils.getEntity(1L, Member.class);

            // when
            Long gameId = gameService.register(1L, requestDto, manager);

            // then
            Game game = entityUtils.getEntity(gameId, Game.class);
            assertInFormationOfGame(game);

            List<GameTeam> gameTeams = game.getGameTeams();
            assertGameTeams(gameTeams);
        }

        @Test
        void 정상적으로_라인업이_등록된다() {
            // given
            Member manager = entityUtils.getEntity(1L, Member.class);

            // when
            Long gameId = gameService.register(1L, requestDto, manager);

            // then
            Game game = entityUtils.getEntity(gameId, Game.class);

            List<GameTeam> gameTeams = game.getGameTeams();
            assertLineupPlayers(gameTeams);
        }

        private void assertInFormationOfGame(Game game) {
            assertAll(() -> assertThat(game).isNotNull(), () -> assertThat(game.getName()).isEqualTo(nameOfGame),
                    () -> assertThat(game.getRound()).isEqualTo(Round.from(16)));

        }

        private void assertGameTeams(List<GameTeam> gameTeams) {
            List<Long> expectedTeamIds = List.of(team1.teamId(), team2.teamId());
            List<Long> actualTeamIds = gameTeams.stream().map(gt -> gt.getTeam().getId()).toList();
            assertThat(actualTeamIds).isEqualTo(expectedTeamIds);
        }

        private void assertLineupPlayers(List<GameTeam> gameTeams) {
            for (GameTeam gameTeam : gameTeams) {
                GameRequest.TeamLineupRequest teamLineupRequest = gameTeam.getTeam().getId().equals(team1.teamId()) 
                        ? team1 : team2;

                Set<Long> expectedPlayerIds = teamLineupRequest.lineupPlayers().stream()
                        .map(lineupPlayerRequest -> {
                            TeamPlayer teamPlayer = entityUtils.getEntity(lineupPlayerRequest.teamPlayerId(), 
                                    TeamPlayer.class);
                            return teamPlayer.getPlayer().getId();
                        })
                        .collect(Collectors.toSet());

                Set<Long> actualPlayerIds = gameTeam.getLineupPlayers().stream()
                        .map(lineupPlayer -> lineupPlayer.getPlayer().getId())
                        .collect(Collectors.toSet());

                assertThat(actualPlayerIds).isEqualTo(expectedPlayerIds);
            }
        }

        @Test
        void 리그의_매니저가_아닌_회원이_리그팀을_등록하려고_하면_예외가_발생한다() {
            // given
            Long leagueId = 1L;
            Member nonManager = entityUtils.getEntity(2L, Member.class);

            // when & then
            assertThatThrownBy(() -> gameService.register(leagueId, requestDto, nonManager)).isInstanceOf(
                    UnauthorizedException.class);
        }

        @Test
        void maxRound보다_큰_round의_경기를_등록할_수_없다() {
            // given
            Long leagueId = 1L;
            Member manager = entityUtils.getEntity(1L, Member.class);
            GameRequest.Register requestDto = new GameRequest.Register(nameOfGame, 32, "전반전", "SCHEDULED",
                    LocalDateTime.now(), null, team1, team2);

            // when & then
            assertThatThrownBy(() -> gameService.register(leagueId, requestDto, manager)).isInstanceOf(
                    CustomException.class).hasMessage("최대 라운드보다 더 큰 라운드의 경기를 등록할 수 없습니다.");
        }
    }

    @Nested
    @DisplayName("게임을 수정할 때")
    class updateGameTest {

        private GameRequest.Update updateDto;
        private Long leagueId;
        private Long gameId;
        private Member manager;

        @BeforeEach
        void setUp() {
            LocalDateTime fixedLocalDateTime = LocalDateTime.of(2024, 9, 11, 12, 0, 0);
            updateDto = new GameRequest.Update(nameOfGame, 8, "후반전", "PLAYING", fixedLocalDateTime, "videoId");
            leagueId = 1L;
            gameId = 1L;
            manager = entityUtils.getEntity(1L, Member.class);
        }

        @Test
        void 정상적으로_게임이_수정된다() {
            // when
            gameService.updateGame(leagueId, gameId, updateDto, manager);

            // then
            Game game = entityUtils.getEntity(gameId, Game.class);
            assertAll(() -> assertThat(game.getGameQuarter()).isEqualTo(updateDto.quarter()),
                    () -> assertThat(game.getRound()).isEqualTo(Round.from(updateDto.round())),
                    () -> assertThat(game.getName()).isEqualTo(updateDto.name()),
                    () -> assertThat(game.getStartTime()).isEqualTo(updateDto.startTime()),
                    () -> assertThat(game.getState()).isEqualTo(GameState.from(updateDto.state())),
                    () -> assertThat(game.getVideoId()).isEqualTo(updateDto.videoId()));
        }

        @Test
        void 게임을_직접_종료하면_결과가_저장된다() {
            // given
            GameRequest.Update finishRequest = new GameRequest.Update(
                    nameOfGame, 4, "경기후", "FINISHED", LocalDateTime.of(2024, 9, 11, 12, 0, 0), "videoId"
            );

            // when
            gameService.updateGame(leagueId, gameId, finishRequest, manager);

            // then
            Game game = entityUtils.getEntity(gameId, Game.class);
            GameTeam firstGameTeam = entityUtils.getEntity(1L, GameTeam.class);
            GameTeam secondGameTeam = entityUtils.getEntity(2L, GameTeam.class);

            assertAll(
                    () -> assertThat(game.getState()).isEqualTo(GameState.FINISHED),
                    () -> assertThat(firstGameTeam.getResult()).isEqualTo(GameResult.LOSE),
                    () -> assertThat(secondGameTeam.getResult()).isEqualTo(GameResult.WIN)
            );
        }

        @Test
        void 게임이_속한_리그의_매니저가_아닌_회원이_게임을_수정하려고_하면_예외가_발생한다() {
            // given
            Member nonManager = entityUtils.getEntity(2L, Member.class);

            // when & then
            assertThatThrownBy(() -> gameService.updateGame(leagueId, gameId, updateDto, nonManager)).isInstanceOf(
                    UnauthorizedException.class);
        }
    }

    @Test
    void 정상적으로_게임이_삭제된다() {
        // given
        Long leagueId = 1L;
        Long gameId = 1L;
        Member manager = entityUtils.getEntity(1L, Member.class);

        // when
        gameService.deleteGame(leagueId, gameId, manager);

        // then
        assertThatThrownBy(() -> entityUtils.getEntity(gameId, Game.class)).isInstanceOf(NotFoundException.class);
    }

    @Nested
    @DisplayName("게임 상태를 업데이트할 때")
    class UpdateGameStatusToFinishTest {
        @Test
        @DisplayName("정상적으로 시작한지 5시간이 지난 게임의 상태가 FINISHED로 변경된다")
        void updateGamesOlderThanFiveHoursToFinished() {
            // given
            LocalDateTime now = LocalDateTime.now(clock);

            League league = entityUtils.getEntity(1L, League.class);
            Member manager = entityUtils.getEntity(1L, Member.class);
            Round round = Round.ROUND_16;

            Game recentGame = new Game(manager, league, "종료되면 안되는 경기", now.minusHours(4), "videoId", "전반전",
                    GameState.PLAYING, round, false);
            Game oldGame1 = new Game(manager, league, "오래된 경기1", now.minusHours(5), "videoId", "전반전",
                    GameState.PLAYING, round, false);
            Game oldGame2 = new Game(manager, league, "오래된 경기2", now.minusHours(6), "videoId", "전반전",
                    GameState.PLAYING, round, false);

            gameFixtureRepository.save(recentGame);
            gameFixtureRepository.save(oldGame1);
            gameFixtureRepository.save(oldGame2);

            // when
            gameService.updateGameStatusToFinish(now);

            // then
            assertAll(
                    () -> assertThat(gameFixtureRepository.findByName("종료되면 안되는 경기").orElse(null).getState()).isEqualTo(
                            GameState.PLAYING),
                    () -> assertThat(gameFixtureRepository.findByName("오래된 경기1").orElse(null).getState()).isEqualTo(
                            GameState.FINISHED),
                    () -> assertThat(gameFixtureRepository.findByName("오래된 경기2").orElse(null).getState()).isEqualTo(
                            GameState.FINISHED));
        }
    }

}
