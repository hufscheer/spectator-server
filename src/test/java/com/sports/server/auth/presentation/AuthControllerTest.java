package com.sports.server.auth.presentation;

import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.cookies.CookieDocumentation.cookieWithName;
import static org.springframework.restdocs.cookies.CookieDocumentation.responseCookies;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sports.server.auth.dto.LoginResponse;
import com.sports.server.auth.dto.LoginRequest;
import com.sports.server.support.DocumentationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.ResultActions;

public class AuthControllerTest extends DocumentationTest {

    @Value("${cookie.name}")
    public String COOKIE_NAME;

    @Test
    void 로그인을_한다() throws Exception {

        // given
        String email = "example@example.com";
        String password = "1234";
        LoginRequest loginRequest = new LoginRequest(email, password);
        LoginResponse loginResponse = new LoginResponse("testAccessToken");

        given(authService.loginByManager(loginRequest)).willReturn(loginResponse);

        // when
        ResultActions result = mockMvc.perform(post("/manager/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
        );

        // then
        result.andExpect(status().isOk())
                .andDo(restDocsHandler.document(
                        requestFields(
                                fieldWithPath("email").type(JsonFieldType.STRING).description("사용자의 이메일"),
                                fieldWithPath("password").type(JsonFieldType.STRING).description("비밀번호")
                        ),
                        responseCookies(
                                cookieWithName(COOKIE_NAME).description("JWT 액세스 토큰이 담긴 쿠키")
                        )
                ));
    }
}
