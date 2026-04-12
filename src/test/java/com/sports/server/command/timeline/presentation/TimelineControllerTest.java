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
import com.sports.server.command.league.domain.BasketballQuarter;
import com.sports.server.command.league.domain.SportType;
import com.sports.server.command.league.domain.SoccerQuarter;
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
        TimelineRequest.RegisterSoccerScore request = new TimelineRequest.RegisterSoccerScore(
                1L, SportType.SOCCER, SoccerQuarter.FIRST_HALF.name(),
                1L,
                10,
                null
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
                                fieldWithPath("sportType").type(JsonFieldType.STRING).description("스포츠 종류 (SOCCER, BASKETBALL)"),
                                fieldWithPath("gameTeamId").type(JsonFieldType.NUMBER).description("경기 팀의 Id"),
                                fieldWithPath("recordedQuarter").type(JsonFieldType.STRING).description("쿼터 (PRE_GAME, FIRST_HALF, SECOND_HALF, EXTRA_TIME, PENALTY_SHOOTOUT, POST_GAME)"),
                                fieldWithPath("scoreLineupPlayerId").type(JsonFieldType.NUMBER).description("득점 선수 Id"),
                                fieldWithPath("recordedAt").type(JsonFieldType.NUMBER).description("득점 시간"),
                                fieldWithPath("assistLineupPlayerId").type(JsonFieldType.NULL).description("어시스트 선수 Id (없으면 null)").optional()
                        ),
                        requestCookies(
                                cookieWithName(COOKIE_NAME).description("로그인을 통해 얻은 토큰")
                        )
                ));
    }

    @Test
    void 농구_득점_타임라인을_생성한다() throws Exception {
        // given
        TimelineRequest.RegisterBasketballScore request = new TimelineRequest.RegisterBasketballScore(
                1L, SportType.BASKETBALL, BasketballQuarter.FIRST_QUARTER.name(),
                1L, 10, null, 3
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
                                fieldWithPath("sportType").type(JsonFieldType.STRING).description("스포츠 종류 (BASKETBALL)"),
                                fieldWithPath("gameTeamId").type(JsonFieldType.NUMBER).description("경기 팀의 Id"),
                                fieldWithPath("recordedQuarter").type(JsonFieldType.STRING).description("쿼터 (FIRST_QUARTER, SECOND_QUARTER, THIRD_QUARTER, FOURTH_QUARTER, OVERTIME)"),
                                fieldWithPath("scoreLineupPlayerId").type(JsonFieldType.NUMBER).description("득점 선수 Id"),
                                fieldWithPath("recordedAt").type(JsonFieldType.NUMBER).description("득점 시간"),
                                fieldWithPath("assistLineupPlayerId").type(JsonFieldType.NULL).description("어시스트 선수 Id (없으면 null)").optional(),
                                fieldWithPath("score").type(JsonFieldType.NUMBER).description("득점한 점수 (1, 2, 3 중 하나)")
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
                1L, SportType.SOCCER, SoccerQuarter.FIRST_HALF.name(),
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
                                fieldWithPath("sportType").type(JsonFieldType.STRING).description("스포츠 종류 (SOCCER, BASKETBALL)"),
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
                10, SportType.SOCCER, SoccerQuarter.SECOND_HALF.name(),
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
                                fieldWithPath("sportType").type(JsonFieldType.STRING).description("스포츠 종류 (SOCCER, BASKETBALL)"),
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
                10, SportType.SOCCER, SoccerQuarter.PENALTY_SHOOTOUT.name(),
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
                                fieldWithPath("sportType").type(JsonFieldType.STRING).description("스포츠 종류 (SOCCER, BASKETBALL)"),
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
                10, SportType.SOCCER, SoccerQuarter.SECOND_HALF.name(),
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
                                fieldWithPath("sportType").type(JsonFieldType.STRING).description("스포츠 종류 (SOCCER, BASKETBALL)"),
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