package com.sports.server.common.presentation;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.cookies.CookieDocumentation.cookieWithName;
import static org.springframework.restdocs.cookies.CookieDocumentation.requestCookies;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sports.server.support.DocumentationTest;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;

public class S3ControllerTest extends DocumentationTest {

    @Test
    void 이미지_업로드용_사전_서명_URL을_발급한다() throws Exception {
        // given
        String extension = "jpg";
        String presignedUrl = "https://bucket.s3.ap-northeast-2.amazonaws.com/uuid.jpg?X-Amz-Algorithm=...";
        when(s3Service.generatePresignedUrl(anyString())).thenReturn(presignedUrl);

        // when
        ResultActions result = mockMvc.perform(get("/manager/aws/generate-presigned-url")
                .param("extension", extension)
                .cookie(new Cookie(COOKIE_NAME, "temp-cookie")));

        // then
        result.andExpect(status().isOk())
                .andDo(restDocsHandler.document(
                        queryParameters(
                                parameterWithName("extension").description("업로드할 파일의 확장자 (예: jpg, png)")
                        ),
                        requestCookies(
                                cookieWithName(COOKIE_NAME).description("로그인을 통해 얻은 토큰")
                        )
                ));
    }
}
