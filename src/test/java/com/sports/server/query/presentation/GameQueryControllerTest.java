package com.sports.server.query.presentation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sports.server.command.game.domain.LineupPlayerState;
import com.sports.server.query.dto.response.*;
import com.sports.server.support.DocumentationTest;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.ResultActions;

class GameQueryControllerTest extends DocumentationTest {

    @Test
    void 게임을_상세_조회한다() throws Exception {
        // given
        Long gameId = 1L;
        List<GameDetailResponse.TeamResponse> gameTeams = List.of(
                new GameDetailResponse.TeamResponse(
                        1L, "A팀", "logo.com", 2, 0, "#00000"),
                new GameDetailResponse.TeamResponse(
                        2L, "B팀", "logo.com", 1, 0, "#00000")
        );
        LocalDateTime startTime = LocalDateTime.of(2024, 1, 19, 13, 0, 0);
        GameDetailResponse response = new GameDetailResponse(gameId,
                startTime, "videoId", "전반전", "여름축구", gameTeams, "PLAYING", 4, false, 1L, "외대 월드컵"
        );
        given(gameQueryService.getGameDetail(gameId))
                .willReturn(response);

        // when
        ResultActions result = mockMvc.perform(get("/games/{gameId}", gameId)
                .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        result.andExpect((status().isOk()))
                .andDo(restDocsHandler.document(
                        pathParameters(
                                parameterWithName("gameId").description("게임의 ID")
                        ),
                        responseFields(
                                fieldWithPath("gameId").type(JsonFieldType.NUMBER).description("게임 ID"),
                                fieldWithPath("startTime").type(JsonFieldType.STRING).description("게임 시작 시간"),
                                fieldWithPath("videoId").type(JsonFieldType.STRING).description("게임 비디오 ID"),
                                fieldWithPath("gameQuarter").type(JsonFieldType.STRING).description("게임 쿼터"),
                                fieldWithPath("gameName").type(JsonFieldType.STRING).description("게임 이름"),
                                fieldWithPath("round").type(JsonFieldType.NUMBER).description("게임의 라운드"),
                                fieldWithPath("gameTeams[].gameTeamId").type(JsonFieldType.NUMBER)
                                        .description("게임팀의 ID"),
                                fieldWithPath("gameTeams[].gameTeamName").type(JsonFieldType.STRING)
                                        .description("게임팀의 이름"),
                                fieldWithPath("gameTeams[].logoImageUrl").type(JsonFieldType.STRING)
                                        .description("게임팀의 이미지 URL"),
                                fieldWithPath("gameTeams[].pkScore").type(JsonFieldType.NUMBER)
                                        .description("게임팀의 승부차기 점수"),
                                fieldWithPath("gameTeams[].teamColor").type(JsonFieldType.STRING)
                                        .description("게임팀의 컬러"),
                                fieldWithPath("gameTeams[].score").type(JsonFieldType.NUMBER).description("게임팀의 현재 점수"),
                                fieldWithPath("state").type(JsonFieldType.STRING).description("게임 상태"),
                                fieldWithPath("isPkTaken").type(JsonFieldType.BOOLEAN).description("승부차기 진출 여부"),
                                fieldWithPath("leagueId").type(JsonFieldType.NUMBER).description("게임이 소속된 리그 id"),
                                fieldWithPath("leagueName").type(JsonFieldType.STRING).description("게임이 소속된 리그 이름")
                        )
                ));
    }

    @Test
    void 게임_영상_ID를_조회한다() throws Exception {
        // given
        Long gameId = 1L;
        given(gameQueryService.getVideo(gameId))
                .willReturn(new VideoResponse("videoId"));

        // when
        ResultActions result = mockMvc.perform(get("/games/{gameId}/video", gameId)
                .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        result.andExpect((status().isOk()))
                .andDo(restDocsHandler.document(
                        pathParameters(
                                parameterWithName("gameId").description("게임의 ID")
                        ),
                        responseFields(
                                fieldWithPath("videoId").type(JsonFieldType.STRING).description("게임 비디오 ID")
                        )
                ));
    }

    @Test
    void 게임_목록을_조회한다() throws Exception {
        // given
        LocalDateTime startTime = LocalDateTime.of(2024, 1, 19, 13, 0, 0);
        List<GameResponseDto.TeamResponse> gameTeams1 = List.of(
                new GameResponseDto.TeamResponse(1L, "A팀", "logo.com", 2, 0),
                new GameResponseDto.TeamResponse(2L, "B팀", "logo.com", 1, 0)
        );
        List<GameResponseDto.TeamResponse> gameTeams2 = List.of(
                new GameResponseDto.TeamResponse(3L, "C팀", "logo.com", 2, 0),
                new GameResponseDto.TeamResponse(4L, "D팀", "logo.com", 2, 0)
        );
        List<GameResponseDto> responses = List.of(
                new GameResponseDto(1L, startTime, "전반전", "4강", 4, "abc123", gameTeams1, false),
                new GameResponseDto(2L, startTime, "1쿼터", "결승전", 2, "abc123", gameTeams2, false)
        );
        List<LeagueWithGamesResponse> finalResponse = List.of(
                new LeagueWithGamesResponse(1L, "2025 외대월드컵", responses)
        );

        given(gameQueryService.getAllGames(any(), any())).willReturn(finalResponse);

        // when
        ResultActions result = mockMvc.perform(get("/games")
                .queryParam("league_id", "1")
                .queryParam("state", "PLAYING")
                .queryParam("round", "4")
                .queryParam("league_team_id", "1")
                .queryParam("cursor", String.valueOf(12))
                .queryParam("size", String.valueOf(10))
                .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        result.andExpect((status().isOk()))
                .andDo(restDocsHandler.document(
                        queryParameters(
                                parameterWithName("league_id").description("대회의 ID"),
                                parameterWithName("state").description("게임의 상태 (default: PLAYING)"),
                                parameterWithName("cursor").description("페이징 커서"),
                                parameterWithName("size").description("페이징 사이즈"),
                                parameterWithName("league_team_id").description("리그팀의 ID"),
                                parameterWithName("round").description("라운드의 이름 ex. 4강->4, 결승->2")
                        ),
                        responseFields(
                                fieldWithPath("[]").description("리그 목록"),
                                fieldWithPath("[].leagueId").type(JsonFieldType.NUMBER).description("리그 ID"),
                                fieldWithPath("[].leagueName").type(JsonFieldType.STRING).description("리그 이름"),
                                fieldWithPath("[].games").type(JsonFieldType.ARRAY).description("게임 목록"),
                                fieldWithPath("[].games[].id").type(JsonFieldType.NUMBER).description("게임의 ID"),
                                fieldWithPath("[].games[].startTime").type(JsonFieldType.STRING).description("게임 시작 시간"),
                                fieldWithPath("[].games[].gameQuarter").type(JsonFieldType.STRING).description("게임 쿼터"),
                                fieldWithPath("[].games[].gameName").type(JsonFieldType.STRING).description("게임 이름"),
                                fieldWithPath("[].games[].round").type(JsonFieldType.NUMBER)
                                        .description("라운드의 이름 ex. 4강->4, 결승->2"),
                                fieldWithPath("[].games[].videoId").type(JsonFieldType.STRING).description("경기 영상 ID"),
                                fieldWithPath("[].games[].isPkTaken").type(JsonFieldType.BOOLEAN)
                                        .description("승부차기 진출 여부"),
                                fieldWithPath("[].games[].gameTeams[].gameTeamId").type(JsonFieldType.NUMBER)
                                        .description("게임팀의 ID"),
                                fieldWithPath("[].games[].gameTeams[].gameTeamName").type(JsonFieldType.STRING)
                                        .description("게임팀의 이름"),
                                fieldWithPath("[].games[].gameTeams[].logoImageUrl").type(JsonFieldType.STRING)
                                        .description("게임팀의 이미지 URL"),
                                fieldWithPath("[].games[].gameTeams[].score").type(JsonFieldType.NUMBER)
                                        .description("게임팀의 현재 점수"),
                                fieldWithPath("[].games[].gameTeams[].pkScore").type(JsonFieldType.NUMBER)
                                        .description("게임팀의 승부차기 점수")
                        )
                ));
    }

    @Test
    void 응원_횟수를_조회한다() throws Exception {
        // given
        Long gameId = 1L;
        given(gameTeamQueryService.getCheerCountOfGameTeams(gameId))
                .willReturn(List.of(
                        new GameTeamCheerResponseDto(1L, 100),
                        new GameTeamCheerResponseDto(2L, 150)
                ));

        // when
        ResultActions result = mockMvc.perform(get("/games/{gameId}/cheer", gameId)
                .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        result.andExpect((status().isOk()))
                .andDo(restDocsHandler.document(
                        pathParameters(
                                parameterWithName("gameId").description("게임의 ID")
                        ),
                        responseFields(
                                fieldWithPath("[].gameTeamId").type(JsonFieldType.NUMBER).description("게임팀의 ID"),
                                fieldWithPath("[].cheerCount").type(JsonFieldType.NUMBER).description("응원 횟수")
                        )
                ));
    }

    @Test
    void 라인업을_조회한다() throws Exception {
        // given
        Long gameId = 1L;
        List<LineupPlayerResponse.PlayerResponse> playersA = List.of(
                new LineupPlayerResponse.PlayerResponse(1L, "선수A", 1, true, LineupPlayerState.STARTER, true, new LineupPlayerResponse.PlayerSummary(4L, "선수D", 4)),
                new LineupPlayerResponse.PlayerResponse(2L, "선수B", 2, false, LineupPlayerState.STARTER, false, null),
                new LineupPlayerResponse.PlayerResponse(3L, "선수C", 3, false, LineupPlayerState.STARTER, false, null),
                new LineupPlayerResponse.PlayerResponse(4L, "선수D", 4, false, LineupPlayerState.CANDIDATE, true, new LineupPlayerResponse.PlayerSummary(1L, "선수A", 1)),
                new LineupPlayerResponse.PlayerResponse(5L, "선수E", 5, false, LineupPlayerState.STARTER, false, null)
        );
        List<LineupPlayerResponse.PlayerResponse> playersB = List.of(
                new LineupPlayerResponse.PlayerResponse(1L, "선수F", 1, true, LineupPlayerState.STARTER, false, null),
                new LineupPlayerResponse.PlayerResponse(2L, "선수G", 2, false, LineupPlayerState.STARTER, false, null),
                new LineupPlayerResponse.PlayerResponse(3L, "선수H", 3, false, LineupPlayerState.STARTER, false, null),
                new LineupPlayerResponse.PlayerResponse(4L, "선수I", 4, false, LineupPlayerState.STARTER, false, null),
                new LineupPlayerResponse.PlayerResponse(5L, "선수J", 5, false, LineupPlayerState.CANDIDATE, false, null)
        );

        given(lineupPlayerQueryService.getLineup(gameId))
                .willReturn(List.of(
                        new LineupPlayerResponse.All(1L, "팀A",
                                playersA.stream().filter(playerResponse -> playerResponse.state().equals(LineupPlayerState.STARTER)).toList(),
                                playersA.stream().filter(playerResponse -> playerResponse.state().equals(LineupPlayerState.CANDIDATE)).toList()),
                        new LineupPlayerResponse.All(2L, "팀B",
                                playersB.stream().filter(playerResponse -> playerResponse.state().equals(LineupPlayerState.STARTER)).toList(),
                                playersB.stream().filter(playerResponse -> playerResponse.state().equals(LineupPlayerState.CANDIDATE)).toList())
                ));

        // when
        ResultActions result = mockMvc.perform(get("/games/{gameId}/lineup", gameId)
                .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        result.andExpect((status().isOk()))
                .andDo(restDocsHandler.document(
                        pathParameters(
                                parameterWithName("gameId").description("게임의 ID")
                        ),
                        responseFields(
                                fieldWithPath("[].gameTeamId").type(JsonFieldType.NUMBER).description("게임팀의 ID"),
                                fieldWithPath("[].teamName").type(JsonFieldType.STRING).description("게임팀 이름"),
                                fieldWithPath("[].starterPlayers[].id").type(JsonFieldType.NUMBER)
                                        .description("선발 선수 ID"),
                                fieldWithPath("[].starterPlayers[].playerName").type(JsonFieldType.STRING)
                                        .description("선발 선수 이름"),
                                fieldWithPath("[].starterPlayers[].jerseyNumber").type(JsonFieldType.NUMBER)
                                        .description("선발 선수의 등번호"),
                                fieldWithPath("[].starterPlayers[].isCaptain").type(JsonFieldType.BOOLEAN)
                                        .description("선발 선수가 주장인지에 대한 정보"),
                                fieldWithPath("[].starterPlayers[].state").type(JsonFieldType.STRING)
                                        .description("선발 선수의 선발 상태(STARTER)"),
                                fieldWithPath("[].starterPlayers[].isReplaced").type(JsonFieldType.BOOLEAN)
                                        .description("선발 선수의 교체 여부"),
                                fieldWithPath("[].starterPlayers[].replacedPlayer")
                                        .type(JsonFieldType.OBJECT)  // replacedPlayer는 객체 (또는 null)
                                        .optional()  // null일 수도 있음
                                        .description("교체되었을 시 해당 선수와 교체된 선수 정보 (없을 경우 null)"),
                                subsectionWithPath("[].starterPlayers[].replacedPlayer")
                                        .type(JsonFieldType.OBJECT)
                                        .optional()
                                        .description("교체된 선수 정보 (없으면 null). 포함 필드: id, playerName, number"),
                                fieldWithPath("[].starterPlayers[].replacedPlayer.id")
                                        .type(JsonFieldType.NUMBER)
                                        .optional()
                                        .description("교체된 선수의 ID"),
                                fieldWithPath("[].starterPlayers[].replacedPlayer.playerName")
                                        .type(JsonFieldType.STRING)
                                        .optional()
                                        .description("교체된 선수의 이름"),
                                fieldWithPath("[].starterPlayers[].replacedPlayer.number")
                                        .type(JsonFieldType.NUMBER)
                                        .optional()
                                        .description("교체된 선수의 등번호"),
                                fieldWithPath("[].candidatePlayers[].id").type(JsonFieldType.NUMBER)
                                        .description("후보 선수 ID"),
                                fieldWithPath("[].candidatePlayers[].playerName").type(JsonFieldType.STRING)
                                        .description("후보 선수 이름"),
                                fieldWithPath("[].candidatePlayers[].jerseyNumber").type(JsonFieldType.NUMBER)
                                        .description("후보 선수의 등번호"),
                                fieldWithPath("[].candidatePlayers[].isCaptain").type(JsonFieldType.BOOLEAN)
                                        .description("후보 선수가 주장인지에 대한 정보"),
                                fieldWithPath("[].candidatePlayers[].state").type(JsonFieldType.STRING)
                                        .description("후보 선수의 선발 상태(CANDIDATE)"),
                                fieldWithPath("[].candidatePlayers[].isReplaced").type(JsonFieldType.BOOLEAN)
                                        .description("후보 선수의 교체 여부"),
                                fieldWithPath("[].candidatePlayers[].replacedPlayer")
                                        .type(JsonFieldType.OBJECT)  // replacedPlayer는 객체 (또는 null)
                                        .optional()  // null일 수도 있음
                                        .description("교체되었을 시 해당 선수와 교체된 선수 정보 (없을 경우 null)"),
                                subsectionWithPath("[].candidatePlayers[].replacedPlayer")
                                        .type(JsonFieldType.OBJECT)
                                        .optional()
                                        .description("교체된 선수 정보 (없으면 null). 포함 필드: id, playerName, number"),
                                fieldWithPath("[].candidatePlayers[].replacedPlayer.id")
                                        .type(JsonFieldType.NUMBER)
                                        .optional()
                                        .description("교체된 선수의 ID"),
                                fieldWithPath("[].candidatePlayers[].replacedPlayer.playerName")
                                        .type(JsonFieldType.STRING)
                                        .optional()
                                        .description("교체된 선수의 이름"),
                                fieldWithPath("[].candidatePlayers[].replacedPlayer.number")
                                        .type(JsonFieldType.NUMBER)
                                        .optional()
                                        .description("교체된 선수의 등번호")
                        )
                ));
    }

    @Test
    void 출전_선수를_조회한다() throws Exception {
        // given
        Long gameId = 1L;
        List<LineupPlayerResponse.PlayerResponse> playersA = List.of(
                new LineupPlayerResponse.PlayerResponse(1L, "선수A", 1, true, LineupPlayerState.STARTER, false, null),
                new LineupPlayerResponse.PlayerResponse(2L, "선수B", 2, false, LineupPlayerState.STARTER, false, null),
                new LineupPlayerResponse.PlayerResponse(3L, "선수C", 3, false, LineupPlayerState.STARTER, false, null),
                new LineupPlayerResponse.PlayerResponse(4L, "선수D", 4, false, LineupPlayerState.STARTER, false, null),
                new LineupPlayerResponse.PlayerResponse(5L, "선수E", 5, false, LineupPlayerState.STARTER, false, null)
        );
        List<LineupPlayerResponse.PlayerResponse> playersB = List.of(
                new LineupPlayerResponse.PlayerResponse(1L, "선수F", 1, true, LineupPlayerState.STARTER, false, null),
                new LineupPlayerResponse.PlayerResponse(2L, "선수G", 2, false, LineupPlayerState.STARTER, false, null),
                new LineupPlayerResponse.PlayerResponse(3L, "선수H", 3, false, LineupPlayerState.STARTER, false, null),
                new LineupPlayerResponse.PlayerResponse(4L, "선수I", 4, false, LineupPlayerState.STARTER, false, null),
                new LineupPlayerResponse.PlayerResponse(5L, "선수J", 5, false, LineupPlayerState.STARTER, false, null)
        );

        given(lineupPlayerQueryService.getPlayingLineup(gameId))
                .willReturn(List.of(
                        new LineupPlayerResponse.Playing(1L, "팀A", playersA),
                        new LineupPlayerResponse.Playing(2L, "팀B", playersB)
                ));

        // when
        ResultActions result = mockMvc.perform(get("/games/{gameId}/lineup/playing", gameId)
                .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        result.andExpect((status().isOk()))
                .andDo(restDocsHandler.document(
                        pathParameters(
                                parameterWithName("gameId").description("게임의 ID")
                        ),
                        responseFields(
                                fieldWithPath("[].gameTeamId").type(JsonFieldType.NUMBER).description("게임팀의 ID"),
                                fieldWithPath("[].teamName").type(JsonFieldType.STRING).description("게임팀 이름"),
                                fieldWithPath("[].gameTeamPlayers[].id").type(JsonFieldType.NUMBER)
                                        .description("선수 ID"),
                                fieldWithPath("[].gameTeamPlayers[].playerName").type(JsonFieldType.STRING)
                                        .description("선수 이름"),
                                fieldWithPath("[].gameTeamPlayers[].jerseyNumber").type(JsonFieldType.NUMBER)
                                        .description("선수의 등번호"),
                                fieldWithPath("[].gameTeamPlayers[].isCaptain").type(JsonFieldType.BOOLEAN)
                                        .description("선수가 주장인지에 대한 정보"),
                                fieldWithPath("[].gameTeamPlayers[].state").type(JsonFieldType.STRING)
                                        .description("선수의 선발 상태"),
                                fieldWithPath("[].gameTeamPlayers[].isReplaced").type(JsonFieldType.BOOLEAN)
                                        .description("선수의 교체 여부"),
                                fieldWithPath("[].gameTeamPlayers[].replacedPlayer")
                                        .type(JsonFieldType.OBJECT)  // replacedPlayer는 객체 (또는 null)
                                        .optional()  // null일 수도 있음
                                        .description("교체되었을 시 해당 선수와 교체된 선수 정보 (없을 경우 null)"),
                                subsectionWithPath("[].gameTeamPlayers[].replacedPlayer")
                                        .type(JsonFieldType.OBJECT)
                                        .optional()
                                        .description("교체된 선수 정보 (없으면 null). 포함 필드: id, playerName, number"),
                                fieldWithPath("[].gameTeamPlayers[].replacedPlayer.id")
                                        .type(JsonFieldType.NUMBER)
                                        .optional()
                                        .description("교체된 선수의 ID"),
                                fieldWithPath("[].gameTeamPlayers[].replacedPlayer.playerName")
                                        .type(JsonFieldType.STRING)
                                        .optional()
                                        .description("교체된 선수의 이름"),
                                fieldWithPath("[].gameTeamPlayers[].replacedPlayer.number")
                                        .type(JsonFieldType.NUMBER)
                                        .optional()
                                        .description("교체된 선수의 등번호")
                        )
                ));
    }

    @Test
    void 연도와_월로_게임을_검색한다() throws Exception {
        // given
        int year = 2024;
        int month = 3;
        
        LocalDateTime startTime1 = LocalDateTime.of(2024, 3, 15, 14, 0, 0);
        LocalDateTime startTime2 = LocalDateTime.of(2024, 3, 20, 16, 0, 0);
        
        List<GameDetailResponse.TeamResponse> gameTeams1 = List.of(
                new GameDetailResponse.TeamResponse(1L, "A팀", "logo1.com", 2, 0, "#00000"),
                new GameDetailResponse.TeamResponse(2L, "B팀", "logo2.com", 1, 0, "#00000")
        );
        List<GameDetailResponse.TeamResponse> gameTeams2 = List.of(
                new GameDetailResponse.TeamResponse(3L, "C팀", "logo3.com", 0, 0, "#00000"),
                new GameDetailResponse.TeamResponse(4L, "D팀", "logo4.com", 1, 0, "#00000")
        );
        
        List<GameDetailResponse> responses = List.of(
                new GameDetailResponse(1L, startTime1, "video1", "전반전", "4강", gameTeams1, "FINISHED", 4, false, 1L, "춘계리그"),
                new GameDetailResponse(2L, startTime2, "video2", "후반전", "결승", gameTeams2, "PLAYING", 2, false, 1L, "춘계리그")
        );

        given(gameQueryService.getGamesByYearAndMonth(year, month))
                .willReturn(responses);

        // when
        ResultActions result = mockMvc.perform(get("/games/search")
                .queryParam("year", String.valueOf(year))
                .queryParam("month", String.valueOf(month))
                .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        result.andExpect((status().isOk()))
                .andDo(restDocsHandler.document(
                        queryParameters(
                                parameterWithName("year").description("게임이 진행되는 연도"),
                                parameterWithName("month").description("게임이 진행되는 월")
                        ),
                        responseFields(
                                fieldWithPath("[].gameId").type(JsonFieldType.NUMBER).description("게임 ID"),
                                fieldWithPath("[].startTime").type(JsonFieldType.STRING).description("게임 시작 시간"),
                                fieldWithPath("[].videoId").type(JsonFieldType.STRING).description("게임 비디오 ID"),
                                fieldWithPath("[].gameQuarter").type(JsonFieldType.STRING).description("게임 쿼터"),
                                fieldWithPath("[].gameName").type(JsonFieldType.STRING).description("게임 이름"),
                                fieldWithPath("[].round").type(JsonFieldType.NUMBER).description("게임의 라운드"),
                                fieldWithPath("[].gameTeams[].gameTeamId").type(JsonFieldType.NUMBER)
                                        .description("게임팀의 ID"),
                                fieldWithPath("[].gameTeams[].gameTeamName").type(JsonFieldType.STRING)
                                        .description("게임팀의 이름"),
                                fieldWithPath("[].gameTeams[].logoImageUrl").type(JsonFieldType.STRING)
                                        .description("게임팀의 이미지 URL"),
                                fieldWithPath("[].gameTeams[].pkScore").type(JsonFieldType.NUMBER)
                                        .description("게임팀의 승부차기 점수"),
                                fieldWithPath("[].gameTeams[].teamColor").type(JsonFieldType.STRING)
                                        .description("게임팀의 팀컬러"),
                                fieldWithPath("[].gameTeams[].score").type(JsonFieldType.NUMBER).description("게임팀의 현재 점수"),
                                fieldWithPath("[].state").type(JsonFieldType.STRING).description("게임 상태"),
                                fieldWithPath("[].isPkTaken").type(JsonFieldType.BOOLEAN).description("승부차기 진출 여부"),
                                fieldWithPath("[].leagueId").type(JsonFieldType.NUMBER).description("게임이 소속된 리그 id"),
                                fieldWithPath("[].leagueName").type(JsonFieldType.STRING).description("게임이 소속된 리그 이름")
                        )
                ));
    }
}
