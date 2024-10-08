package com.sports.server.command.game.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.sports.server.command.game.domain.Game;
import com.sports.server.command.game.domain.GameState;
import com.sports.server.command.game.domain.GameTeam;
import com.sports.server.command.game.domain.LineupPlayer;
import com.sports.server.command.game.dto.GameRequestDto;
import com.sports.server.command.league.domain.Round;
import com.sports.server.command.leagueteam.domain.LeagueTeam;
import com.sports.server.command.leagueteam.domain.LeagueTeamPlayer;
import com.sports.server.command.member.domain.Member;
import com.sports.server.common.application.EntityUtils;
import com.sports.server.common.exception.CustomException;
import com.sports.server.common.exception.NotFoundException;
import com.sports.server.common.exception.UnauthorizedException;
import com.sports.server.support.ServiceTest;
import com.sports.server.support.fixture.GameFixtureRepository;
import com.sports.server.support.fixture.GameTeamFixtureRepository;
import com.sports.server.support.fixture.LeagueTeamPlayerFixtureRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

@Sql("/game-fixture.sql")
public class GameServiceTest extends ServiceTest {

    @Autowired
    private GameService gameService;

    @Autowired
    private EntityUtils entityUtils;

    @Autowired
    private GameFixtureRepository gameFixtureRepository;

    @Autowired
    private GameTeamFixtureRepository gameTeamFixtureRepository;

    @Autowired
    private LeagueTeamPlayerFixtureRepository leagueTeamPlayerFixtureRepository;

    private GameRequestDto.Register requestDto;
    private String nameOfGame;
    private Long idOfTeam1;
    private Long idOfTeam2;

    @BeforeEach
    void setUp() {
        this.nameOfGame = "경기 이름";
        this.idOfTeam1 = 1L;
        this.idOfTeam2 = 2L;
        this.requestDto = new GameRequestDto.Register(nameOfGame, 16, "전반전", "SCHEDULED",
                LocalDateTime.now(), idOfTeam1, idOfTeam2, null);
    }

    @Nested
    @DisplayName("게임을 저장할 때")
    class registerGameTest {
        @Test
        void 정상적으로_게임팀이_저장된다() {
            // given
            Member manager = entityUtils.getEntity(1L, Member.class);

            // when
            gameService.register(1L, requestDto, manager);

            // then
            Optional<Game> gameOptional = gameFixtureRepository.findByName(nameOfGame);
            assertThat(gameOptional).isPresent().withFailMessage("게임이 등록되지 않았습니다.");

            Game game = gameOptional.get();
            assertInFormationOfGame(game);

            List<GameTeam> gameTeams = gameTeamFixtureRepository.findByGame(game);
            assertGameTeams(gameTeams);
        }

        @Test
        void 정상적으로_라인업이_복사된다() {
            // given
            Member manager = entityUtils.getEntity(1L, Member.class);

            // when
            gameService.register(1L, requestDto, manager);

            // then
            Optional<Game> gameOptional = gameFixtureRepository.findByName(nameOfGame);
            assertThat(gameOptional).isPresent().withFailMessage("게임이 등록되지 않았습니다.");

            List<GameTeam> gameTeams = gameTeamFixtureRepository.findByGame(gameOptional.get());
            assertLineupPlayers(gameTeams);
        }

        private void assertInFormationOfGame(Game game) {
            assertAll(
                    () -> assertThat(game).isNotNull(),
                    () -> assertThat(game.getName()).isEqualTo(nameOfGame),
                    () -> assertThat(game.getRound()).isEqualTo(Round.from(16))
            );

        }

        private void assertGameTeams(List<GameTeam> gameTeams) {
            List<Long> expectedTeamIds = List.of(idOfTeam1, idOfTeam2);
            List<Long> actualTeamIds = gameTeams.stream().map(gt -> gt.getLeagueTeam().getId()).toList();
            assertThat(actualTeamIds).isEqualTo(expectedTeamIds);
        }

        private void assertLineupPlayers(List<GameTeam> gameTeams) {
            for (GameTeam gameTeam : gameTeams) {
                LeagueTeam leagueTeam = gameTeam.getLeagueTeam();
                List<Long> expectedPlayerIds = leagueTeamPlayerFixtureRepository.findByLeagueTeam(leagueTeam).stream()
                        .map(LeagueTeamPlayer::getId).toList();
                List<Long> actualPlayerIds = gameTeam.getLineupPlayers().stream()
                        .map(LineupPlayer::getLeagueTeamPlayerId).toList();
                assertThat(actualPlayerIds).isEqualTo(expectedPlayerIds);
            }
        }

        @Test
        void 리그의_매니저가_아닌_회원이_리그팀을_등록하려고_하면_예외가_발생한다() {
            // given
            Long leagueId = 1L;
            Member nonManager = entityUtils.getEntity(2L, Member.class);

            // when & then
            assertThatThrownBy(() -> gameService.register(leagueId, requestDto, nonManager))
                    .isInstanceOf(UnauthorizedException.class);
        }

        @Test
        void maxRound보다_큰_round의_경기를_등록할_수_없다() {
            // given
            Long leagueId = 1L;
            Member manager = entityUtils.getEntity(1L, Member.class);
            GameRequestDto.Register requestDto = new GameRequestDto.Register(nameOfGame, 32, "전반전", "SCHEDULED",
                    LocalDateTime.now(), idOfTeam1, idOfTeam2, null);

            // when & then
            assertThatThrownBy(() -> gameService.register(leagueId, requestDto, manager))
                    .isInstanceOf(CustomException.class)
                    .hasMessage("최대 라운드보다 더 큰 라운드의 경기를 등록할 수 없습니다.");
        }
    }

    @Nested
    @DisplayName("게임을 수정할 때")
    class updateGameTest {

        private GameRequestDto.Update updateDto;
        private Long leagueId;
        private Long gameId;
        private Member manager;

        @BeforeEach
        void setUp() {
            LocalDateTime fixedLocalDateTime = LocalDateTime.of(2024, 9, 11, 12, 0, 0);
            updateDto = new GameRequestDto.Update(nameOfGame, 8, "후반전", "PLAYING",
                    fixedLocalDateTime, "videoId");
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
            assertAll(
                    () -> assertThat(game.getGameQuarter()).isEqualTo(updateDto.quarter()),
                    () -> assertThat(game.getRound()).isEqualTo(Round.from(updateDto.round())),
                    () -> assertThat(game.getName()).isEqualTo(updateDto.name()),
                    () -> assertThat(game.getStartTime()).isEqualTo(updateDto.startTime()),
                    () -> assertThat(game.getState()).isEqualTo(GameState.from(updateDto.state())),
                    () -> assertThat(game.getVideoId()).isEqualTo(updateDto.videoId())
            );
        }

        @Test
        void 게임이_속한_리그의_매니저가_아닌_회원이_게임을_수정하려고_하면_예외가_발생한다() {
            // given
            Member nonManager = entityUtils.getEntity(2L, Member.class);

            // when & then
            assertThatThrownBy(() -> gameService.updateGame(leagueId, gameId, updateDto, nonManager))
                    .isInstanceOf(UnauthorizedException.class);
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
        assertThatThrownBy(
                () -> entityUtils.getEntity(gameId, Game.class))
                .isInstanceOf(NotFoundException.class);
    }

}
