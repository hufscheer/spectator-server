package com.sports.server.command.game.presentation;

import static org.springframework.restdocs.cookies.CookieDocumentation.cookieWithName;
import static org.springframework.restdocs.cookies.CookieDocumentation.requestCookies;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.responseHeaders;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sports.server.command.game.domain.LineupPlayerState;
import com.sports.server.command.game.dto.CheerCountUpdateRequest;
import com.sports.server.command.game.dto.GameRequest;
import com.sports.server.support.DocumentationTest;
import jakarta.servlet.http.Cookie;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.ResultActions;

public class GameControllerTest extends DocumentationTest {

    @Test
    void 응원_횟수를_업데이트한다() throws Exception {

        //given
        Long gameId = 1L;
        CheerCountUpdateRequest request = new CheerCountUpdateRequest(1L, 1);

        //when
        ResultActions result = mockMvc.perform(post("/games/{gameId}/cheer", gameId, request)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        );

        //then
        result.andExpect((status().isOk()))
                .andDo(restDocsHandler.document(
                        pathParameters(
                                parameterWithName("gameId").description("게임의 ID")
                        ),
                        requestFields(
                                fieldWithPath("gameTeamId").type(JsonFieldType.NUMBER).description("게임팀의 id"),
                                fieldWithPath("cheerCount").type(JsonFieldType.NUMBER).description("증가시킬 응원 횟수")

                        )
                ));
    }

    @Test
    void 라인업_선수의_상태를_선발로_변경한다() throws Exception {

        //given
        Long gameId = 1L;
        Long lineupPlayerId = 1L;

        //when
        ResultActions result = mockMvc.perform(
                patch("/games/{gameId}/lineup-players/{lineupPlayerId}/starter", gameId, lineupPlayerId)
                        .contentType(MediaType.APPLICATION_JSON)
        );

        //then
        result.andExpect((status().isOk()))
                .andDo(restDocsHandler.document(
                        pathParameters(
                                parameterWithName("gameId").description("게임의 ID"),
                                parameterWithName("lineupPlayerId").description("라인업 선수의 ID")
                        )
                ));
    }

    @Test
    void 라인업_선수의_상태를_후보로_변경한다() throws Exception {

        //given
        Long gameId = 1L;
        Long lineupPlayerId = 1L;

        //when
        ResultActions result = mockMvc.perform(
                patch("/games/{gameId}/lineup-players/{lineupPlayerId}/candidate", gameId, lineupPlayerId)
                        .contentType(MediaType.APPLICATION_JSON)
        );

        //then
        result.andExpect((status().isOk()))
                .andDo(restDocsHandler.document(
                        pathParameters(
                                parameterWithName("gameId").description("게임의 ID"),
                                parameterWithName("lineupPlayerId").description("라인업 선수의 ID")
                        )
                ));
    }

    @Test
    void 게임을_등록한다() throws Exception {
        // given
        Long leagueId = 1L;
        Cookie cookie = new Cookie(COOKIE_NAME, "temp-cookie");

        List<GameRequest.LineupPlayerRequest> team1LineupPlayers = List.of(
                new GameRequest.LineupPlayerRequest(1L, LineupPlayerState.STARTER, true),
                new GameRequest.LineupPlayerRequest(2L, LineupPlayerState.STARTER, false)
        );
        GameRequest.TeamLineupRequest team1 = new GameRequest.TeamLineupRequest(1L, team1LineupPlayers);

        List<GameRequest.LineupPlayerRequest> team2LineupPlayers = List.of(
                new GameRequest.LineupPlayerRequest(3L, LineupPlayerState.STARTER, true),
                new GameRequest.LineupPlayerRequest(4L, LineupPlayerState.CANDIDATE, false)
        );
        GameRequest.TeamLineupRequest team2 = new GameRequest.TeamLineupRequest(2L, team2LineupPlayers);

        // 최종 요청 DTO 생성
        GameRequest.Register requestDto = new GameRequest.Register(
                "결승전",
                2,
                "결승",
                "SCHEDULED",
                LocalDateTime.of(2025, 11, 11, 19, 0, 0),
                "youtube video url",
                team1,
                team2
        );


        // when
        ResultActions result = mockMvc.perform(post("/leagues/{leagueId}/games", leagueId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto))
                .cookie(cookie)
        );

        // then
        result.andExpect(status().isCreated())
                .andDo(restDocsHandler.document(
                        pathParameters(
                                parameterWithName("leagueId").description("리그의 ID")
                        ),
                        requestFields(
                                fieldWithPath("name").type(JsonFieldType.STRING).description("경기의 이름"),
                                fieldWithPath("round").type(JsonFieldType.NUMBER).description("라운드 (16강, 8강, 4강, 결승)"),
                                fieldWithPath("quarter").type(JsonFieldType.STRING).description("쿼터 정보 (사용자 지정 문자열)"),
                                fieldWithPath("state").type(JsonFieldType.STRING).description("경기의 상태 (SCHEDULED, PLAYING, FINISHED)"),
                                fieldWithPath("startTime").type(JsonFieldType.STRING).description("경기 시작 날짜 및 시각"),
                                fieldWithPath("videoId").type(JsonFieldType.STRING).description("경기 영상 링크 (nullable)").optional(),

                                fieldWithPath("team1").type(JsonFieldType.OBJECT).description("첫 번째 팀의 라인업 정보"),
                                fieldWithPath("team1.teamId").type(JsonFieldType.NUMBER).description("첫 번째 팀의 ID"),
                                fieldWithPath("team1.lineupPlayers").type(JsonFieldType.ARRAY).description("첫 번째 팀의 라인업 선수 목록(없다면 빈 리스트)"),
                                fieldWithPath("team1.lineupPlayers[].teamPlayerId").type(JsonFieldType.NUMBER).description("라인업 선수의 ID"),
                                fieldWithPath("team1.lineupPlayers[].state").type(JsonFieldType.STRING).description("선수 상태 (STARTER, CANDIDATE)"),
                                fieldWithPath("team1.lineupPlayers[].isCaptain").type(JsonFieldType.BOOLEAN).description("주장 여부"),

                                fieldWithPath("team2").type(JsonFieldType.OBJECT).description("두 번째 팀의 라인업 정보"),
                                fieldWithPath("team2.teamId").type(JsonFieldType.NUMBER).description("두 번째 팀의 ID"),
                                fieldWithPath("team2.lineupPlayers").type(JsonFieldType.ARRAY).description("두 번째 팀의 라인업 선수 목록(없다면 빈 리스트)"),
                                fieldWithPath("team2.lineupPlayers[].teamPlayerId").type(JsonFieldType.NUMBER).description("라인업 선수의 ID"),
                                fieldWithPath("team2.lineupPlayers[].state").type(JsonFieldType.STRING).description("선수 상태 (STARTER, CANDIDATE)"),
                                fieldWithPath("team2.lineupPlayers[].isCaptain").type(JsonFieldType.BOOLEAN).description("주장 여부")
                        ),
                        requestCookies(
                                cookieWithName(COOKIE_NAME).description("로그인을 통해 얻은 토큰")
                        ),
                        responseHeaders(
                                headerWithName("Location").description("새로 생성된 리소스의 URL")
                        )
                ));
    }

    @Test
    void 게임을_수정한다() throws Exception {

        // given
        Long leagueId = 1L;
        Long gameId = 1L;
        GameRequest.Update requestDto = new GameRequest.Update(
                "게임 이름", 16, "전반전", "PLAYING", LocalDateTime.of(2024, 9, 11, 12, 0, 0), "videoId"
        );

        // when
        ResultActions result = mockMvc.perform(put("/leagues/{leagueId}/{gameId}", leagueId, gameId)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(requestDto))
                .cookie(new Cookie(COOKIE_NAME, "temp-cookie")));

        // then
        result.andExpect(status().isOk())
                .andDo(restDocsHandler.document(
                        pathParameters(
                                parameterWithName("leagueId").description("리그의 ID"),
                                parameterWithName("gameId").description("경기의 ID")
                        ),
                        requestFields(
                                fieldWithPath("name").type(JsonFieldType.STRING).description("변경할 경기의 이름"),
                                fieldWithPath("round").type(JsonFieldType.NUMBER)
                                        .description("변경할 경기의 라운드 ex. 16강->16, 결승->2"),
                                fieldWithPath("quarter").type(JsonFieldType.STRING).description("쿼터"),
                                fieldWithPath("state").type(JsonFieldType.STRING).description("경기의 상태"),
                                fieldWithPath("startTime").type(JsonFieldType.STRING).description("시작 날짜 및 시각"),
                                fieldWithPath("videoId").type(JsonFieldType.STRING)
                                        .description("경기 영상 링크")
                        ),
                        requestCookies(
                                cookieWithName(COOKIE_NAME).description("로그인을 통해 얻은 토큰")
                        )
                ));
    }

    @Test
    void 게임을_삭제한다() throws Exception {
        // given
        Long leagueId = 1L;
        Long gameId = 1L;

        // when
        ResultActions result = mockMvc.perform(delete("/leagues/{leagueId}/{gameId}", leagueId, gameId)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .cookie(new Cookie(COOKIE_NAME, "temp-cookie")));

        // then
        result.andExpect((status().isNoContent()))
                .andDo(restDocsHandler.document(
                        pathParameters(
                                parameterWithName("leagueId").description("리그의 ID"),
                                parameterWithName("gameId").description("게임의 ID")
                        )
                ));
    }

    @Test
    void 게임팀_라인업에_선수를_추가한다() throws Exception {
        // given
        Long gameTeamId = 1L;
        GameRequest.LineupPlayerRequest request = new GameRequest.LineupPlayerRequest(
                5L, LineupPlayerState.CANDIDATE, false
        );

        // when
        ResultActions result = mockMvc.perform(post("/game-teams/{gameTeamId}/lineup-players", gameTeamId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .cookie(new Cookie(COOKIE_NAME, "temp-cookie")));

        // then
        result.andExpect(status().isOk())
                .andDo(restDocsHandler.document(
                        pathParameters(
                                parameterWithName("gameTeamId").description("라인업 선수를 추가할 게임팀의 ID")
                        ),
                        requestFields(
                                fieldWithPath("teamPlayerId").type(JsonFieldType.NUMBER).description("추가할 선수의 팀플레이어 ID"),
                                fieldWithPath("state").type(JsonFieldType.STRING).description("선수 상태 (STARTER, CANDIDATE)"),
                                fieldWithPath("isCaptain").type(JsonFieldType.BOOLEAN).description("주장 여부")
                        ),
                        requestCookies(
                                cookieWithName(COOKIE_NAME).description("로그인을 통해 얻은 토큰")
                        )
                ));
    }

    @Test
    void 게임팀_라인업에서_선수를_삭제한다() throws Exception {
        // given
        Long gameTeamId = 1L;
        Long lineupPlayerId = 10L;

        // when
        ResultActions result = mockMvc.perform(delete("/game-teams/{gameTeamId}/lineup-players/{lineupPlayerId}", gameTeamId, lineupPlayerId)
                .cookie(new Cookie(COOKIE_NAME, "temp-cookie")));

        // then
        result.andExpect(status().isNoContent())
                .andDo(restDocsHandler.document(
                        pathParameters(
                                parameterWithName("gameTeamId").description("게임팀의 ID"),
                                parameterWithName("lineupPlayerId").description("게임팀에서 삭제할 라인업 선수의 ID")
                        ),
                        requestCookies(
                                cookieWithName(COOKIE_NAME).description("로그인을 통해 얻은 토큰")
                        )
                ));
    }

    @Test
    void 라인업_선수를_주장으로_등록한다() throws Exception {

        //given
        Long gameId = 1L;
        Long lineupPlayerId = 1L;

        //when
        ResultActions result = mockMvc.perform(
                patch("/games/{gameId}/lineup-players/{lineupPlayerId}/captain/register", gameId,
                        lineupPlayerId)
                        .contentType(MediaType.APPLICATION_JSON)
        );

        //then
        result.andExpect((status().isOk()))
                .andDo(restDocsHandler.document(
                        pathParameters(
                                parameterWithName("gameId").description("게임의 ID"),
                                parameterWithName("lineupPlayerId").description("라인업 선수의 ID")
                        )
                ));
    }

    @Test
    void 라인업_선수를_주장에서_해제한다() throws Exception {

        //given
        Long gameId = 1L;
        Long lineupPlayerId = 1L;

        //when
        ResultActions result = mockMvc.perform(
                patch("/games/{gameId}/lineup-players/{lineupPlayerId}/captain/revoke", gameId,
                        lineupPlayerId)
                        .contentType(MediaType.APPLICATION_JSON)
        );

        //then
        result.andExpect((status().isOk()))
                .andDo(restDocsHandler.document(
                        pathParameters(
                                parameterWithName("gameId").description("게임의 ID"),
                                parameterWithName("lineupPlayerId").description("라인업 선수의 ID")
                        )
                ));
    }
}
