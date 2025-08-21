package com.sports.server.command.player.presentation;

import com.sports.server.command.player.dto.PlayerRequest;
import com.sports.server.support.DocumentationTest;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.restdocs.cookies.CookieDocumentation.cookieWithName;
import static org.springframework.restdocs.cookies.CookieDocumentation.requestCookies;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class PlayerControllerTest extends DocumentationTest {

    @Test
    void 선수를_생성한다() throws Exception {
        // given
        PlayerRequest.Register request = new PlayerRequest.Register("손흥민", "202500001");

        // when
        ResultActions result = mockMvc.perform(post("/players")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .cookie(new Cookie(COOKIE_NAME, "temp-cookie"))
        );

        // then
        result.andExpect(status().isCreated())
                .andDo(restDocsHandler.document(
                        requestFields(
                                fieldWithPath("name").type(JsonFieldType.STRING).description("선수의 이름"),
                                fieldWithPath("studentNumber").type(JsonFieldType.STRING).description("선수의 학번")
                        ),
                        requestCookies(
                                cookieWithName(COOKIE_NAME).description("로그인을 통해 얻은 토큰")
                        )
                ));
    }

    @Test
    void 선수_정보를_수정한다() throws Exception {
        // given
        Long playerId = 1L;
        PlayerRequest.Update request = new PlayerRequest.Update("손흥민", "202500001");

        // when
        ResultActions result = mockMvc.perform(patch("/players/{playerId}", playerId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .cookie(new Cookie(COOKIE_NAME, "temp-cookie"))
        );

        // then
        result.andExpect(status().isOk())
                .andDo(restDocsHandler.document(
                        pathParameters(
                                parameterWithName("playerId").description("선수의 ID")
                        ),
                        requestFields(
                                fieldWithPath("name").type(JsonFieldType.STRING).description("수정할 선수의 이름"),
                                fieldWithPath("studentNumber").type(JsonFieldType.STRING).description("수정할 선수의 학번")
                        ),
                        requestCookies(
                                cookieWithName(COOKIE_NAME).description("로그인을 통해 얻은 토큰")
                        )
                ));
    }

    @Test
    void 선수를_삭제한다() throws Exception {
        // given
        Long playerId = 1L;

        // when
        ResultActions result = mockMvc.perform(delete("/players/{playerId}", playerId)
                .cookie(new Cookie(COOKIE_NAME, "temp-cookie")));

        // then
        result.andExpect(status().isNoContent())
                .andDo(restDocsHandler.document(
                        pathParameters(
                                parameterWithName("playerId").description("선수의 ID")
                        ),
                        requestCookies(
                                cookieWithName(COOKIE_NAME).description("로그인을 통해 얻은 토큰")
                        )
                ));
    }
}
