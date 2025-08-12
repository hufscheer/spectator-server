package com.sports.server.query.presentation;

import com.sports.server.query.dto.response.PlayerResponse;
import com.sports.server.query.dto.response.TeamResponse;
import com.sports.server.support.DocumentationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Collections;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class PlayerQueryControllerTest extends DocumentationTest {

    @Test
    void 선수_전체를_조회한다() throws Exception {
        // given
        List<TeamResponse> teamResponses1 = List.of(
                new TeamResponse(1L, "정치외교학과 PSD", "s3:logoImageUrl1", "사회과학대학", "#F7CAC9"),
                new TeamResponse(2L, "국제통상학과 무역풍", "s3:logoImageUrl1", "사회과학대학", "#92A8D1")
        );

        List<TeamResponse> teamResponses2 = List.of(
                new TeamResponse(1L, "정치외교학과 PSD", "s3:logoImageUrl1", "사회과학대학", "#F7CAC9")
        );

        List<PlayerResponse> responses = List.of(
                new PlayerResponse(1L, "선수1", "202500001", null, 0, teamResponses1),
                new PlayerResponse(2L, "선수2", "202500002", null, 5, teamResponses2),
                new PlayerResponse(3L, "선수3", "202500003", null, 10, Collections.emptyList())
        );

        given(playerQueryService.getAllPlayers())
                .willReturn(responses);

        // when
        ResultActions result = mockMvc.perform(get("/players")
                .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        result.andExpect((status().isOk()))
                .andDo(restDocsHandler.document(
                        responseFields(
                                fieldWithPath("[].playerId").type(JsonFieldType.NUMBER).description("선수의 ID"),
                                fieldWithPath("[].name").type(JsonFieldType.STRING).description("선수의 이름"),
                                fieldWithPath("[].studentNumber").type(JsonFieldType.STRING).description("선수의 학번"),
                                fieldWithPath("[].totalGoalCount").type(JsonFieldType.NUMBER).description("선수의 전체 골 개수"),
                                fieldWithPath("[].teams").type(JsonFieldType.ARRAY).description("선수의 모든 소속팀 목록"),
                                fieldWithPath("[].teams[].id").type(JsonFieldType.NUMBER).description("소속팀의 ID"),
                                fieldWithPath("[].teams[].name").type(JsonFieldType.STRING).description("소속팀의 이름"),
                                fieldWithPath("[].teams[].logoImageUrl").type(JsonFieldType.STRING).description("소속팀의 로고 이미지 url"),
                                fieldWithPath("[].teams[].unit").type(JsonFieldType.STRING).description("소속팀의 소속 단위"),
                                fieldWithPath("[].teams[].teamColor").type(JsonFieldType.STRING).description("소속팀의 대표 색상")
                        )
                ));
    }

    @Test
    void 선수를_상세_조회한다() throws Exception {
        // given
        Long playerId = 1L;
        List<TeamResponse> teamResponses = List.of(
                new TeamResponse(1L, "정치외교학과 PSD", "s3:logoImageUrl1", "사회과학대학", "#F7CAC9"),
                new TeamResponse(2L, "국제통상학과 무역풍", "s3:logoImageUrl1", "사회과학대학", "#92A8D1")
        );
        PlayerResponse response = new PlayerResponse(playerId, "선수1", "202500001", null,  0, teamResponses);
        given(playerQueryService.getPlayerDetail(playerId))
                .willReturn(response);

        // when
        ResultActions result = mockMvc.perform(get("/players/{playerId}", playerId)
                .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        result.andExpect((status().isOk()))
                .andDo(restDocsHandler.document(
                        pathParameters(
                                parameterWithName("playerId").description("선수의 ID")
                        ),
                        responseFields(
                                fieldWithPath("playerId").type(JsonFieldType.NUMBER).description("선수의 ID"),
                                fieldWithPath("name").type(JsonFieldType.STRING).description("선수의 이름"),
                                fieldWithPath("studentNumber").type(JsonFieldType.STRING).description("선수의 학번"),
                                fieldWithPath("totalGoalCount").type(JsonFieldType.NUMBER).description("선수의 전체 골 개수"),
                                fieldWithPath("teams").type(JsonFieldType.ARRAY).description("선수의 모든 소속팀 목록"),
                                fieldWithPath("teams[].id").type(JsonFieldType.NUMBER).description("소속팀의 ID"),
                                fieldWithPath("teams[].name").type(JsonFieldType.STRING).description("소속팀의 이름"),
                                fieldWithPath("teams[].logoImageUrl").type(JsonFieldType.STRING).description("소속팀의 로고 이미지 url"),
                                fieldWithPath("teams[].unit").type(JsonFieldType.STRING).description("소속팀의 소속 단위"),
                                fieldWithPath("teams[].teamColor").type(JsonFieldType.STRING).description("소속팀의 대표 색상")
                        )
                ));
    }

}
