package com.sports.server.query.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.sports.server.common.dto.PageRequestDto;
import com.sports.server.common.exception.CustomException;
import com.sports.server.query.dto.request.GamesQueryRequestDto;
import com.sports.server.query.dto.response.GameResponseDto;
import com.sports.server.query.dto.response.GameResponseDto.TeamResponse;
import com.sports.server.query.dto.response.LeagueWithGamesResponse;
import com.sports.server.support.ServiceTest;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

@Sql(scripts = "/game-fixture.sql")
public class GameQueryServiceTest extends ServiceTest {

    @Autowired
    private GameQueryService gameQueryService;
    private final Long validLeagueId = 1L;
    private final int size = 5;
    private final PageRequestDto pageRequestDto = new PageRequestDto(null, size);
    private final String stateValue = "SCHEDULED";
    private final List<Long> leagueTeamIds = List.of(1L, 2L);

    @Test
    void 존재하지_않는_게임_상태를_조회할_때_예외가_발생한다() {
        //given
        String invalidStateValue = "INVALID";
        GamesQueryRequestDto queryRequestDto = new GamesQueryRequestDto(validLeagueId, invalidStateValue,
                null, null);

        // when
        assertThrows(CustomException.class,
                () -> gameQueryService.getAllGames(queryRequestDto, pageRequestDto));
    }

    @Test
    void 날짜순으로_게임들이_반환된다() {
        //given
        Long leagueId = 1L;
        PageRequestDto pageRequestDto = new PageRequestDto(null, 5);
        GamesQueryRequestDto queryRequestDto = new GamesQueryRequestDto(leagueId, "SCHEDULED",
                leagueTeamIds, null);

        //when
        List<LeagueWithGamesResponse> response = gameQueryService.getAllGames(queryRequestDto, pageRequestDto);

        // then
        assertAll(
                () -> assertThat(response).hasSize(1),
                () -> {
                    LeagueWithGamesResponse leagueGames = response.get(0);
                    assertThat(leagueGames.leagueId()).isEqualTo(1L);
                    assertThat(leagueGames.leagueName()).isEqualTo("삼건물 대회");
                    assertThat(leagueGames.games()).hasSize(5);
                    assertThat(leagueGames.games()).map(GameResponseDto::id)
                            .containsExactlyInAnyOrder(24L, 13L, 12L, 11L, 10L);
                }
        );
    }

    @Test
    void 시작_날짜가_같은_경우_pk_내림차순_순으로_게임이_반환된다() {
        //given
        Long leagueId = 1L;
        PageRequestDto pageRequestDto = new PageRequestDto(2L, 5);
        GamesQueryRequestDto queryRequestDto = new GamesQueryRequestDto(leagueId, "SCHEDULED", leagueTeamIds,
                null);

        //when
        List<LeagueWithGamesResponse> response = gameQueryService.getAllGames(queryRequestDto, pageRequestDto);

        // then
        assertAll(
                () -> assertThat(response).hasSize(1),
                () -> {
                    LeagueWithGamesResponse leagueGames = response.get(0);
                    assertThat(leagueGames).isNotNull();
                    assertThat(leagueGames.games()).hasSize(5);
                    assertThat(leagueGames.games()).extracting(GameResponseDto::id)
                            .containsExactly(24L, 13L, 12L, 11L, 10L);
                }
        );
    }

    @Test
    void 커서를_이용해서_조회한다() {
        // given
        int size = 7;
        GamesQueryRequestDto queryRequestDto = new GamesQueryRequestDto(null, stateValue, null, null);

        // when
        List<LeagueWithGamesResponse> firstPage = gameQueryService.getAllGames(
                queryRequestDto,
                new PageRequestDto(null, size)
        );

        List<GameResponseDto> allGamesInFirstPage = firstPage.stream()
                .flatMap(leagueWithGames -> leagueWithGames.games().stream())
                .toList();
        Long cursor = allGamesInFirstPage.get(allGamesInFirstPage.size() - 1).id();

        List<LeagueWithGamesResponse> secondPage = gameQueryService.getAllGames(
                queryRequestDto,
                new PageRequestDto(cursor, size)
        );

        // then
        List<Long> secondPageGameIds = secondPage.stream()
                .flatMap(leagueWithGames -> leagueWithGames.games().stream())
                .map(GameResponseDto::id)
                .toList();

        assertAll(
                () -> assertThat(allGamesInFirstPage.stream().map(GameResponseDto::id).toList())
                        .containsExactly(27L, 24L, 17L, 16L, 18L, 19L, 13L),
                () -> assertThat(secondPageGameIds)
                        .containsExactly(27L, 24L, 17L, 16L, 18L, 19L)
        );
    }

    @Test
    void 리그_아이디가_쿼리_스트링으로_조회되지_않는_경우_전체가_반환된다() {
        //given
        GamesQueryRequestDto queryRequestDto = new GamesQueryRequestDto(null, stateValue, leagueTeamIds, null);

        //when
        List<LeagueWithGamesResponse> response = gameQueryService.getAllGames(queryRequestDto, pageRequestDto);

        //then
        assertAll(
                () -> assertThat(response).hasSize(1),
                () -> {
                    LeagueWithGamesResponse leagueGames = response.get(0);
                    assertThat(leagueGames.games()).hasSize(size);
                }
        );
    }

//    @Test
//    void 하나의_게임에서_팀이_아이디_순으로_반환된다() {
//
//        //given
//        GamesQueryRequestDto queryRequestDto = new GamesQueryRequestDto(null, stateValue, leagueTeamIds, null);
//
//        //when
//        List<LeagueWithGamesResponse> response = gameQueryService.getAllGames(queryRequestDto, pageRequestDto);
//
//        //then
//        for (GameResponseDto game : games) {
//            List<GameResponseDto.TeamResponse> gameTeams = game.gameTeams();
//
//            List<Long> teamIds = gameTeams.stream()
//                    .map(GameResponseDto.TeamResponse::gameTeamId)
//                    .collect(Collectors.toList());
//
//            assertThat(teamIds).isSorted();
//        }
//
//    }
//
//    @Test
//    void 게임에_참여한_팀들의_순서가_알맞게_반환된다() {
//
//        //given
//        GamesQueryRequestDto queryRequestDto = new GamesQueryRequestDto(null, "FINISHED", leagueTeamIds, null);
//
//        //when
//        List<GameResponseDto> games = gameQueryService.getAllGames(queryRequestDto, pageRequestDto);
//
//        // then
//        List<List<TeamResponse>> teamResponsesOfGames = games.stream()
//                .map(GameResponseDto::gameTeams).toList();
//
//        for (List<TeamResponse> teamResponses : teamResponsesOfGames) {
//
//            assertThat(teamResponses).isSortedAccordingTo(
//                    Comparator.comparingLong(TeamResponse::gameTeamId));
//        }
//    }
//
//    @Nested
//    @DisplayName("게임들을 페이징을 이용해서 조회할 때")
//    class PagingTest {
//
//
//        @Test
//        void 세개만_조회한다() {
//
//            // given
//            GamesQueryRequestDto queryRequestDto = new GamesQueryRequestDto(null, stateValue, leagueTeamIds, null);
//            PageRequestDto pageRequestDto = new PageRequestDto(1L, 3);
//
//            //when
//            List<GameResponseDto> games = gameQueryService.getAllGames(queryRequestDto, pageRequestDto);
//
//            //then
//            assertThat(games)
//                    .map(GameResponseDto::id)
//                    .containsExactly(2L, 3L, 4L);
//        }
//
//
//        @Test
//        void 기본적으로_10개를_조회한다() {
//
//            // given
//            GamesQueryRequestDto queryRequestDto = new GamesQueryRequestDto(null, stateValue, leagueTeamIds,
//                    null);
//            PageRequestDto pageRequestDto = new PageRequestDto(1L, null);
//
//            //when
//            List<GameResponseDto> games = gameQueryService.getAllGames(queryRequestDto, pageRequestDto);
//
//            //then
//            assertThat(games)
//                    .map(GameResponseDto::id)
//                    .containsExactly(2L, 3L, 4L, 6L, 7L, 5L, 8L, 9L, 10L, 11L);
//
//        }
//
//        @Test
//        void 커서_이후를_조회한다() {
//            // given
//            int size = 7;
//            GamesQueryRequestDto queryRequestDto = new GamesQueryRequestDto(null, stateValue, null, null);
//
//            //when
//            List<GameResponseDto> firstPage = gameQueryService.getAllGames(
//                    queryRequestDto,
//                    new PageRequestDto(null, size)
//            );
//            Long cursor = firstPage.get(size - 1).id();
//            List<GameResponseDto> secondPage = gameQueryService.getAllGames(
//                    queryRequestDto,
//                    new PageRequestDto(cursor, size)
//            );
//
//            //then
//            assertAll(
//                    () -> assertThat(firstPage)
//                            .map(GameResponseDto::id)
//                            .containsExactly(1L, 2L, 3L, 4L, 6L, 7L, 5L),
//                    () -> assertThat(secondPage)
//                            .map(GameResponseDto::id)
//                            .containsExactly(8L, 9L, 10L, 11L, 12L, 13L, 19L)
//            );
//
//        }
//
//        @Test
//        void game_id가_start_time과_순서가_불일치해도_커서페이징이_잘_수행된다_SCHEDULED() {
//            // given
//            GamesQueryRequestDto queryRequestDto = new GamesQueryRequestDto(validLeagueId, stateValue, null, null);
//
//            //when
//            List<GameResponseDto> after15 = gameQueryService.getAllGames(
//                    queryRequestDto,
//                    new PageRequestDto(15L, null)
//            );
//            List<GameResponseDto> after19 = gameQueryService.getAllGames(
//                    queryRequestDto,
//                    new PageRequestDto(19L, null)
//            );
//            List<GameResponseDto> after18 = gameQueryService.getAllGames(
//                    queryRequestDto,
//                    new PageRequestDto(18L, null)
//            );
//            List<GameResponseDto> after16 = gameQueryService.getAllGames(
//                    queryRequestDto,
//                    new PageRequestDto(16L, null)
//            );
//
//            //then
//            assertAll(
//                    () -> assertThat(after15)
//                            .map(GameResponseDto::id)
//                            .containsExactly(19L, 18L, 16L, 17L, 24L),
//                    () -> assertThat(after19)
//                            .map(GameResponseDto::id)
//                            .containsExactly(18L, 16L, 17L, 24L),
//                    () -> assertThat(after18)
//                            .map(GameResponseDto::id)
//                            .containsExactly(16L, 17L, 24L),
//                    () -> assertThat(after16)
//                            .map(GameResponseDto::id)
//                            .containsExactly(17L, 24L)
//            );
//        }
//
//        @Test
//        void game_id가_start_time과_순서가_불일치해도_커서페이징이_잘_수행된다_FINISHED() {
//            // given
//            GamesQueryRequestDto queryRequestDto = new GamesQueryRequestDto(1L, "FINISHED", null, null);
//
//            //when
//            List<GameResponseDto> firstPage = gameQueryService.getAllGames(
//                    queryRequestDto,
//                    new PageRequestDto(null, 4)
//            );
//            List<GameResponseDto> after21 = gameQueryService.getAllGames(
//                    queryRequestDto,
//                    new PageRequestDto(22L, 3)
//            );
//
//            //then
//            assertAll(
//                    () -> assertThat(firstPage)
//                            .map(GameResponseDto::id)
//                            .containsExactly(14L, 15L, 23L, 22L),
//                    () -> assertThat(after21)
//                            .map(GameResponseDto::id)
//                            .containsExactly(20L, 21L)
//            );
//        }
//    }
//
//    @Test
//    void league_team_id로_게임을_조회한다() {
//        // given
//        GamesQueryRequestDto queryRequestDto = new GamesQueryRequestDto(1L, "SCHEDULED", leagueTeamIds, null);
//
//        //when
//        List<GameResponseDto> firstPage = gameQueryService.getAllGames(
//                queryRequestDto,
//                new PageRequestDto(null, 4)
//        );
//        List<GameResponseDto> secondPage = gameQueryService.getAllGames(
//                queryRequestDto,
//                new PageRequestDto(4L, 4)
//        );
//
//        //then
//        assertAll(
//                () -> assertThat(firstPage)
//                        .map(GameResponseDto::id)
//                        .containsExactly(1L, 2L, 3L, 4L),
//                () -> assertThat(secondPage)
//                        .map(GameResponseDto::id)
//                        .containsExactly(6L, 7L, 5L, 8L)
//        );
//    }
//
//    @Test
//    void round로_게임을_조회한다() {
//        // given
//        GamesQueryRequestDto queryRequestDto = new GamesQueryRequestDto(1L, "SCHEDULED", null, 8);
//
//        //when
//        List<GameResponseDto> firstPage = gameQueryService.getAllGames(
//                queryRequestDto,
//                new PageRequestDto(null, 4)
//        );
//        List<GameResponseDto> secondPage = gameQueryService.getAllGames(
//                queryRequestDto,
//                new PageRequestDto(19L, null)
//        );
//
//        //then
//        assertAll(
//                () -> assertThat(firstPage)
//                        .map(GameResponseDto::id)
//                        .containsExactly(10L, 11L, 12L, 13L),
//                () -> assertThat(secondPage)
//                        .map(GameResponseDto::id)
//                        .containsExactly(18L, 16L, 17L)
//        );
//    }
//
//    @Test
//    void 게임팀이_순서에_맞게_반환된다() {
//
//        // given
//        GamesQueryRequestDto queryRequestDto = new GamesQueryRequestDto(1L, "SCHEDULED", null, 8);
//
//        //when
//        List<GameResponseDto> responseDtos = gameQueryService.getAllGames(
//                queryRequestDto,
//                new PageRequestDto(null, 4)
//        );
//
//        // then
//        List<Long> gameTeamIds = responseDtos.get(0).gameTeams().stream().map(TeamResponse::gameTeamId).toList();
//        List<Long> sortedGameTeamIds = gameTeamIds.stream()
//                .sorted(Comparator.comparingLong(Long::valueOf))
//                .collect(Collectors.toList());
//        assertEquals(
//                gameTeamIds, sortedGameTeamIds
//        );
//
//    }
}
