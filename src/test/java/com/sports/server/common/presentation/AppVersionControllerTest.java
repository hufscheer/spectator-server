package com.sports.server.common.presentation;

import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sports.server.support.DocumentationTest;
import org.junit.jupiter.api.Test;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.ResultActions;

public class AppVersionControllerTest extends DocumentationTest {

    @Test
    void 앱_최소_버전_정책을_조회한다() throws Exception {
        // when
        ResultActions result = mockMvc.perform(get("/app/versions"));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.ios.minimumBuild").value(0))
                .andExpect(jsonPath("$.android.minimumBuild").value(0))
                .andDo(restDocsHandler.document(
                        responseFields(
                                fieldWithPath("ios.minimumBuild").type(JsonFieldType.NUMBER).description("iOS 앱이 실행되려면 필요한 최소 빌드 번호. 이보다 낮은 빌드는 업데이트해야 한다 (기본 0)"),
                                fieldWithPath("ios.storeUrl").type(JsonFieldType.STRING).optional().description("iOS 앱 스토어 주소 (설정이 없으면 null)"),
                                fieldWithPath("android.minimumBuild").type(JsonFieldType.NUMBER).description("Android 앱이 실행되려면 필요한 최소 빌드 번호. 이보다 낮은 빌드는 업데이트해야 한다 (기본 0)"),
                                fieldWithPath("android.storeUrl").type(JsonFieldType.STRING).optional().description("Android 앱 스토어 주소 (설정이 없으면 null)")
                        )
                ));
    }
}
