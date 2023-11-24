package com.sports.server.game.application;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sports.server.common.exception.CustomException;
import com.sports.server.game.dto.request.GamesQueryRequestDto;
import com.sports.server.game.dto.request.PageRequestDto;
import com.sports.server.game.dto.response.GameResponseDto;
import com.sports.server.support.ServiceTest;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

@Sql(scripts = "/game-fixture.sql")
public class GameServiceTest extends ServiceTest {

    @Autowired
    GameService gameService;
    private final Long validLeagueId = 1L;
    private final int size = 5;
    private final PageRequestDto pageRequestDto = new PageRequestDto(0L, size);
    private final List<Long> sportIds = List.of(1L, 2L);
    private final String stateValue = "SCHEDULED";
    private final GamesQueryRequestDto queryRequestDto = new GamesQueryRequestDto(validLeagueId, stateValue, sportIds);

    @Test
    void 존재하지_않는_게임_상태를_조회할_때_예외가_발생한다() {

        //given
        String invalidStateValue = "INVALID";
        GamesQueryRequestDto queryRequestDto = new GamesQueryRequestDto(validLeagueId, invalidStateValue, sportIds);

        // when
        assertThrows(CustomException.class,
                () -> gameService.getAllGames(queryRequestDto, pageRequestDto));

    }

    @Test
    void 날짜순으로_경기들이_반환된다() {

        //given
        List<Long> sportIds = List.of(1L);

        //when
        List<GameResponseDto> games = gameService.getAllGames(queryRequestDto, pageRequestDto);

        //then
        LocalDateTime startTimeOfFirstGame = games.get(0).startTime();
        LocalDateTime startTimeOfSecondGame = games.get(1).startTime();

        assertTrue(startTimeOfFirstGame.isEqual(startTimeOfSecondGame) || startTimeOfFirstGame
                .isBefore(startTimeOfSecondGame));

    }

    @Test
    void 날짜가_같은_경우_경기의_pk순으로_경기들이_반환된다() {

        //given
        List<Long> sportIds = List.of(1L);

        //when
        List<GameResponseDto> games = gameService.getAllGames(queryRequestDto, pageRequestDto);

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
        GamesQueryRequestDto queryRequestDto = new GamesQueryRequestDto(validLeagueId, stateValue, null);

        //when
        List<GameResponseDto> games = gameService.getAllGames(queryRequestDto, pageRequestDto);

        //then
        Assertions.assertEquals(
                games.size(), size
        );
    }

    @Test
    void 리그_아이디가_쿼리_스트링으로_조회되지_않는_경우_전체가_반환된다() {

        //given
        GamesQueryRequestDto queryRequestDto = new GamesQueryRequestDto(null, stateValue, null);

        //when
        List<GameResponseDto> games = gameService.getAllGames(queryRequestDto, pageRequestDto);

        //then
        Assertions.assertEquals(
                games.size(), size
        );

    }

    @Test
    void 하나의_경기에서_팀이_아이디_순으로_반환된다() {

        //given
        GamesQueryRequestDto queryRequestDto = new GamesQueryRequestDto(null, stateValue, null);

        //when
        List<GameResponseDto> games = gameService.getAllGames(queryRequestDto, pageRequestDto);

        //then
        Assertions.assertTrue(
                games.get(0).gameTeams().get(0).gameTeamId() < games.get(0).gameTeams().get(1).gameTeamId()
        );

    }

    @DisplayName("경기들을 페이징을 이용해서 조회할 때")
    class PagingTest {

        @Test
        void 세개만_조회한다() {

            // given
            GamesQueryRequestDto queryRequestDto = new GamesQueryRequestDto(null, stateValue, null);
            PageRequestDto pageRequestDto = new PageRequestDto(null, 3);

            //when
            List<GameResponseDto> games = gameService.getAllGames(queryRequestDto, pageRequestDto);

            //then
            Assertions.assertEquals(
                    games.size(), 3
            );


        }

        @Test
        void 기본적으로_10개를_조회한다() {

            // given
            GamesQueryRequestDto queryRequestDto = new GamesQueryRequestDto(null, stateValue, null);
            PageRequestDto pageRequestDto = new PageRequestDto(null, null);

            //when
            List<GameResponseDto> games = gameService.getAllGames(queryRequestDto, pageRequestDto);

            //then
            Assertions.assertEquals(
                    games.size(), 10
            );

        }


    }


}
