package com.sports.server.command.game.presentation;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
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

import com.sports.server.command.game.dto.CheerCountUpdateRequest;
import com.sports.server.command.game.dto.GameRequestDto;
import com.sports.server.command.member.domain.Member;
import com.sports.server.support.DocumentationTest;
import jakarta.servlet.http.Cookie;
import java.time.LocalDateTime;
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
    void 경기를_등록한다() throws Exception {

        // given
        Long leagueId = 1L;
        Long idOfTeam1 = 1L;
        Long idOfTeam2 = 2L;
        GameRequestDto.Register requestDto = new GameRequestDto.Register("경기 이름", "16강", "경기전", "SCHEDULED",
                LocalDateTime.of(2024, 9, 11, 12, 0, 0), idOfTeam1, idOfTeam2, "videoId");

        Cookie cookie = new Cookie(COOKIE_NAME, "temp-cookie");

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
                                fieldWithPath("round").type(JsonFieldType.STRING).description("라운드의 설명 ex. 4강, 결승"),
                                fieldWithPath("quarter").type(JsonFieldType.STRING).description("쿼터"),
                                fieldWithPath("state").type(JsonFieldType.STRING).description("경기의 상태"),
                                fieldWithPath("startTime").type(JsonFieldType.STRING).description("시작 날짜 및 시각"),
                                fieldWithPath("idOfTeam1").type(JsonFieldType.NUMBER)
                                        .description("경기게 참여하는 첫번째 리그팀의 아이디"),
                                fieldWithPath("idOfTeam2").type(JsonFieldType.NUMBER)
                                        .description("경기게 참여하는 두번째 리그팀의 아이디"),
                                fieldWithPath("videoId").type(JsonFieldType.STRING)
                                        .description("경기 영상 링크")
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
    void 경기를_수정한다() throws Exception {

        // given
        Long leagueId = 1L;
        Long gameId = 1L;
        GameRequestDto.Update requestDto = new GameRequestDto.Update(
                "게임 이름", "16강", "전반전", "PLAYING", LocalDateTime.of(2024, 9, 11, 12, 0, 0), "videoId"
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
                                fieldWithPath("round").type(JsonFieldType.STRING)
                                        .description("변경할 경기의 라운드 ex. 16강, 결승"),
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
    void 경기를_삭제한다() throws Exception {
        // given
        Long leagueId = 1L;
        Long gameId = 1L;

        // when
        ResultActions result = mockMvc.perform(delete("/leagues/{leagueId}/{gameId}", leagueId, gameId)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .cookie(new Cookie(COOKIE_NAME, "temp-cookie")));

        // then
        result.andExpect((status().isOk()))
                .andDo(restDocsHandler.document(
                        pathParameters(
                                parameterWithName("leagueId").description("리그의 ID"),
                                parameterWithName("gameId").description("게임의 ID")
                        )
                ));
    }

    @Test
    void 라인업_선수를_주장으로_등록한다() throws Exception {

        //given
        Long gameId = 1L;
        Long gameTeamId = 1L;
        Long lineupPlayerId = 1L;

        //when
        ResultActions result = mockMvc.perform(
                patch("/games/{gameId}/{gameTeamId}/lineup-players/{lineupPlayerId}/captain/register", gameId,
                        gameTeamId,
                        lineupPlayerId)
                        .contentType(MediaType.APPLICATION_JSON)
        );

        //then
        result.andExpect((status().isOk()))
                .andDo(restDocsHandler.document(
                        pathParameters(
                                parameterWithName("gameId").description("게임의 ID"),
                                parameterWithName("gameTeamId").description("게임팀의 ID"),
                                parameterWithName("lineupPlayerId").description("라인업 선수의 ID")
                        )
                ));
    }

    @Test
    void 라인업_선수를_주장에서_해제한다() throws Exception {

        //given
        Long gameId = 1L;
        Long gameTeamId = 1L;
        Long lineupPlayerId = 1L;

        //when
        ResultActions result = mockMvc.perform(
                patch("/games/{gameId}/{gameTeamId}/lineup-players/{lineupPlayerId}/captain/revoke", gameId,
                        gameTeamId,
                        lineupPlayerId)
                        .contentType(MediaType.APPLICATION_JSON)
        );

        //then
        result.andExpect((status().isOk()))
                .andDo(restDocsHandler.document(
                        pathParameters(
                                parameterWithName("gameId").description("게임의 ID"),
                                parameterWithName("gameTeamId").description("게임팀의 ID"),
                                parameterWithName("lineupPlayerId").description("라인업 선수의 ID")
                        )
                ));
    }


}
