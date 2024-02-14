package com.sports.server.query.presentation;

import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sports.server.query.dto.response.SportResponse;
import com.sports.server.support.DocumentationTest;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.ResultActions;

public class SportQueryControllerTest extends DocumentationTest {

    @Test
    void 종목을_전체_조회한다() throws Exception {

        // given
        List<SportResponse> responses = List.of(
                new SportResponse(1L, "농구"),
                new SportResponse(2L, "루미큐브")
        );

        given(sportQueryService.findAll())
                .willReturn(responses);

        // when
        ResultActions result = mockMvc.perform(get("/sports")
                .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        result.andExpect((status().isOk()))
                .andDo(restDocsHandler.document(
                        responseFields(
                                fieldWithPath("[].id").type(JsonFieldType.NUMBER).description("종목의 ID"),
                                fieldWithPath("[].name").type(JsonFieldType.STRING).description("종목의 이름")
                        )
                ));

    }

}
