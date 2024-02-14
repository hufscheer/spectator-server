package com.sports.server.command.cheertalk.presentation;

import com.sports.server.command.cheertalk.dto.CheerTalkRequest;
import com.sports.server.support.DocumentationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CheerTalkControllerTest extends DocumentationTest {

    @Test
    void 응원톡을_저장한다() throws Exception {
        // given
        CheerTalkRequest request = new CheerTalkRequest("응원해요~", 1L);


        // when
        ResultActions result = mockMvc.perform(post("/cheer-talks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        );

        result.andExpect((status().isOk()))
                .andDo(restDocsHandler.document(
                        requestFields(
                                fieldWithPath("content").type(JsonFieldType.STRING).description("응원톡의 내용"),
                                fieldWithPath("gameTeamId").type(JsonFieldType.NUMBER).description("응원하는 게임팀")
                        )
                ));
    }
}
