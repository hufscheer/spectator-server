package com.sports.server.query.presentation;


import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sports.server.common.dto.PageRequestDto;
import com.sports.server.query.dto.response.CheerTalkResponse;
import com.sports.server.support.DocumentationTest;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.ResultActions;

public class CheerTalkQueryControllerTest extends DocumentationTest {

    @Test
    void 응원톡을_조회한다() throws Exception {

        //given
        Long gameId = 1L;

        PageRequestDto pageRequestDto = new PageRequestDto(1L, 2);

        LocalDateTime createdAt = LocalDateTime.of(2024, 1, 21, 11, 46, 0);
        List<CheerTalkResponse> response = List.of(
                new CheerTalkResponse(
                        2L, "응원해요", 1L, createdAt, false, 1
                ),
                new CheerTalkResponse(
                        3L, "파이팅", 2L, createdAt, false, 2
                )
        );

        given(cheerTalkQueryService.getCheerTalksByGameId(gameId, pageRequestDto))
                .willReturn(response);

        // when
        ResultActions result = mockMvc.perform(get("/games/{gameId}/cheer-talks", gameId)
                .queryParam("cursor", String.valueOf(1))
                .queryParam("size", String.valueOf(2))
                .contentType(MediaType.APPLICATION_JSON)
        );

        result.andExpect((status().isOk()))
                .andDo(RESULT_HANDLER.document(
                        queryParameters(
                                parameterWithName("cursor").description("마지막 응원톡의 ID"),
                                parameterWithName("size").description("조회하고자 하는 응원톡의 개수")
                        ),
                        pathParameters(
                                parameterWithName("gameId").description("게임의 ID")
                        ),
                        responseFields(
                                fieldWithPath("[].cheerTalkId").type(JsonFieldType.NUMBER).description("응원톡의 ID"),
                                fieldWithPath("[].content").type(JsonFieldType.STRING).description("응원톡의 내용"),
                                fieldWithPath("[].gameTeamId").type(JsonFieldType.NUMBER)
                                        .description("응원톡에 해당하는 게임팀의 ID"),
                                fieldWithPath("[].createdAt").type(JsonFieldType.STRING).description("생성된 날짜 및 시각"),
                                fieldWithPath("[].isBlocked").type(JsonFieldType.BOOLEAN).description("응원톡의 블락 여부"),
                                fieldWithPath("[].order").type(JsonFieldType.NUMBER).description("게임팀의 순서")
                        )
                ));
    }

}
