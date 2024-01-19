package com.sports.server.query.presentation;

import com.sports.server.query.dto.response.*;
import com.sports.server.support.DocumentationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GameQueryControllerTest extends DocumentationTest {

    @Test
    void 게임을_상세_조회한다() throws Exception {
        // given
        Long gameId = 1L;
        List<GameDetailResponse.TeamResponse> gameTeams = List.of(
                new GameDetailResponse.TeamResponse(
                        1L, "A팀", "logo.com", 2, 1),
                new GameDetailResponse.TeamResponse(
                        2L, "B팀", "logo.com", 1, 2)
        );
        LocalDateTime startTime = LocalDateTime.of(2024, 1, 19, 13, 0, 0);
        GameDetailResponse response = new GameDetailResponse(
                startTime, "videoId", "전반전", "4강", "축구", gameTeams
        );
        given(gameQueryService.getGameDetail(gameId))
                .willReturn(response);

        // when
        ResultActions result = mockMvc.perform(get("/games/{gameId}", gameId)
                .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        result.andExpect((status().isOk()))
                .andDo(RESULT_HANDLER.document(
                        pathParameters(
                                parameterWithName("gameId").description("게임의 ID")
                        ),
                        responseFields(
                                fieldWithPath("startTime").type(JsonFieldType.STRING).description("게임 시작 시간"),
                                fieldWithPath("videoId").type(JsonFieldType.STRING).description("게임 비디오 ID"),
                                fieldWithPath("gameQuarter").type(JsonFieldType.STRING).description("게임 쿼터"),
                                fieldWithPath("gameName").type(JsonFieldType.STRING).description("게임 이름"),
                                fieldWithPath("sportName").type(JsonFieldType.STRING).description("종목"),
                                fieldWithPath("sportName").type(JsonFieldType.STRING).description("종목"),
                                fieldWithPath("gameTeams[].gameTeamId").type(JsonFieldType.NUMBER).description("게임팀의 ID"),
                                fieldWithPath("gameTeams[].gameTeamName").type(JsonFieldType.STRING).description("게임팀의 이름"),
                                fieldWithPath("gameTeams[].logoImageUrl").type(JsonFieldType.STRING).description("게임팀의 이미지 URL"),
                                fieldWithPath("gameTeams[].score").type(JsonFieldType.NUMBER).description("게임팀의 현재 점수"),
                                fieldWithPath("gameTeams[].order").type(JsonFieldType.NUMBER).description("게임팀의 순서")
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
                .andDo(RESULT_HANDLER.document(
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
                new GameResponseDto.TeamResponse(1L, "A팀", "logo.com", 2, 1),
                new GameResponseDto.TeamResponse(2L, "B팀", "logo.com", 1, 2)
        );
        List<GameResponseDto.TeamResponse> gameTeams2 = List.of(
                new GameResponseDto.TeamResponse(3L, "C팀", "logo.com", 2, 1),
                new GameResponseDto.TeamResponse(4L, "D팀", "logo.com", 2, 2)
        );
        List<GameResponseDto> responses = List.of(
                new GameResponseDto(1L, startTime, "전반전", "4강", gameTeams1, "축구"),
                new GameResponseDto(2L, startTime, "1쿼터", "결승전", gameTeams2, "농구")
        );

        given(gameQueryService.getAllGames(any(), any()))
                .willReturn(responses);

        // when
        ResultActions result = mockMvc.perform(get("/games")
                .queryParam("league_id", "1")
                .queryParam("status", "PLAYING")
                .queryParam("sport_id", "1")
                .queryParam("sport_id", "2")
                .queryParam("cursor", String.valueOf(12))
                .queryParam("size", String.valueOf(10))
                .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        result.andExpect((status().isOk()))
                .andDo(RESULT_HANDLER.document(
                        queryParameters(
                                parameterWithName("league_id").description("대회의 ID"),
                                parameterWithName("status").description("게임의 상태"),
                                parameterWithName("sport_id").description("게임의 종목"),
                                parameterWithName("cursor").description("페이징 커서"),
                                parameterWithName("size").description("페이징 사이즈")
                        ),
                        responseFields(
                                fieldWithPath("[].id").type(JsonFieldType.NUMBER).description("게임의 ID"),
                                fieldWithPath("[].startTime").type(JsonFieldType.STRING).description("게임 시작 시간"),
                                fieldWithPath("[].gameQuarter").type(JsonFieldType.STRING).description("게임 쿼터"),
                                fieldWithPath("[].gameName").type(JsonFieldType.STRING).description("게임 이름"),
                                fieldWithPath("[].sportsName").type(JsonFieldType.STRING).description("종목"),
                                fieldWithPath("[].gameTeams[].gameTeamId").type(JsonFieldType.NUMBER).description("게임팀의 ID"),
                                fieldWithPath("[].gameTeams[].gameTeamName").type(JsonFieldType.STRING).description("게임팀의 이름"),
                                fieldWithPath("[].gameTeams[].logoImageUrl").type(JsonFieldType.STRING).description("게임팀의 이미지 URL"),
                                fieldWithPath("[].gameTeams[].score").type(JsonFieldType.NUMBER).description("게임팀의 현재 점수"),
                                fieldWithPath("[].gameTeams[].order").type(JsonFieldType.NUMBER).description("게임팀의 순서")
                        )
                ));
    }

    @Test
    void 응원_횟수를_조회한다() throws Exception {
        // given
        Long gameId = 1L;
        given(gameTeamQueryService.getCheerCountOfGameTeams(gameId))
                .willReturn(List.of(
                        new GameTeamCheerResponseDto(1L, 100, 1),
                        new GameTeamCheerResponseDto(2L, 150, 2)
                ));

        // when
        ResultActions result = mockMvc.perform(get("/games/{gameId}/cheer", gameId)
                .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        result.andExpect((status().isOk()))
                .andDo(RESULT_HANDLER.document(
                        pathParameters(
                                parameterWithName("gameId").description("게임의 ID")
                        ),
                        responseFields(
                                fieldWithPath("[].gameTeamId").type(JsonFieldType.NUMBER).description("게임팀의 ID"),
                                fieldWithPath("[].cheerCount").type(JsonFieldType.NUMBER).description("응원 횟수"),
                                fieldWithPath("[].order").type(JsonFieldType.NUMBER).description("게임팀의 순서")
                        )
                ));
    }

    @Test
    void 라인업을_조회한다() throws Exception {
        // given
        Long gameId = 1L;
        List<LineupPlayerResponse.PlayerResponse> playersA = List.of(
                new LineupPlayerResponse.PlayerResponse("선수A", "탑"),
                new LineupPlayerResponse.PlayerResponse("선수B", "미드"),
                new LineupPlayerResponse.PlayerResponse("선수C", "정글"),
                new LineupPlayerResponse.PlayerResponse("선수D", "원딜"),
                new LineupPlayerResponse.PlayerResponse("선수E", "서폿")
        );
        List<LineupPlayerResponse.PlayerResponse> playersB = List.of(
                new LineupPlayerResponse.PlayerResponse("선수F", "탑"),
                new LineupPlayerResponse.PlayerResponse("선수G", "미드"),
                new LineupPlayerResponse.PlayerResponse("선수H", "정글"),
                new LineupPlayerResponse.PlayerResponse("선수I", "원딜"),
                new LineupPlayerResponse.PlayerResponse("선수J", "서폿")
        );

        given(lineupPlayerQueryService.getLineup(gameId))
                .willReturn(List.of(
                        new LineupPlayerResponse(1L, "팀A", playersA, 1),
                        new LineupPlayerResponse(2L, "팀B", playersB, 2)
                ));

        // when
        ResultActions result = mockMvc.perform(get("/games/{gameId}/lineup", gameId)
                .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        result.andExpect((status().isOk()))
                .andDo(RESULT_HANDLER.document(
                        pathParameters(
                                parameterWithName("gameId").description("게임의 ID")
                        ),
                        responseFields(
                                fieldWithPath("[].gameTeamId").type(JsonFieldType.NUMBER).description("게임팀의 ID"),
                                fieldWithPath("[].teamName").type(JsonFieldType.STRING).description("게임팀 이름"),
                                fieldWithPath("[].order").type(JsonFieldType.NUMBER).description("게임팀의 순서"),
                                fieldWithPath("[].gameTeamPlayers[].playerName").type(JsonFieldType.STRING).description("선수 이름"),
                                fieldWithPath("[].gameTeamPlayers[]업.description").type(JsonFieldType.STRING).description("선수 설명")
                        )
                ));
    }
}
