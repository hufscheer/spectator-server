package com.sports.server.query.presentation;

import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;

import com.sports.server.query.dto.response.GameDetailResponse;
import com.sports.server.query.dto.response.PlayerResponse;
import com.sports.server.query.dto.response.TeamDetailResponse;
import com.sports.server.query.dto.response.TeamResponse;
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
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class TeamQueryControllerTest extends DocumentationTest {

    @Test
    void 모든_팀을_조회한다() throws Exception {
        // given
        List<TeamResponse> response = List.of(
                new TeamResponse(1L, "정치외교학과 PSD", "s3:logoImageUrl1", "사회과학대학", "#F7CAC9"),
                new TeamResponse(2L, "국제통상학과 무역풍", "s3:logoImageUrl2", "사회과학대학", "#92A8D1")
        );

        given(teamQueryService.getAllTeams()).willReturn(response);

        // when
        ResultActions result = mockMvc.perform(get("/teams")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect((status().isOk()))
                .andDo(restDocsHandler.document(
                        responseFields(
                                fieldWithPath("[].id").type(JsonFieldType.NUMBER).description("팀의 ID"),
                                fieldWithPath("[].name").type(JsonFieldType.STRING).description("팀의 이름"),
                                fieldWithPath("[].logoImageUrl").type(JsonFieldType.STRING).description("팀의 로고 이미지 URL"),
                                fieldWithPath("[].unit").type(JsonFieldType.STRING).description("팀의 소속 단위"),
                                fieldWithPath("[].teamColor").type(JsonFieldType.STRING).description("팀의 대표 색상")
                        )
                ));
    }

    @Test
    void 팀을_상세_조회한다() throws Exception {
        // given
        Long teamId = 1L;
        List<PlayerResponse> players = List.of(
                new PlayerResponse(1L, "양효빈", "202500001", null, 5, null),
                new PlayerResponse(2L, "양뚜이", "202500002", null, 2, null)
        );
        TeamDetailResponse response = new TeamDetailResponse(
                "정치외교학과 PSD", "s3:logoImageUrl1", "사회과학대학", "#F7CAC9", players);

        given(teamQueryService.getTeamDetail(teamId)).willReturn(response);

        // when
        ResultActions result = mockMvc.perform(get("/teams/{teamId}", teamId)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect((status().isOk()))
                .andDo(restDocsHandler.document(
                        pathParameters(
                                parameterWithName("teamId").description("팀 ID")
                        ),
                        responseFields(
                                fieldWithPath("name").type(JsonFieldType.STRING).description("팀의 이름"),
                                fieldWithPath("logoImageUrl").type(JsonFieldType.STRING).description("팀의 로고 이미지 URL"),
                                fieldWithPath("unit").type(JsonFieldType.STRING).description("팀의 소속 단위"),
                                fieldWithPath("teamColor").type(JsonFieldType.STRING).description("팀의 대표 색상"),
                                fieldWithPath("teamPlayers").type(JsonFieldType.ARRAY).description("팀 소속 선수 목록"),
                                fieldWithPath("teamPlayers[].playerId").type(JsonFieldType.NUMBER).description("선수의 ID"),
                                fieldWithPath("teamPlayers[].name").type(JsonFieldType.STRING).description("선수의 이름"),
                                fieldWithPath("teamPlayers[].studentNumber").type(JsonFieldType.STRING).description("선수의 학번"),
                                fieldWithPath("teamPlayers[].totalGoalCount").type(JsonFieldType.NUMBER).description("선수의 총 골 개수"))
                ));
    }

    @Test
    void 팀이_참가한_모든_경기를_조회한다() throws Exception {
        // given
        Long teamId = 1L;
        List<GameDetailResponse.TeamResponse> gameTeams = List.of(
                new GameDetailResponse.TeamResponse(1L, "정치외교학과 PSD", "s3:logoImageUrl1", 3, 0),
                new GameDetailResponse.TeamResponse(2L, "국제통상학과 무역풍", "s3:logoImageUrl2", 1, 0)
        );

        List<GameDetailResponse> responses = List.of(
                new GameDetailResponse(1L, LocalDateTime.now(), "video-id", "전반전", "혁명 대전 결승",
                        gameTeams, "PLAYING", 2, false, "혁명 대전")
        );

        given(gameQueryService.getAllGamesDetailByTeam(teamId)).willReturn(responses);

        // when
        ResultActions result = mockMvc.perform(get("/teams/{teamId}/games", teamId)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect((status().isOk()))
                .andDo(restDocsHandler.document(
                        pathParameters(
                                parameterWithName("teamId").description("팀 ID")
                        ),
                        responseFields(
                                fieldWithPath("[].gameId").type(JsonFieldType.NUMBER).description("게임의 ID"),
                                fieldWithPath("[].startTime").type(JsonFieldType.STRING).description("경기 시작 시간"),
                                fieldWithPath("[].videoId").type(JsonFieldType.STRING).description("경기 영상 URL"),
                                fieldWithPath("[].gameQuarter").type(JsonFieldType.STRING).description("현재 쿼터"),
                                fieldWithPath("[].gameName").type(JsonFieldType.STRING).description("경기 이름"),
                                fieldWithPath("[].state").type(JsonFieldType.STRING).description("경기 상태 (SCHEDULED, PLAYING, FINISHED)"),
                                fieldWithPath("[].round").type(JsonFieldType.NUMBER).description("현재 라운드"),
                                fieldWithPath("[].isPkTaken").type(JsonFieldType.BOOLEAN).description("승부차기 진행 여부"),
                                fieldWithPath("[].leagueName").type(JsonFieldType.STRING).description("이 경기가 소속된 리그 이름"),
                                fieldWithPath("[].gameTeams").type(JsonFieldType.ARRAY).description("경기에 참여하는 두 팀의 정보"),
                                fieldWithPath("[].gameTeams[].gameTeamId").type(JsonFieldType.NUMBER).description("경기 팀의 ID"),
                                fieldWithPath("[].gameTeams[].gameTeamName").type(JsonFieldType.STRING).description("경기 팀의 이름"),
                                fieldWithPath("[].gameTeams[].logoImageUrl").type(JsonFieldType.STRING).description("경기 팀의 로고 이미지 URL"),
                                fieldWithPath("[].gameTeams[].score").type(JsonFieldType.NUMBER).description("경기 팀의 점수"),
                                fieldWithPath("[].gameTeams[].pkScore").type(JsonFieldType.NUMBER).description("경기 팀의 승부차기 점수")
                        )
                ));
    }

}
