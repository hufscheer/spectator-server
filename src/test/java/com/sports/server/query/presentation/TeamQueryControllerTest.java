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
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class TeamQueryControllerTest extends DocumentationTest {

    @Test
    void 모든_팀을_단위별로_조회한다() throws Exception {
        // given
        List<String> units = List.of("SOCIAL_SCIENCES", "ENGLISH");
        List<TeamResponse> response = List.of(
                new TeamResponse(1L, "정치외교학과 PSD", "s3:logoImageUrl1", "사회과학대학", "#F7CAC9"),
                new TeamResponse(2L, "국제통상학과 무역풍", "s3:logoImageUrl2", "사회과학대학", "#92A8D1"),
                new TeamResponse(3L, "영어영문학과", "s3:logoImageUrl2", "영어대학", "#92A8D1")
        );

        given(teamQueryService.getAllTeamsByUnits(units)).willReturn(response);

        // when
        ResultActions result = mockMvc.perform(get("/teams")
                .param("units", "SOCIAL_SCIENCES", "ENGLISH")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect((status().isOk()))
                .andDo(restDocsHandler.document(
                        queryParameters(
                                parameterWithName("units").description("필터링할 소속 단위 리스트 (ENGLISH, OCCIDENTAL_LANGUAGES," +
                                        " ASIAN_LANGUAGES_AND_CULTURE, CHINESE_STUDIES," +
                                        " JAPANESE_STUDIES, SOCIAL_SCIENCES, BUSINESS_AND_ECONOMICS," +
                                        " BUSINESS, EDUCATION, AI_CONVERGENCE, INTERNATIONAL_STUDIES," +
                                        " LANGUAGE_AND_DIPLOMACY, LANGUAGE_AND_TRADE, KOREAN_AS_A_FOREIGN_LANGUAGE," +
                                        " LIBERAL_ARTS, ETC)").optional()
                        ),
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
        List<TeamDetailResponse.TeamTopScorer> topScorers = List.of(
                new TeamDetailResponse.TeamTopScorer(1L, "21", 1, "양선수", 5),
                new TeamDetailResponse.TeamTopScorer(2L, "20",2, "김선수", 2),
                new TeamDetailResponse.TeamTopScorer(3L, "19",2, "박선수", 2)
        );

        List<TeamDetailResponse.Trophy> trophies = List.of(
                new TeamDetailResponse.Trophy(1L, "2025 외대 월드컵", "우승"),
                new TeamDetailResponse.Trophy(2L, "2024 트로이카", "준우승")
        );

        TeamDetailResponse response = new TeamDetailResponse(
                "정치외교학과 PSD", "s3:logoImageUrl1", "사회과학대학", "#F7CAC9",
                5, 2, 3, topScorers, trophies);

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
                                fieldWithPath("winCount").type(JsonFieldType.NUMBER).description("팀의 전체 승리 횟수"),
                                fieldWithPath("drawCount").type(JsonFieldType.NUMBER).description("팀의 전체 무승부 횟수"),
                                fieldWithPath("loseCount").type(JsonFieldType.NUMBER).description("팀의 전체 패배 횟수"),

                                fieldWithPath("topScorers").type(JsonFieldType.ARRAY).description("팀 내 골 득점 순위 (최대 20명)"),
                                fieldWithPath("topScorers[].playerId").type(JsonFieldType.NUMBER).description("선수 ID"),
                                fieldWithPath("topScorers[].admissionYear").type(JsonFieldType.STRING).description("선수 입학년도"),
                                fieldWithPath("topScorers[].rank").type(JsonFieldType.NUMBER).description("선수의 팀 내 득점 순위"),
                                fieldWithPath("topScorers[].playerName").type(JsonFieldType.STRING).description("선수 이름"),
                                fieldWithPath("topScorers[].totalGoals").type(JsonFieldType.NUMBER).description("선수가 득점한 총 골 개수"),

                                fieldWithPath("trophies").type(JsonFieldType.ARRAY).description("팀의 트로피"),
                                fieldWithPath("trophies[].leagueId").type(JsonFieldType.NUMBER).description("리그 ID"),
                                fieldWithPath("trophies[].leagueName").type(JsonFieldType.STRING).description("리그의 이름"),
                                fieldWithPath("trophies[].trophyType").type(JsonFieldType.STRING).description("트로피 종류 (우승, 준우승)")
                        )
                ));
    }

    @Test
    void 팀에_소속된_모든_선수를_조회한다() throws Exception {
        // given
        Long teamId = 1L;
        List<PlayerResponse> players = List.of(
                new PlayerResponse(1L, 10L, "양선수", "202500001", null, 5, null),
                new PlayerResponse(2L, 20L, "김선수", "202500002", null, 2, null),
                new PlayerResponse(3L, 30L, "박선수", "202500003", null, 2, null)
        );

        given(teamQueryService.getAllTeamPlayers(teamId)).willReturn(players);

        // when
        ResultActions result = mockMvc.perform(get("/teams/{teamId}/players", teamId)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isOk())
                .andDo(restDocsHandler.document(
                        pathParameters(
                                parameterWithName("teamId").description("팀 ID")
                        ),
                        responseFields(
                                fieldWithPath("[].playerId").type(JsonFieldType.NUMBER).description("선수 ID"),
                                fieldWithPath("[].teamPlayerId").type(JsonFieldType.NUMBER).description("팀선수 ID"),
                                fieldWithPath("[].name").type(JsonFieldType.STRING).description("선수 이름"),
                                fieldWithPath("[].studentNumber").type(JsonFieldType.STRING).description("선수 학번"),
                                fieldWithPath("[].totalGoalCount").type(JsonFieldType.NUMBER).description("선수의 리그 내 총 득점 수")
                        )
                ));
    }

    @Test
    void 팀이_참가한_모든_경기를_조회한다() throws Exception {
        // given
        Long teamId = 1L;

        List<GameDetailResponse.TeamResponse> gameTeams1 = List.of(
                new GameDetailResponse.TeamResponse(1L, "정치외교학과 PSD", "s3:logoImageUrl1", 3, 0),
                new GameDetailResponse.TeamResponse(2L, "국제통상학과 무역풍", "s3:logoImageUrl2", 1, 0)
        );
        List<GameDetailResponse.TeamResponse> gameTeams2 = List.of(
                new GameDetailResponse.TeamResponse(3L, "정치외교학과 PSD", "s3:logoImageUrl1", 1, 2),
                new GameDetailResponse.TeamResponse(4L, "LD학부", "s3:logoImageUrl3", 1, 1)
        );

        List<GameDetailResponse> response = List.of(
                new GameDetailResponse(1L, LocalDateTime.of(2025, 8, 21, 19, 30, 0), "video-id", "후반전", "혁명 대전 결승",
                        gameTeams1, "PLAYING", 2, false, "혁명 대전"),
                new GameDetailResponse(2L, LocalDateTime.of(2024, 10, 10, 13, 0, 0), "video-id", "승부차기", "외교 대전 4강",
                        gameTeams2, "FINISHED", 4, true, "외교 대전")
        );

        given(gameQueryService.getAllGamesDetailByTeam(teamId)).willReturn(response);

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
                                fieldWithPath("[].leagueName").type(JsonFieldType.STRING).description("이 경기가 소속된 리그 이름"),
                                fieldWithPath("[].startTime").type(JsonFieldType.STRING).description("경기 시작 시간"),
                                fieldWithPath("[].videoId").type(JsonFieldType.STRING).description("경기 영상 URL"),
                                fieldWithPath("[].gameQuarter").type(JsonFieldType.STRING).description("현재 쿼터"),
                                fieldWithPath("[].gameName").type(JsonFieldType.STRING).description("경기 이름"),
                                fieldWithPath("[].state").type(JsonFieldType.STRING).description("경기 상태 (SCHEDULED, PLAYING, FINISHED)"),
                                fieldWithPath("[].round").type(JsonFieldType.NUMBER).description("현재 라운드"),
                                fieldWithPath("[].isPkTaken").type(JsonFieldType.BOOLEAN).description("승부차기 진행 여부"),

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
