package com.sports.server.game.application;

import com.sports.server.common.dto.PageRequestDto;
import com.sports.server.common.exception.CustomException;
import com.sports.server.game.dto.request.GamesQueryRequestDto;
import com.sports.server.game.dto.response.GameResponseDto;
import com.sports.server.game.dto.response.GameResponseDto.TeamResponse;
import com.sports.server.support.ServiceTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Sql(scripts = "/game-fixture.sql")
public class GameServiceTest extends ServiceTest {

    @Autowired
    GameService gameService;
    private final Long validLeagueId = 1L;
    private final int size = 5;
    private final PageRequestDto pageRequestDto = new PageRequestDto(null, size);
    private final List<Long> sportIds = List.of(1L, 2L);
    private final String stateValue = "SCHEDULED";

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
        PageRequestDto pageRequestDto = new PageRequestDto(null, 5);
        GamesQueryRequestDto queryRequestDto = new GamesQueryRequestDto(1L, "SCHEDULED", List.of(1L));

        //when
        List<GameResponseDto> games = gameService.getAllGames(queryRequestDto, pageRequestDto);

        // then
        assertThat(games).isNotNull();
        assertThat(games).hasSize(5);

        assertThat(games).extracting(GameResponseDto::id)
                .containsExactly(1L, 2L, 3L, 4L, 6L);
    }

    
    @Test
    void 시작_날짜가_같은_경우_pk순으로_경기가_반환된다() {

        //given
        PageRequestDto pageRequestDto = new PageRequestDto(2L, 5);
        GamesQueryRequestDto queryRequestDto = new GamesQueryRequestDto(1L, "SCHEDULED", List.of(1L));

        //when
        List<GameResponseDto> games = gameService.getAllGames(queryRequestDto, pageRequestDto);

        // then
        assertThat(games).isNotNull();
        assertThat(games).hasSize(5);

        assertThat(games).extracting(GameResponseDto::id)
                .containsExactly(3L, 4L, 6L, 7L, 5L);
    }

    
    @Test
    void 커서를_이용해서_조회하는_경우_경기_시작_시간이_빠른_경기가_아이디가_커서보다_큰_경기보다_먼저_반환된다() {

        //given
        int size = 5;
        PageRequestDto pageRequestDto = new PageRequestDto(null, size);
        GamesQueryRequestDto queryRequestDto = new GamesQueryRequestDto(1L, "SCHEDULED", List.of(1L));
        GameResponseDto gameResponseDto = gameService.getAllGames(queryRequestDto, pageRequestDto).get(size - 1);
        Long cursor = gameResponseDto.id();

        //when
        pageRequestDto = new PageRequestDto(cursor, size);
        List<GameResponseDto> games = gameService.getAllGames(queryRequestDto, pageRequestDto);

        // then
        assertThat(games).extracting(GameResponseDto::id)
                .containsExactly(7L, 5L, 8L, 9L, 10L);

    }

    
    @Test
    void 스포츠_아이디가_쿼리_스트링으로_조회되지_않는_경우_전체가_반환된다() {

        //given
        GamesQueryRequestDto queryRequestDto = new GamesQueryRequestDto(validLeagueId, stateValue, null);

        //when
        List<GameResponseDto> games = gameService.getAllGames(queryRequestDto, pageRequestDto);

        //then
        assertEquals(
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
        assertEquals(
                games.size(), size
        );

    }

    
    @Test
    void 스포츠_아이디가_여러개인_경우_스포츠에_해당하는_전체_경기가_반환된다() {

        //given
        PageRequestDto pageRequestDto = new PageRequestDto(1L, 15);
        GamesQueryRequestDto queryRequestDto = new GamesQueryRequestDto(1L, "SCHEDULED", List.of(1L, 2L));

        //when
        List<GameResponseDto> games = gameService.getAllGames(queryRequestDto, pageRequestDto);

        //then
        assertThat(games).isNotNull();

        assertThat(games).extracting(GameResponseDto::id)
                .containsExactly(2L, 3L, 4L, 6L, 7L, 5L, 8L, 9L, 10L, 11L, 12L, 13L);
    }

    @Test
    void 하나의_경기에서_팀이_아이디_순으로_반환된다() {

        //given
        GamesQueryRequestDto queryRequestDto = new GamesQueryRequestDto(null, stateValue, null);

        //when
        List<GameResponseDto> games = gameService.getAllGames(queryRequestDto, pageRequestDto);

        //then
        for (GameResponseDto game : games) {
            List<GameResponseDto.TeamResponse> gameTeams = game.gameTeams();

            List<Long> teamIds = gameTeams.stream()
                    .map(GameResponseDto.TeamResponse::gameTeamId)
                    .collect(Collectors.toList());

            assertThat(teamIds).isSorted();
        }

    }

    
    @Test
    void 조회한_경기상태에_해당하는_경기만_반환한다() {

        //given
        GamesQueryRequestDto queryRequestDto = new GamesQueryRequestDto(null, "FINISHED", null);

        //when
        List<GameResponseDto> games = gameService.getAllGames(queryRequestDto, pageRequestDto);

        //then
        assertThat(games).extracting(GameResponseDto::id)
                .containsExactly(15L, 14L);

    }

    @Test
    void 경기에_참여한_팀들의_순서가_알맞게_반환된다() {

        //given
        GamesQueryRequestDto queryRequestDto = new GamesQueryRequestDto(null, "FINISHED", null);

        //when
        List<GameResponseDto> games = gameService.getAllGames(queryRequestDto, pageRequestDto);

        // then
        List<List<TeamResponse>> teamResponsesOfGames = games.stream()
                .map(GameResponseDto::gameTeams).toList();

        for (List<TeamResponse> teamResponses : teamResponsesOfGames) {

            assertThat(teamResponses).isSortedAccordingTo(
                    Comparator.comparingLong(TeamResponse::gameTeamId));

            for (int i = 0; i < teamResponses.size(); i++) {
                assertEquals(i + 1, teamResponses.get(i).order());
            }
        }
    }

    @Nested
    @DisplayName("경기들을 페이징을 이용해서 조회할 때")
    class PagingTest {

        
        @Test
        void 세개만_조회한다() {

            // given
            GamesQueryRequestDto queryRequestDto = new GamesQueryRequestDto(null, stateValue, null);
            PageRequestDto pageRequestDto = new PageRequestDto(1L, 3);

            //when
            List<GameResponseDto> games = gameService.getAllGames(queryRequestDto, pageRequestDto);

            //then
            assertEquals(
                    games.size(), 3
            );


        }

        
        @Test
        void 기본적으로_10개를_조회한다() {

            // given
            GamesQueryRequestDto queryRequestDto = new GamesQueryRequestDto(null, stateValue, null);
            PageRequestDto pageRequestDto = new PageRequestDto(1L, null);

            //when
            List<GameResponseDto> games = gameService.getAllGames(queryRequestDto, pageRequestDto);

            //then
            assertEquals(
                    games.size(), 10
            );

        }

    }


}
