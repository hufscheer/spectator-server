package com.sports.server.command.timeline.presentation;

import static org.springframework.restdocs.cookies.CookieDocumentation.cookieWithName;
import static org.springframework.restdocs.cookies.CookieDocumentation.requestCookies;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sports.server.command.timeline.domain.GameProgressType;
import com.sports.server.command.timeline.domain.Quarter;
import com.sports.server.command.timeline.domain.WarningCardType;
import com.sports.server.command.timeline.dto.TimelineRequest;
import com.sports.server.support.DocumentationTest;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.ResultActions;

public class TimelineControllerTest extends DocumentationTest {
    @Test
    void 득점_타임라인을_생성한다() throws Exception {
        // given
        TimelineRequest.RegisterScore request = new TimelineRequest.RegisterScore(
                1L,
                Quarter.FIRST_HALF,
                1L,
                10
        );

        // when
        ResultActions result = mockMvc.perform(post("/games/{gameId}/timelines/score", 1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .cookie(new Cookie(COOKIE_NAME, "temp-cookie"))
        );

        // then
        result.andExpect(status().isCreated())
                .andDo(restDocsHandler.document(
                        pathParameters(
                                parameterWithName("gameId").description("경기의 ID")
                        ),
                        requestFields(
                                fieldWithPath("gameTeamId").type(JsonFieldType.NUMBER).description("경기 팀의 Id"),
                                fieldWithPath("recordedQuarter").type(JsonFieldType.STRING).description("쿼터 (PRE_GAME, FIRST_HALF, SECOND_HALF, EXTRA_TIME, PENALTY_SHOOTOUT, POST_GAME)"),
                                fieldWithPath("scoreLineupPlayerId").type(JsonFieldType.NUMBER).description("득점 선수 Id"),
                                fieldWithPath("recordedAt").type(JsonFieldType.NUMBER).description("득점 시간")
                        ),
                        requestCookies(
                                cookieWithName(COOKIE_NAME).description("로그인을 통해 얻은 토큰")
                        )
                ));
    }

    @Test
    void 교체_타임라인을_생성한다() throws Exception {
        // given
        TimelineRequest.RegisterReplacement request = new TimelineRequest.RegisterReplacement(
                1L,
                Quarter.FIRST_HALF,
                2L,
                3L,
                5
        );

        // when
        ResultActions result = mockMvc.perform(post("/games/{gameId}/timelines/replacement", 1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .cookie(new Cookie(COOKIE_NAME, "temp-cookie"))
        );

        // then
        result.andExpect(status().isCreated())
                .andDo(restDocsHandler.document(
                        pathParameters(
                                parameterWithName("gameId").description("경기의 ID")
                        ),
                        requestFields(
                                fieldWithPath("gameTeamId").type(JsonFieldType.NUMBER).description("경기 팀의 Id"),
                                fieldWithPath("recordedQuarter").type(JsonFieldType.STRING).description("쿼터 (PRE_GAME, FIRST_HALF, SECOND_HALF, EXTRA_TIME, PENALTY_SHOOTOUT, POST_GAME)"),
                                fieldWithPath("originLineupPlayerId").type(JsonFieldType.NUMBER)
                                        .description("기존 선수 Id"),
                                fieldWithPath("replacementLineupPlayerId").type(JsonFieldType.NUMBER)
                                        .description("교체 선수 Id"),
                                fieldWithPath("recordedAt").type(JsonFieldType.NUMBER).description("교체 시간")
                        ),
                        requestCookies(
                                cookieWithName(COOKIE_NAME).description("로그인을 통해 얻은 토큰")
                        )
                ));
    }

    @Test
    void 게임_진행_변경_타임라인을_생성한다() throws Exception {
        // given
        TimelineRequest.RegisterProgress request = new TimelineRequest.RegisterProgress(
                10,
                Quarter.SECOND_HALF,
                GameProgressType.QUARTER_START
        );

        // when
        ResultActions result = mockMvc.perform(post("/games/{gameId}/timelines/progress", 1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .cookie(new Cookie(COOKIE_NAME, "temp-cookie"))
        );

        // then
        result.andExpect(status().isCreated())
                .andDo(restDocsHandler.document(
                        pathParameters(
                                parameterWithName("gameId").description("경기의 ID")
                        ),
                        requestFields(
                                fieldWithPath("recordedQuarter").type(JsonFieldType.STRING).description("쿼터 (PRE_GAME, FIRST_HALF, SECOND_HALF, EXTRA_TIME, PENALTY_SHOOTOUT, POST_GAME)"),
                                fieldWithPath("recordedAt").type(JsonFieldType.NUMBER).description("기록 시간"),
                                fieldWithPath("gameProgressType").type(JsonFieldType.STRING).description("변경할 게임 진행 상황 (QUARTER_START, GAME_END)")
                        ),
                        requestCookies(
                                cookieWithName(COOKIE_NAME).description("로그인을 통해 얻은 토큰")
                        )
                ));
    }

    @Test
    void 게임_승부차기_타임라인을_생성한다() throws Exception {
        // given
        TimelineRequest.RegisterPk request = new TimelineRequest.RegisterPk(
                10,
                Quarter.PENALTY_SHOOTOUT,
                1L,
                1L,
                true
        );

        // when
        ResultActions result = mockMvc.perform(post("/games/{gameId}/timelines/pk", 1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .cookie(new Cookie(COOKIE_NAME, "temp-cookie"))
        );

        // then
        result.andExpect(status().isCreated())
                .andDo(restDocsHandler.document(
                        pathParameters(
                                parameterWithName("gameId").description("경기의 ID")
                        ),
                        requestFields(
                                fieldWithPath("gameTeamId").type(JsonFieldType.NUMBER).description("경기 팀의 Id"),
                                fieldWithPath("recordedQuarter").type(JsonFieldType.STRING).description("쿼터 (PRE_GAME, FIRST_HALF, SECOND_HALF, EXTRA_TIME, PENALTY_SHOOTOUT, POST_GAME)"),
                                fieldWithPath("scorerId").type(JsonFieldType.NUMBER).description("승부차기 득점 선수 Id"),
                                fieldWithPath("recordedAt").type(JsonFieldType.NUMBER).description("득점 시간"),
                                fieldWithPath("isSuccess").type(JsonFieldType.BOOLEAN).description("승부차기 득점 성공 여부")
                        ),
                        requestCookies(
                                cookieWithName(COOKIE_NAME).description("로그인을 통해 얻은 토큰")
                        )
                ));
    }

    @Test
    void 경고_타임라인을_생성한다() throws Exception {
        // given
        TimelineRequest.RegisterWarningCard request = new TimelineRequest.RegisterWarningCard(
                10,
                Quarter.SECOND_HALF,
                1L,
                2L,
                WarningCardType.YELLOW
        );

        // when
        ResultActions result = mockMvc.perform(post("/games/{gameId}/timelines/warning-card", 1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .cookie(new Cookie(COOKIE_NAME, "temp-cookie"))
        );

        // then
        result.andExpect(status().isCreated())
                .andDo(restDocsHandler.document(
                        pathParameters(
                                parameterWithName("gameId").description("경기의 ID")
                        ),
                        requestFields(
                                fieldWithPath("gameTeamId").type(JsonFieldType.NUMBER).description("경기 팀의 Id"),
                                fieldWithPath("recordedQuarter").type(JsonFieldType.STRING).description("쿼터 (PRE_GAME, FIRST_HALF, SECOND_HALF, EXTRA_TIME, PENALTY_SHOOTOUT, POST_GAME)"),
                                fieldWithPath("warnedLineupPlayerId").type(JsonFieldType.NUMBER).description("경고 선수 Id"),
                                fieldWithPath("recordedAt").type(JsonFieldType.NUMBER).description("경고 시간"),
                                fieldWithPath("cardType").type(JsonFieldType.STRING).description("경고 카드 종류(YELLOW, RED)")
                        ),
                        requestCookies(
                                cookieWithName(COOKIE_NAME).description("로그인을 통해 얻은 토큰")
                        )
                ));
    }

    @Test
    void 타임라인을_삭제한다() throws Exception {
        // when
        ResultActions result = mockMvc.perform(
                delete("/games/{gameId}/timelines/{timelineId}", 1, 1)
                        .cookie(new Cookie(COOKIE_NAME, "temp-cookie"))
        );

        // then
        result.andExpect(status().isNoContent())
                .andDo(restDocsHandler.document(
                        pathParameters(
                                parameterWithName("gameId").description("경기의 ID"),
                                parameterWithName("timelineId").description("타임라인의 ID")
                        ),
                        requestCookies(
                                cookieWithName(COOKIE_NAME).description("로그인을 통해 얻은 토큰")
                        )
                ));
    }
}