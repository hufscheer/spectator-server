package com.sports.server.auth.presentation;

import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.cookies.CookieDocumentation.cookieWithName;
import static org.springframework.restdocs.cookies.CookieDocumentation.responseCookies;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sports.server.auth.JwtResponse;
import com.sports.server.auth.dto.LoginVO;
import com.sports.server.support.DocumentationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.ResultActions;

public class AuthControllerTest extends DocumentationTest {

    @Test
    void 로그인을_한다() throws Exception {

        // given
        String email = "example@example.com";
        String password = "1234";
        LoginVO loginVO = new LoginVO(email, password);
        JwtResponse jwtResponse = new JwtResponse("testAccessToken");

        given(authService.managerLogin(loginVO)).willReturn(jwtResponse);

        // when
        ResultActions result = mockMvc.perform(post("/manager/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginVO))
        );

        // then
        result.andExpect(status().isOk())
                .andDo(restDocsHandler.document(
                        requestFields(
                                fieldWithPath("email").type(JsonFieldType.STRING).description("사용자의 이메일"),
                                fieldWithPath("password").type(JsonFieldType.STRING).description("비밀번호")
                        ),
                        responseCookies(
                                cookieWithName("HCC_SES").description("JWT 액세스 토큰이 담긴 쿠키")
                        )
                ));
    }
}
