package com.sports.server.command.game.application;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sports.server.command.game.domain.Game;
import com.sports.server.command.game.domain.GameTeam;
import com.sports.server.command.game.domain.LineupPlayer;
import com.sports.server.command.game.dto.GameRequestDto;
import com.sports.server.command.league.domain.Round;
import com.sports.server.command.leagueteam.domain.LeagueTeam;
import com.sports.server.command.leagueteam.domain.LeagueTeamPlayer;
import com.sports.server.command.member.domain.Member;
import com.sports.server.common.application.EntityUtils;
import com.sports.server.common.exception.UnauthorizedException;
import com.sports.server.support.ServiceTest;
import com.sports.server.support.fixture.GameFixtureRepository;
import com.sports.server.support.fixture.GameTeamFixtureRepository;
import com.sports.server.support.fixture.LeagueTeamPlayerFixtureRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
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
        this.requestDto = new GameRequestDto.Register(nameOfGame, "16강", "전반전", "SCHEDULED",
                LocalDateTime.now(), idOfTeam1, idOfTeam2, null);
    }

    @Test
    @Transactional
    void 정상적으로_게임이_등록된다() {
        // given
        Member manager = entityUtils.getEntity(1L, Member.class);

        // when
        gameService.register(1L, requestDto, manager);

        // then
        Optional<Game> gameOptional = gameFixtureRepository.findByName(nameOfGame);
        assertTrue(gameOptional.isPresent(), "게임이 등록되지 않았습니다.");

        Game game = gameOptional.get();
        assertInFormationOfGame(game);

        List<GameTeam> gameTeams = gameTeamFixtureRepository.findByGame(game);
        assertGameTeams(gameTeams);
        assertLineupPlayers(gameTeams);
    }

    private void assertInFormationOfGame(Game game) {
        assertAll(
                () -> assertNotNull(game, "게임이 null 입니다."),
                () -> assertEquals(nameOfGame, game.getName(), "게임 이름이 일치하지 않습니다."),
                () -> assertEquals(Round.from("16강"), game.getRound(), "라운드가 일치하지 않습니다.")
        );
    }

    private void assertGameTeams(List<GameTeam> gameTeams) {
        List<Long> expectedTeamIds = List.of(idOfTeam1, idOfTeam2);
        List<Long> actualTeamIds = gameTeams.stream().map(gt -> gt.getLeagueTeam().getId()).toList();
        assertEquals(expectedTeamIds, actualTeamIds, "게임 팀 ID가 일치하지 않습니다.");
    }

    private void assertLineupPlayers(List<GameTeam> gameTeams) {
        for (GameTeam gameTeam : gameTeams) {
            LeagueTeam leagueTeam = gameTeam.getLeagueTeam();
            leagueTeamPlayerFixtureRepository.findByLeagueTeam(leagueTeam);
            List<Long> expectedPlayerIds = leagueTeam.getLeagueTeamPlayers().stream()
                    .map(LeagueTeamPlayer::getId).toList();
            List<Long> actualPlayerIds = gameTeam.getLineupPlayers().stream()
                    .map(LineupPlayer::getLeagueTeamPlayerId).toList();
            assertEquals(expectedPlayerIds, actualPlayerIds, "라인업 선수 ID가 일치하지 않습니다.");
        }
    }

    @Test
    void 리그의_매니저가_아닌_회원이_리그팀을_등록하려고_하면_예외가_발생한다() {
        // given
        Long leagueId = 1L;
        Member nonManager = entityUtils.getEntity(2L, Member.class);

        // when & then
        assertThrows(UnauthorizedException.class, () -> {
            gameService.register(leagueId, requestDto, nonManager);
        });
    }
}
