package com.sports.server.command.timeline.presentation;

import com.sports.server.command.timeline.TimelineDto;
import com.sports.server.support.DocumentationTest;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.restdocs.cookies.CookieDocumentation.cookieWithName;
import static org.springframework.restdocs.cookies.CookieDocumentation.requestCookies;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class TimelineControllerTest extends DocumentationTest {
    @Test
    void 득점_타임라인을_생성한다() throws Exception {
        // given
        TimelineDto.RegisterScore request = new TimelineDto.RegisterScore(
                1L,
                2L,
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
                                fieldWithPath("recordedQuarterId").type(JsonFieldType.NUMBER).description("쿼터 Id"),
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
        TimelineDto.RegisterReplacement request = new TimelineDto.RegisterReplacement(
                1L,
                1L,
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
                                fieldWithPath("recordedQuarterId").type(JsonFieldType.NUMBER).description("쿼터 Id"),
                                fieldWithPath("originLineupPlayerId").type(JsonFieldType.NUMBER).description("기존 선수 Id"),
                                fieldWithPath("replacementLineupPlayerId").type(JsonFieldType.NUMBER).description("교체 선수 Id"),
                                fieldWithPath("recordedAt").type(JsonFieldType.NUMBER).description("교체 시간")
                        ),
                        requestCookies(
                                cookieWithName(COOKIE_NAME).description("로그인을 통해 얻은 토큰")
                        )
                ));
    }
}
