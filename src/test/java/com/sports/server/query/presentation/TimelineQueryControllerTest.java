package com.sports.server.query.presentation;

import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sports.server.query.dto.response.TimelineResponse;
import com.sports.server.query.dto.response.TimelineResponse.RecordResponse;
import com.sports.server.support.DocumentationTest;
import java.util.List;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.ResultActions;

public class TimelineQueryControllerTest extends DocumentationTest {

    @Test
    @Disabled
    void 타임라인을_조회한다() throws Exception {

        // given
        Long gameId = 1L;

        List<TimelineResponse> responses = List.of(
                new TimelineResponse("쿼터1", List.of(
                        new RecordResponse(25),
                        new RecordResponse(30)
                )),
                new TimelineResponse("쿼터2", List.of(
                        new RecordResponse(25),
                        new RecordResponse(30)
                ))

        );

        given(timelineQueryService.getTimeline(gameId))
                .willReturn(responses);

        // when
        ResultActions result = mockMvc.perform(get("/games/{gameId}/timeline", gameId)
                .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        result.andExpect((status().isOk()))
                .andDo(RESULT_HANDLER.document(
                        pathParameters(
                                parameterWithName("gameId").description("게임의 ID")
                        ),
                        responseFields(
                                fieldWithPath("[].gameQuarter").type(JsonFieldType.STRING).description("쿼터의 이름"),
                                fieldWithPath("[].records[].scoredAt").type(JsonFieldType.NUMBER).description("득점한 시간"),
                                fieldWithPath("[].records[].playerName").type(JsonFieldType.STRING)
                                        .description("득점한 선수의 이름"),
                                fieldWithPath("[].records[].teamName").type(JsonFieldType.STRING).description("팀 이름"),
                                fieldWithPath("[].records[].score").type(JsonFieldType.NUMBER).description("득점한 점수")
                        )
                ));


    }
}
