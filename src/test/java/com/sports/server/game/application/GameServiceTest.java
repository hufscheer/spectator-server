package com.sports.server.game.application;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sports.server.common.exception.CustomException;
import com.sports.server.game.dto.response.GameResponseDto;
import com.sports.server.support.ServiceTest;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

@Sql(scripts = "/game-fixture.sql")
public class GameServiceTest extends ServiceTest {

    @Autowired
    GameService gameService;

    @Test
    void 존재하지_않는_게임_상태를_조회할_때_예외가_발생한다() {

        //given
        Long leagueId = 1L;
        String invalidStateValue = "INVALID";
        List<Long> sportIds = List.of(1L);

        // when
        assertThrows(CustomException.class, () -> gameService.getAllGames(leagueId, invalidStateValue, sportIds));

    }

    @Test
    void 날짜순으로_경기들이_반환된다() {

        //given
        Long leagueId = 1L;
        String stateValue = "scheduled";
        List<Long> sportIds = List.of(1L);

        //when
        List<GameResponseDto> games = gameService.getAllGames(leagueId, stateValue, sportIds);

        //then
        LocalDateTime startTimeOfFirstGame = games.get(0).startTime();
        LocalDateTime startTimeOfSecondGame = games.get(1).startTime();

        assertTrue(startTimeOfFirstGame.isEqual(startTimeOfSecondGame) || startTimeOfFirstGame
                .isBefore(startTimeOfSecondGame));

    }

    @Test
    void 날짜가_같은_경우_경기의_pk순으로_경기들이_반환된다() {

        //given
        Long leagueId = 1L;
        String stateValue = "scheduled";
        List<Long> sportIds = List.of(1L);

        //when
        List<GameResponseDto> games = gameService.getAllGames(leagueId, stateValue, sportIds);

        //then
        GameResponseDto firstGame = games.get(0);
        GameResponseDto secondGame = games.get(1);

        if (firstGame.startTime().equals(secondGame.startTime())) {
            assertTrue(
                    firstGame.id() < secondGame.id()
            );
        } else {
            assertTrue(
                    firstGame.startTime().isBefore(secondGame.startTime())
            );
        }

    }

    @Test
    void 스포츠_아이디가_쿼리_스트링으로_조회되지_않는_경우_전체가_반환된다() {

        //given
        Long leagueId = 1L;
        String stateValue = "scheduled";

        //when
        List<GameResponseDto> games = gameService.getAllGames(leagueId, stateValue, null);

        //then
        Assertions.assertEquals(
                games.size(), 3
        );
    }

    @Test
    void 리그_아이디가_쿼리_스트링으로_조회되지_않는_경우_전체가_반환된다() {

        //given
        String stateValue = "scheduled";

        //when
        List<GameResponseDto> games = gameService.getAllGames(null, stateValue, null);

        //then
        Assertions.assertEquals(
                games.size(), 4
        );

    }

    @Test
    void 하나의_경기에서_팀이_아이디_순으로_반환된다() {

        //given
        String stateValue = "scheduled";

        //when
        List<GameResponseDto> games = gameService.getAllGames(null, stateValue, null);

        //then
        Assertions.assertTrue(
                games.get(0).gameTeams().get(0).gameTeamId() < games.get(0).gameTeams().get(1).gameTeamId()
        );

    }
}
