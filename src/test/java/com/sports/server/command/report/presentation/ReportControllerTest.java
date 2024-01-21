package com.sports.server.command.report.presentation;

import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sports.server.command.report.dto.ReportRequest;
import com.sports.server.support.DocumentationTest;
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
                .andDo(RESULT_HANDLER.document(
                        requestFields(
                                fieldWithPath("cheerTalkId").type(JsonFieldType.NUMBER).description("신고하는 응원톡의 ID")
                        )
                ));
    }

}
