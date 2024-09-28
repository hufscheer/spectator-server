package com.sports.server.command.report.presentation;

import static org.springframework.restdocs.cookies.CookieDocumentation.cookieWithName;
import static org.springframework.restdocs.cookies.CookieDocumentation.requestCookies;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.patch;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sports.server.command.report.dto.ReportRequest;
import com.sports.server.support.DocumentationTest;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.ResultActions;

public class ReportControllerTest extends DocumentationTest {

    @Test
    void 응원톡을_신고한다() throws Exception {

        //given
        ReportRequest request = new ReportRequest(1L);

        //when
        ResultActions result = mockMvc.perform(post("/reports", request)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        );

        //then
        result.andExpect((status().isNoContent()))
                .andDo(restDocsHandler.document(
                        requestFields(
                                fieldWithPath("cheerTalkId").type(JsonFieldType.NUMBER).description("신고하는 응원톡의 ID")
                        )
                ));
    }

    @Test
    void 신고된_응원톡을_무효처리한다() throws Exception {
        // given
        Long leagueId = 1L;
        Long cheerTalkId = 4L;

        // when
        ResultActions result = mockMvc.perform(
                patch("/reports/{leagueId}/{cheerTalkId}/cancel", leagueId, cheerTalkId)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .cookie(new Cookie(COOKIE_NAME, "temp-cookie"))
        );

        // then
        result.andExpect(status().isOk())
                .andDo(restDocsHandler.document(
                        pathParameters(
                                parameterWithName("leagueId").description("대회의 ID"),
                                parameterWithName("cheerTalkId").description("신고된 응원톡의 ID")
                        ),
                        requestCookies(
                               cookieWithName(COOKIE_NAME).description("로그인을 통해 얻은 토큰")
                        )
                ));
    }

}
