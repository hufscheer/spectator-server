package com.sports.server.query.presentation;

import com.sports.server.query.dto.response.OrganizationResponse;
import com.sports.server.support.DocumentationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class OrganizationQueryControllerTest extends DocumentationTest {

    @Test
    void 학교_목록을_조회한다() throws Exception {
        // given
        List<OrganizationResponse> responses = List.of(
                new OrganizationResponse(1L, "한국외대"),
                new OrganizationResponse(2L, "경희대")
        );

        given(organizationQueryService.findAll())
                .willReturn(responses);

        // when
        ResultActions result = mockMvc.perform(get("/organizations")
                .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        result.andExpect(status().isOk())
                .andDo(restDocsHandler.document(
                        responseFields(
                                fieldWithPath("[].id").type(JsonFieldType.NUMBER).description("학교의 ID"),
                                fieldWithPath("[].name").type(JsonFieldType.STRING).description("학교의 이름")
                        )
                ));
    }
}
