package com.sports.server.command.game.presentation;


import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sports.server.command.game.dto.CheerCountUpdateRequest;
import com.sports.server.support.DocumentationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.ResultActions;

public class GameControllerTest extends DocumentationTest {

    @Test
    void 응원_횟수를_업데이트한다() throws Exception {

        //given
        Long gameId = 1L;
        CheerCountUpdateRequest request = new CheerCountUpdateRequest(1L, 1);

        //when
        ResultActions result = mockMvc.perform(post("/games/{gameId}/cheer", gameId, request)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        );

        //then
        result.andExpect((status().isOk()))
                .andDo(RESULT_HANDLER.document(
                        pathParameters(
                                parameterWithName("gameId").description("게임의 ID")
                        ),
                        requestFields(
                                fieldWithPath("gameTeamId").type(JsonFieldType.NUMBER).description("게임팀의 id"),
                                fieldWithPath("cheerCount").type(JsonFieldType.NUMBER).description("증가시킬 응원 횟수")

                        )
                ));
    }
}
