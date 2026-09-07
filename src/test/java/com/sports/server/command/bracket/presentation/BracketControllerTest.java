package com.sports.server.command.bracket.presentation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.springframework.restdocs.cookies.CookieDocumentation.cookieWithName;
import static org.springframework.restdocs.cookies.CookieDocumentation.requestCookies;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.put;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sports.server.command.bracket.dto.BracketRequest;
import com.sports.server.command.member.domain.Member;
import com.sports.server.support.DocumentationTest;
import jakarta.servlet.http.Cookie;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.ResultActions;

class BracketControllerTest extends DocumentationTest {

    @Test
    void 대진표를_배치한다() throws Exception {
        // given
        Long leagueId = 1L;
        BracketRequest.Save request = new BracketRequest.Save(4, List.of(
                new BracketRequest.Entry(1, 1L),
                new BracketRequest.Entry(2, 2L),
                new BracketRequest.Entry(3, 3L),
                new BracketRequest.Entry(4, 4L)
        ));

        doNothing().when(bracketService).replace(any(Member.class), anyLong(), any(BracketRequest.Save.class));

        // when
        ResultActions result = mockMvc.perform(put("/leagues/{leagueId}/bracket", leagueId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .cookie(new Cookie(COOKIE_NAME, "temp-cookie")));

        // then
        result.andExpect(status().isOk())
                .andDo(restDocsHandler.document(
                        pathParameters(
                                parameterWithName("leagueId").description("대진표를 배치할 리그의 ID")
                        ),
                        requestFields(
                                fieldWithPath("size").type(JsonFieldType.NUMBER)
                                        .description("대진표 크기 (2, 4, 8, 16). 리그 최대 라운드 이하"),
                                fieldWithPath("entries").type(JsonFieldType.ARRAY)
                                        .description("1라운드 팀 배치 목록"),
                                fieldWithPath("entries[].position").type(JsonFieldType.NUMBER)
                                        .description("배치 위치 (1 ~ size). 비어있는 위치는 부전승"),
                                fieldWithPath("entries[].teamId").type(JsonFieldType.NUMBER)
                                        .description("배치할 팀 ID")
                        ),
                        requestCookies(
                                cookieWithName(COOKIE_NAME).description("로그인을 통해 얻은 토큰")
                        )
                ));
    }
}
