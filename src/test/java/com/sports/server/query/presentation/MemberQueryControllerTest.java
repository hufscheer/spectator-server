package com.sports.server.query.presentation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sports.server.query.dto.response.MemberResponse;
import com.sports.server.support.DocumentationTest;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.ResultActions;

public class MemberQueryControllerTest extends DocumentationTest {

    @Test
    void 멤버의_정보를_조회한다() throws Exception {
        // given
        Cookie cookie = new Cookie(COOKIE_NAME, "temp-cookie");
        when(memberQueryService.getMemberInfo(any())).thenReturn(new MemberResponse("test@gmail.com", "축구 협회"));

        // when
        ResultActions result = mockMvc.perform(get("/members/info")
                .contentType(MediaType.APPLICATION_JSON)
                .cookie(cookie)
        );

        // then
        result.andExpect(status().isOk())
                .andDo(restDocsHandler.document(
                        responseFields(
                                fieldWithPath("email").type(JsonFieldType.STRING).description("멤버의 이메일"),
                                fieldWithPath("nameOfOrganization").type(JsonFieldType.STRING).description("멤버가 속한 단체명")
                        )
                ));
    }

}
