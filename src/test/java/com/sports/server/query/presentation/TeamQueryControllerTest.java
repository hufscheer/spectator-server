package com.sports.server.query.presentation;

import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;

import com.sports.server.query.dto.response.*;
import com.sports.server.support.DocumentationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class TeamQueryControllerTest extends DocumentationTest {

    @Test
    void 모든_팀을_조회한다() throws Exception {
        // given
        List<String> units = List.of("사회과학대학", "영어대학");
        List<TeamResponse> response = List.of(
                new TeamResponse(1L, "정치외교학과 PSD", "s3:logoImageUrl1", "사회과학대학", "#F7CAC9"),
                new TeamResponse(2L, "국제통상학과 무역풍", "s3:logoImageUrl2", "사회과학대학", "#92A8D1"),
                new TeamResponse(3L, "영어영문학과", "s3:logoImageUrl2", "영어대학", "#92A8D1")
        );

        given(teamQueryService.getAllTeamsByUnits(units)).willReturn(response);

        // when
        ResultActions result = mockMvc.perform(get("/teams")
                .param("units", "사회과학대학", "영어대학")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect((status().isOk()))
                .andDo(restDocsHandler.document(
                        queryParameters(
                                parameterWithName("units").description("필터링할 소속 리스트 (영어대학, 서양어대학, 아시아언어문화대학," +
                                        " 중국학대학, 일본어대학, 사회과학대학, 상경대학, 경영대학, 사범대학, AI융합대학, 국제학부, LD/LT학부" +
                                        " KFL학부, 자유전공학부, 기타)").optional()
                        ),
                        responseFields(
                                combineFields(
                                        fieldWithPath("[].id").type(JsonFieldType.NUMBER).description("팀의 ID"),
                                        getTeamResponseFields("[].")
                                )
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

        List<PlayerResponse> teamPlayers = List.of(
                new PlayerResponse(1L, 10L, "양선수", "202500001", null, 5, null),
                new PlayerResponse(2L, 20L, "김선수", "202500002", null, 2, null),
                new PlayerResponse(3L, 30L, "박선수", "202500003", null, 2, null)
        );

        TeamDetailResponse response = new TeamDetailResponse(
                "정치외교학과 PSD", "s3:logoImageUrl1", "사회과학대학", "#F7CAC9", teamPlayers,
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
                                combineFields(
                                        getTeamResponseFields(""),
                                        fieldWithPath("teamPlayers").type(JsonFieldType.ARRAY).description("팀에 소속된 전체 선수"),
                                        fieldWithPath("teamPlayers[].playerId").type(JsonFieldType.NUMBER).description("선수 ID"),
                                        fieldWithPath("teamPlayers[].teamPlayerId").type(JsonFieldType.NUMBER).description("팀선수 ID"),
                                        fieldWithPath("teamPlayers[].name").type(JsonFieldType.STRING).description("선수 이름"),
                                        fieldWithPath("teamPlayers[].studentNumber").type(JsonFieldType.STRING).description("선수 학번"),
                                        fieldWithPath("teamPlayers[].totalGoalCount").type(JsonFieldType.NUMBER).description("선수의 리그 내 총 득점 수"),

                                        fieldWithPath("winCount").type(JsonFieldType.NUMBER).description("팀의 전체 승리 횟수"),
                                        fieldWithPath("drawCount").type(JsonFieldType.NUMBER).description("팀의 전체 무승부 횟수"),
                                        fieldWithPath("loseCount").type(JsonFieldType.NUMBER).description("팀의 전체 패배 횟수"),
                                        fieldWithPath("topScorers").type(JsonFieldType.ARRAY).description("팀 내 골 득점 순위 (최대 20명)"),
                                        getTeamTopScorerFields("topScorers[]."),
                                        fieldWithPath("trophies").type(JsonFieldType.ARRAY).description("팀의 트로피"),
                                        getTrophyFields("trophies[].")
                                )
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
                        gameTeams1, "PLAYING", 2, false,1L, "혁명 대전"),
                new GameDetailResponse(2L, LocalDateTime.of(2024, 10, 10, 13, 0, 0), "video-id", "승부차기", "외교 대전 4강",
                        gameTeams2, "FINISHED", 4, true, 2L,"외교 대전")
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
                                getGameDetailResponseFields("[].")
                        )
                ));
    }

    @Test
    void 팀별보기_페이지를_조회한다() throws Exception {
        // given
        List<String> units = List.of("사회과학대학", "기타");

        List<TeamDetailResponse.TeamTopScorer> topScorers = List.of(
                new TeamDetailResponse.TeamTopScorer(1L, "21", 1, "양선수", 5),
                new TeamDetailResponse.TeamTopScorer(2L, "20",2, "김선수", 2),
                new TeamDetailResponse.TeamTopScorer(3L, "19",2, "박선수", 2)
        );
        List<TeamDetailResponse.Trophy> trophies = List.of(
                new TeamDetailResponse.Trophy(1L, "2026 외대월드컵", "우승"),
                new TeamDetailResponse.Trophy(2L, "2025 삼건물대회", "준우승")
        );
        TeamDetailResponse teamDetail = new TeamDetailResponse("정치외교학과 PSD", "image url", "사회과학대학",
                "#000000", null, 5, 1, 3, topScorers, trophies);


        List<GameDetailResponse.TeamResponse> gameTeams1 = List.of(
                new GameDetailResponse.TeamResponse(1L, "정치외교학과 PSD", "s3:logoImageUrl1", 3, 0),
                new GameDetailResponse.TeamResponse(2L, "국제통상학과 무역풍", "s3:logoImageUrl2", 1, 0)
        );
        List<GameDetailResponse.TeamResponse> gameTeams2 = List.of(
                new GameDetailResponse.TeamResponse(3L, "정치외교학과 PSD", "s3:logoImageUrl1", 1, 2),
                new GameDetailResponse.TeamResponse(4L, "LD학부", "s3:logoImageUrl3", 1, 1)
        );
        List<GameDetailResponse> recentGames = List.of(
                new GameDetailResponse(1L, LocalDateTime.of(2025, 8, 21, 19, 30, 0), "video-id", "후반전", "혁명 대전 결승",
                        gameTeams1, "PLAYING", 2, false,1L, "혁명 대전"),
                new GameDetailResponse(2L, LocalDateTime.of(2024, 10, 10, 13, 0, 0), "video-id", "승부차기", "외교 대전 4강",
                        gameTeams2, "FINISHED", 4, true, 2L,"외교 대전")
        );

        List<TeamSummaryResponse> teamSummaryResponses = List.of(new TeamSummaryResponse(teamDetail, recentGames));

        given(teamQueryService.getAllTeamsSummary(units)).willReturn(teamSummaryResponses);

        // when
        ResultActions result = mockMvc.perform(get("/teams/summary")
                .param("units", "사회과학대학", "기타")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect((status().isOk()))
                .andDo(restDocsHandler.document(
                        queryParameters(
                                parameterWithName("units").description("필터링할 소속 리스트 (영어대학, 서양어대학, 아시아언어문화대학," +
                                        " 중국학대학, 일본어대학, 사회과학대학, 상경대학, 경영대학, 사범대학, AI융합대학, 국제학부, LD/LT학부," +
                                        " KFL학부, 자유전공학부, 기타)").optional()
                        ),
                        responseFields(
                                combineFields(
                                        fieldWithPath("[].teamDetail").type(JsonFieldType.OBJECT).description("팀 세부 정보"),
                                        fieldWithPath("[].recentGames").type(JsonFieldType.ARRAY).description("최근 경기"),
                                        getTeamResponseFields("[].teamDetail."),
                                        fieldWithPath("[].teamDetail.winCount").type(JsonFieldType.NUMBER).description("전체 승리 횟수"),
                                        fieldWithPath("[].teamDetail.drawCount").type(JsonFieldType.NUMBER).description("전체 무승부 횟수"),
                                        fieldWithPath("[].teamDetail.loseCount").type(JsonFieldType.NUMBER).description("전체 패배 횟수"),
                                        fieldWithPath("[].teamDetail.topScorers").type(JsonFieldType.ARRAY).description("팀 내 골 득점 순위 (최대 3명)"),
                                        getTeamTopScorerFields("[].teamDetail.topScorers[]."),
                                        fieldWithPath("[].teamDetail.trophies").type(JsonFieldType.ARRAY).description("팀의 트로피"),
                                        getTrophyFields("[].teamDetail.trophies[]."),
                                        getGameDetailResponseFields("[].recentGames[]."))
                        )
                )
        );
    }

    private FieldDescriptor[] getTeamResponseFields(String prefix) {
        return new FieldDescriptor[]{
                fieldWithPath(prefix + "name").type(JsonFieldType.STRING).description("팀의 이름"),
                fieldWithPath(prefix + "logoImageUrl").type(JsonFieldType.STRING).description("팀의 로고 이미지 URL"),
                fieldWithPath(prefix + "unit").type(JsonFieldType.STRING).description("팀의 소속 단위"),
                fieldWithPath(prefix + "teamColor").type(JsonFieldType.STRING).description("팀의 대표 색상")
        };
    }

    private FieldDescriptor[] getTeamTopScorerFields(String prefix) {
        return new FieldDescriptor[]{
                fieldWithPath(prefix + "playerId").type(JsonFieldType.NUMBER).description("선수 ID"),
                fieldWithPath(prefix + "admissionYear").type(JsonFieldType.STRING).description("선수 입학년도"),
                fieldWithPath(prefix + "rank").type(JsonFieldType.NUMBER).description("선수의 팀 내 득점 순위"),
                fieldWithPath(prefix + "playerName").type(JsonFieldType.STRING).description("선수 이름"),
                fieldWithPath(prefix + "totalGoals").type(JsonFieldType.NUMBER).description("선수가 득점한 총 골 개수")
        };
    }

    private FieldDescriptor[] getTrophyFields(String prefix) {
        return new FieldDescriptor[]{
                fieldWithPath(prefix + "leagueId").type(JsonFieldType.NUMBER).description("리그 ID"),
                fieldWithPath(prefix + "leagueName").type(JsonFieldType.STRING).description("리그의 이름"),
                fieldWithPath(prefix + "trophyType").type(JsonFieldType.STRING).description("트로피 종류 (우승, 준우승)")
        };
    }

    private FieldDescriptor[] getGameDetailResponseFields(String prefix) {
        return combineFields(
                fieldWithPath(prefix + "gameId").type(JsonFieldType.NUMBER).description("게임의 ID"),
                fieldWithPath(prefix + "leagueId").type(JsonFieldType.NUMBER).description("이 경기가 소속된 리그 id"),
                fieldWithPath(prefix + "leagueName").type(JsonFieldType.STRING).description("이 경기가 소속된 리그 이름"),
                fieldWithPath(prefix + "startTime").type(JsonFieldType.STRING).description("경기 시작 시간"),
                fieldWithPath(prefix + "videoId").type(JsonFieldType.STRING).description("경기 영상 URL"),
                fieldWithPath(prefix + "gameQuarter").type(JsonFieldType.STRING).description("현재 쿼터"),
                fieldWithPath(prefix + "gameName").type(JsonFieldType.STRING).description("경기 이름"),
                fieldWithPath(prefix + "state").type(JsonFieldType.STRING).description("경기 상태 (SCHEDULED, PLAYING, FINISHED)"),
                fieldWithPath(prefix + "round").type(JsonFieldType.NUMBER).description("현재 라운드"),
                fieldWithPath(prefix + "isPkTaken").type(JsonFieldType.BOOLEAN).description("승부차기 진행 여부"),

                fieldWithPath(prefix + "gameTeams").type(JsonFieldType.ARRAY).description("경기에 참여하는 두 팀의 정보"),
                fieldWithPath(prefix + "gameTeams[].gameTeamId").type(JsonFieldType.NUMBER).description("경기 팀의 ID"),
                fieldWithPath(prefix + "gameTeams[].gameTeamName").type(JsonFieldType.STRING).description("경기 팀의 이름"),
                fieldWithPath(prefix + "gameTeams[].logoImageUrl").type(JsonFieldType.STRING).description("경기 팀의 로고 이미지 URL"),
                fieldWithPath(prefix + "gameTeams[].score").type(JsonFieldType.NUMBER).description("경기 팀의 점수"),
                fieldWithPath(prefix + "gameTeams[].pkScore").type(JsonFieldType.NUMBER).description("경기 팀의 승부차기 점수")
        );
    }

    private FieldDescriptor[] combineFields(Object... descriptors) {
        return Stream.of(descriptors)
                .flatMap(desc -> {
                    if (desc instanceof FieldDescriptor[]) {
                        return Arrays.stream((FieldDescriptor[]) desc);
                    } else {
                        return Stream.of((FieldDescriptor) desc);
                    }
                })
                .toArray(FieldDescriptor[]::new);
    }

}
