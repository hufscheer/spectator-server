package com.sports.server.query.presentation;

import com.sports.server.query.dto.response.GameDetailResponse;
import com.sports.server.support.DocumentationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GameQueryControllerTest extends DocumentationTest {

    @Test
    void 게임을_상세_조회한다() throws Exception {
        // given
        Long gameId = 1L;
        List<GameDetailResponse.TeamResponse> gameTeams = List.of(
                new GameDetailResponse.TeamResponse(
                        1L, "A팀", "logo.com", 2, 1),
                new GameDetailResponse.TeamResponse(
                        2L, "B팀", "logo.com", 1, 2)
        );
        LocalDateTime startTime = LocalDateTime.of(2024, 1, 19, 13, 0, 0);
        GameDetailResponse response = new GameDetailResponse(
                startTime, "videoId", "전반전", "4강", "축구", gameTeams
        );
        given(gameQueryService.getGameDetail(gameId))
                .willReturn(response);

        // when
        ResultActions result = mockMvc.perform(get("/games/{gameId}", gameId)
                .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        result.andExpect((status().isOk()))
                .andDo(RESULT_HANDLER.document(
                        responseFields(
                                fieldWithPath("startTime").type(JsonFieldType.STRING).description("게임 시작 시간"),
                                fieldWithPath("videoId").type(JsonFieldType.STRING).description("게임 비디오 ID"),
                                fieldWithPath("gameQuarter").type(JsonFieldType.STRING).description("게임 쿼터"),
                                fieldWithPath("gameName").type(JsonFieldType.STRING).description("게임 이름"),
                                fieldWithPath("sportName").type(JsonFieldType.STRING).description("종목"),
                                fieldWithPath("sportName").type(JsonFieldType.STRING).description("종목"),
                                fieldWithPath("gameTeams[].gameTeamId").type(JsonFieldType.NUMBER).description("게임팀의 ID"),
                                fieldWithPath("gameTeams[].gameTeamName").type(JsonFieldType.STRING).description("게임팀의 이름"),
                                fieldWithPath("gameTeams[].logoImageUrl").type(JsonFieldType.STRING).description("게임팀의 이미지 URL"),
                                fieldWithPath("gameTeams[].score").type(JsonFieldType.NUMBER).description("게임팀의 현재 점수"),
                                fieldWithPath("gameTeams[].order").type(JsonFieldType.NUMBER).description("게임팀의 순서")
                        )
                ));
    }
}
