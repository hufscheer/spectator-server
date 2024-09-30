package com.sports.server.query.presentation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sports.server.command.game.domain.LineupPlayerState;
import com.sports.server.query.dto.response.GameDetailResponse;
import com.sports.server.query.dto.response.GameResponseDto;
import com.sports.server.query.dto.response.GameTeamCheerResponseDto;
import com.sports.server.query.dto.response.LineupPlayerResponse;
import com.sports.server.query.dto.response.VideoResponse;
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
                        1L, "A팀", "logo.com", 2, 0),
                new GameDetailResponse.TeamResponse(
                        2L, "B팀", "logo.com", 1, 0)
        );
        LocalDateTime startTime = LocalDateTime.of(2024, 1, 19, 13, 0, 0);
        GameDetailResponse response = new GameDetailResponse(
                startTime, "videoId", "전반전", "여름축구", "축구", gameTeams, "PLAYING", "4강", false
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
                                fieldWithPath("startTime").type(JsonFieldType.STRING).description("게임 시작 시간"),
                                fieldWithPath("videoId").type(JsonFieldType.STRING).description("게임 비디오 ID"),
                                fieldWithPath("gameQuarter").type(JsonFieldType.STRING).description("게임 쿼터"),
                                fieldWithPath("gameName").type(JsonFieldType.STRING).description("게임 이름"),
                                fieldWithPath("sportName").type(JsonFieldType.STRING).description("종목"),
                                fieldWithPath("round").type(JsonFieldType.STRING).description("게임의 라운드"),
                                fieldWithPath("gameTeams[].gameTeamId").type(JsonFieldType.NUMBER)
                                        .description("게임팀의 ID"),
                                fieldWithPath("gameTeams[].gameTeamName").type(JsonFieldType.STRING)
                                        .description("게임팀의 이름"),
                                fieldWithPath("gameTeams[].logoImageUrl").type(JsonFieldType.STRING)
                                        .description("게임팀의 이미지 URL"),
                                fieldWithPath("gameTeams[].pkScore").type(JsonFieldType.NUMBER)
                                        .description("게임팀의 승부차기 점수"),
                                fieldWithPath("gameTeams[].score").type(JsonFieldType.NUMBER).description("게임팀의 현재 점수"),
                                fieldWithPath("state").type(JsonFieldType.STRING).description("게임 상태"),
                                fieldWithPath("isPkTaken").type(JsonFieldType.BOOLEAN).description("승부차기 진출 여부")
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
                new GameResponseDto(1L, startTime, "전반전", "4강", "4강", "abc123", gameTeams1, "축구", false),
                new GameResponseDto(2L, startTime, "1쿼터", "결승전", "결승", "abc123", gameTeams2, "농구", false)
        );

        given(gameQueryService.getAllGames(any(), any()))
                .willReturn(responses);

        // when
        ResultActions result = mockMvc.perform(get("/games")
                .queryParam("league_id", "1")
                .queryParam("state", "PLAYING")
                .queryParam("sport_id", "1")
                .queryParam("sport_id", "2")
                .queryParam("description_of_round", "4강")
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
                                parameterWithName("state").description("게임의 상태"),
                                parameterWithName("sport_id").description("게임의 종목"),
                                parameterWithName("cursor").description("페이징 커서"),
                                parameterWithName("size").description("페이징 사이즈"),
                                parameterWithName("league_team_id").description("리그팀의 ID"),
                                parameterWithName("description_of_round").description("라운드의 이름 ex. 4강, 결승")
                        ),
                        responseFields(
                                fieldWithPath("[].id").type(JsonFieldType.NUMBER).description("게임의 ID"),
                                fieldWithPath("[].startTime").type(JsonFieldType.STRING).description("게임 시작 시간"),
                                fieldWithPath("[].gameQuarter").type(JsonFieldType.STRING).description("게임 쿼터"),
                                fieldWithPath("[].gameName").type(JsonFieldType.STRING).description("게임 이름"),
                                fieldWithPath("[].descriptionOfRound").type(JsonFieldType.STRING)
                                        .description("게임 라운드의 이름 ex. 4강, 결승"),
                                fieldWithPath("[].videoId").type(JsonFieldType.STRING).description("경기 영상 ID"),
                                fieldWithPath("[].sportsName").type(JsonFieldType.STRING).description("종목"),
                                fieldWithPath("[].isPkTaken").type(JsonFieldType.BOOLEAN)
                                        .description("승부차기 진출 여부"),
                                fieldWithPath("[].gameTeams[].gameTeamId").type(JsonFieldType.NUMBER)
                                        .description("게임팀의 ID"),
                                fieldWithPath("[].gameTeams[].gameTeamName").type(JsonFieldType.STRING)
                                        .description("게임팀의 이름"),
                                fieldWithPath("[].gameTeams[].logoImageUrl").type(JsonFieldType.STRING)
                                        .description("게임팀의 이미지 URL"),
                                fieldWithPath("[].gameTeams[].score").type(JsonFieldType.NUMBER)
                                        .description("게임팀의 현재 점수"),
                                fieldWithPath("[].gameTeams[].pkScore").type(JsonFieldType.NUMBER)
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
                new LineupPlayerResponse.PlayerResponse(1L, "선수A", "탑", 1, true, LineupPlayerState.STARTER),
                new LineupPlayerResponse.PlayerResponse(2L, "선수B", "미드", 2, false, LineupPlayerState.STARTER),
                new LineupPlayerResponse.PlayerResponse(3L, "선수C", "정글", 3, false, LineupPlayerState.STARTER),
                new LineupPlayerResponse.PlayerResponse(4L, "선수D", "원딜", 4, false, LineupPlayerState.STARTER),
                new LineupPlayerResponse.PlayerResponse(5L, "선수E", "서폿", 5, false, LineupPlayerState.STARTER)
        );
        List<LineupPlayerResponse.PlayerResponse> playersB = List.of(
                new LineupPlayerResponse.PlayerResponse(1L, "선수F", "탑", 1, true, LineupPlayerState.STARTER),
                new LineupPlayerResponse.PlayerResponse(2L, "선수G", "미드", 2, false, LineupPlayerState.STARTER),
                new LineupPlayerResponse.PlayerResponse(3L, "선수H", "정글", 3, false, LineupPlayerState.STARTER),
                new LineupPlayerResponse.PlayerResponse(4L, "선수I", "원딜", 4, false, LineupPlayerState.STARTER),
                new LineupPlayerResponse.PlayerResponse(5L, "선수J", "서폿", 5, false, LineupPlayerState.STARTER)
        );

        given(lineupPlayerQueryService.getLineup(gameId))
                .willReturn(List.of(
                        new LineupPlayerResponse(1L, "팀A", playersA),
                        new LineupPlayerResponse(2L, "팀B", playersB)
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
                                fieldWithPath("[].gameTeamPlayers[].id").type(JsonFieldType.NUMBER)
                                        .description("선수 ID"),
                                fieldWithPath("[].gameTeamPlayers[].playerName").type(JsonFieldType.STRING)
                                        .description("선수 이름"),
                                fieldWithPath("[].gameTeamPlayers[].description").type(JsonFieldType.STRING)
                                        .description("선수 설명"),
                                fieldWithPath("[].gameTeamPlayers[].number").type(JsonFieldType.NUMBER)
                                        .description("선수의 등번호"),
                                fieldWithPath("[].gameTeamPlayers[].isCaptain").type(JsonFieldType.BOOLEAN)
                                        .description("선수가 주장인지에 대한 정보"),
                                fieldWithPath("[].gameTeamPlayers[].state").type(JsonFieldType.STRING)
                                        .description("선수의 선발 상태 ")
                        )
                ));
    }

    @Test
    void 출전_선수를_조회한다() throws Exception {
        // given
        Long gameId = 1L;
        List<LineupPlayerResponse.PlayerResponse> playersA = List.of(
                new LineupPlayerResponse.PlayerResponse(1L, "선수A", "탑", 1, true, LineupPlayerState.STARTER),
                new LineupPlayerResponse.PlayerResponse(2L, "선수B", "미드", 2, false, LineupPlayerState.STARTER),
                new LineupPlayerResponse.PlayerResponse(3L, "선수C", "정글", 3, false, LineupPlayerState.STARTER),
                new LineupPlayerResponse.PlayerResponse(4L, "선수D", "원딜", 4, false, LineupPlayerState.STARTER),
                new LineupPlayerResponse.PlayerResponse(5L, "선수E", "서폿", 5, false, LineupPlayerState.STARTER)
        );
        List<LineupPlayerResponse.PlayerResponse> playersB = List.of(
                new LineupPlayerResponse.PlayerResponse(1L, "선수F", "탑", 1, true, LineupPlayerState.STARTER),
                new LineupPlayerResponse.PlayerResponse(2L, "선수G", "미드", 2, false, LineupPlayerState.STARTER),
                new LineupPlayerResponse.PlayerResponse(3L, "선수H", "정글", 3, false, LineupPlayerState.STARTER),
                new LineupPlayerResponse.PlayerResponse(4L, "선수I", "원딜", 4, false, LineupPlayerState.STARTER),
                new LineupPlayerResponse.PlayerResponse(5L, "선수J", "서폿", 5, false, LineupPlayerState.STARTER)
        );

        given(lineupPlayerQueryService.getPlayingLineup(gameId))
                .willReturn(List.of(
                        new LineupPlayerResponse(1L, "팀A", playersA),
                        new LineupPlayerResponse(2L, "팀B", playersB)
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
                                fieldWithPath("[].gameTeamPlayers[].description").type(JsonFieldType.STRING)
                                        .description("선수 설명"),
                                fieldWithPath("[].gameTeamPlayers[].number").type(JsonFieldType.NUMBER)
                                        .description("선수의 등번호"),
                                fieldWithPath("[].gameTeamPlayers[].isCaptain").type(JsonFieldType.BOOLEAN)
                                        .description("선수가 주장인지에 대한 정보"),
                                fieldWithPath("[].gameTeamPlayers[].state").type(JsonFieldType.STRING)
                                        .description("선수의 선발 상태 ")
                        )
                ));
    }
}
