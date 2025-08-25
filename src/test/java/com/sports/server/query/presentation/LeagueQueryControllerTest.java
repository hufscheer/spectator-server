package com.sports.server.query.presentation;

import com.sports.server.command.member.domain.Member;
import com.sports.server.query.dto.response.*;
import com.sports.server.query.dto.response.LeagueResponseWithGames.GameDetail;
import com.sports.server.query.dto.response.LeagueResponseWithGames.GameDetail.GameTeam;
import com.sports.server.query.dto.response.LeagueResponseWithInProgressGames.GameDetailResponse;
import com.sports.server.query.dto.response.LeagueResponseWithInProgressGames.GameDetailResponse.GameTeamResponse;
import com.sports.server.support.DocumentationTest;
import jakarta.servlet.http.Cookie;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import org.springframework.http.MediaType;
import static org.springframework.restdocs.cookies.CookieDocumentation.cookieWithName;
import static org.springframework.restdocs.cookies.CookieDocumentation.requestCookies;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import org.springframework.restdocs.payload.JsonFieldType;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import org.springframework.test.web.servlet.ResultActions;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class LeagueQueryControllerTest extends DocumentationTest {

    @Test
    void 리그_전체를_조회한다() throws Exception {

        // given
        List<LeagueResponse> responses = List.of(
                new LeagueResponse(1L, "리그 첫번째", 16, 4, "종료"),
                new LeagueResponse(2L, "리그 두번째", 32, 32, "진행 중")
        );

        int year = 2024;
        given(leagueQueryService.findLeagues(year))
                .willReturn(responses);

        // when
        ResultActions result = mockMvc.perform(get("/leagues")
                .queryParam("year", String.valueOf(year))
                .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        result.andExpect((status().isOk()))
                .andDo(restDocsHandler.document(
                        queryParameters(
                                parameterWithName("year").description("리그의 연도")
                        ),
                        responseFields(
                                fieldWithPath("[].leagueId").type(JsonFieldType.NUMBER).description("리그의 ID"),
                                fieldWithPath("[].name").type(JsonFieldType.STRING).description("리그의 이름"),
                                fieldWithPath("[].maxRound").type(JsonFieldType.NUMBER).description("리그의 최대 라운드"),
                                fieldWithPath("[].inProgressRound").type(JsonFieldType.NUMBER)
                                        .description("현재 진행 중인 라운드"),
                                fieldWithPath("[].leagueProgress").type(JsonFieldType.STRING).description("현재 대회 진행 상태")
                        )
                ));
    }

    @Test
    void 리그의_모든_리그팀을_라운드별로_조회한다() throws Exception {

        // given
        Long leagueId = 1L;

        List<LeagueTeamResponse> responses = List.of(
                new LeagueTeamResponse(1L, 10L, "경영 야생마", "s3:logoImageUrl1", 3, 0, 0),
                new LeagueTeamResponse(2L, 11L, "서어 뻬데뻬", "s3:logoImageUrl2", 6, 0, 0)
        );

        given(leagueQueryService.findTeamsByLeagueRound(leagueId, 2))
                .willReturn(responses);

        // when
        ResultActions result = mockMvc.perform(get("/leagues/{leagueId}/teams", leagueId)
                .queryParam("round", "2")
                .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        result.andExpect((status().isOk()))
                .andDo(restDocsHandler.document(
                        pathParameters(
                                parameterWithName("leagueId").description("리그의 ID")
                        ),
                        queryParameters(
                                parameterWithName("round").description("라운드의 이름 ex. 4강->4, 결승->2")
                        ),
                        responseFields(
                                fieldWithPath("[].leagueTeamId").type(JsonFieldType.NUMBER).description("리그의 리그팀 ID"),
                                fieldWithPath("[].teamId").type(JsonFieldType.NUMBER).description("해당 리그팀의 팀 ID"),
                                fieldWithPath("[].teamName").type(JsonFieldType.STRING).description("리그에 참여하는 팀의 이름"),
                                fieldWithPath("[].logoImageUrl").type(JsonFieldType.STRING)
                                        .description("리그의 팀 로고 이미지 URL"),
                                fieldWithPath("[].sizeOfTeamPlayers").type(JsonFieldType.NUMBER)
                                        .description("리그팀 선수의 인원수"),
                                fieldWithPath("[].cheerCount").type(JsonFieldType.NUMBER).description("응원 수"),
                                fieldWithPath("[].cheerTalksCount").description("치어톡 수")
                        )
                ));
    }

    @Test
    void 리그_통계를_조회한다() throws Exception {
        // given
        Long leagueId = 1L;
        
        TeamResponse firstWinnerTeam = new TeamResponse(1L, "경영 야생마", "s3:logoImageUrl1", "경영", "#FF0000");
        TeamResponse secondWinnerTeam = new TeamResponse(2L, "서어 뻬데뻬", "s3:logoImageUrl2", "경영", "#0000FF");
        
        LeagueTeamResponse mostCheeredTeam = new LeagueTeamResponse(1L, 1L, "경영 야생마", "s3:logoImageUrl1", 3, 100, 50);
        LeagueTeamResponse mostCheerTalksTeam = new LeagueTeamResponse(2L, 2L, "서어 뻬데뻬", "s3:logoImageUrl2", 4, 80, 120);
        
        LeagueStatisticsResponse response = LeagueStatisticsResponse.builder()
                .leagueStatisticsId(1L)
                .firstWinnerTeam(firstWinnerTeam)
                .secondWinnerTeam(secondWinnerTeam)
                .mostCheeredTeam(mostCheeredTeam)
                .mostCheerTalksTeam(mostCheerTalksTeam)
                .build();
        
        given(leagueQueryService.findLeagueStatistic(leagueId))
                .willReturn(response);

        // when
        ResultActions result = mockMvc.perform(get("/leagues/{leagueId}/statistics", leagueId)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isOk())
                .andDo(restDocsHandler.document(
                        pathParameters(
                                parameterWithName("leagueId").description("리그의 ID")
                        ),
                        responseFields(
                                fieldWithPath("leagueStatisticsId").type(JsonFieldType.NUMBER).description("리그 통계 ID"),
                                fieldWithPath("firstWinnerTeam").type(JsonFieldType.OBJECT).description("1위 팀 정보"),
                                fieldWithPath("firstWinnerTeam.id").type(JsonFieldType.NUMBER).description("1위 팀 ID"),
                                fieldWithPath("firstWinnerTeam.name").type(JsonFieldType.STRING).description("1위 팀 이름"),
                                fieldWithPath("firstWinnerTeam.logoImageUrl").type(JsonFieldType.STRING).description("1위 팀 로고 이미지 URL"),
                                fieldWithPath("firstWinnerTeam.unit").type(JsonFieldType.STRING).description("1위 팀 단위"),
                                fieldWithPath("firstWinnerTeam.teamColor").type(JsonFieldType.STRING).description("1위 팀 컬러"),
                                fieldWithPath("secondWinnerTeam").type(JsonFieldType.OBJECT).description("2위 팀 정보"),
                                fieldWithPath("secondWinnerTeam.id").type(JsonFieldType.NUMBER).description("2위 팀 ID"),
                                fieldWithPath("secondWinnerTeam.name").type(JsonFieldType.STRING).description("2위 팀 이름"),
                                fieldWithPath("secondWinnerTeam.logoImageUrl").type(JsonFieldType.STRING).description("2위 팀 로고 이미지 URL"),
                                fieldWithPath("secondWinnerTeam.unit").type(JsonFieldType.STRING).description("2위 팀 단위"),
                                fieldWithPath("secondWinnerTeam.teamColor").type(JsonFieldType.STRING).description("2위 팀 컬러"),
                                fieldWithPath("mostCheeredTeam").type(JsonFieldType.OBJECT).description("최다 응원 팀 정보"),
                                fieldWithPath("mostCheeredTeam.leagueTeamId").type(JsonFieldType.NUMBER).description("최다 응원 팀의 리그팀 ID"),
                                fieldWithPath("mostCheeredTeam.teamId").type(JsonFieldType.NUMBER).description("최다 응원 팀 ID"),
                                fieldWithPath("mostCheeredTeam.teamName").type(JsonFieldType.STRING).description("최다 응원 팀 이름"),
                                fieldWithPath("mostCheeredTeam.logoImageUrl").type(JsonFieldType.STRING).description("최다 응원 팀 로고 이미지 URL"),
                                fieldWithPath("mostCheeredTeam.sizeOfTeamPlayers").type(JsonFieldType.NUMBER).description("최다 응원 팀 선수 수"),
                                fieldWithPath("mostCheeredTeam.cheerCount").type(JsonFieldType.NUMBER).description("최다 응원 팀 응원 수"),
                                fieldWithPath("mostCheeredTeam.cheerTalksCount").type(JsonFieldType.NUMBER).description("최다 응원 팀 치어톡 수"),
                                fieldWithPath("mostCheerTalksTeam").type(JsonFieldType.OBJECT).description("최다 치어톡 팀 정보"),
                                fieldWithPath("mostCheerTalksTeam.leagueTeamId").type(JsonFieldType.NUMBER).description("최다 치어톡 팀의 리그팀 ID"),
                                fieldWithPath("mostCheerTalksTeam.teamId").type(JsonFieldType.NUMBER).description("최다 치어톡 팀 ID"),
                                fieldWithPath("mostCheerTalksTeam.teamName").type(JsonFieldType.STRING).description("최다 치어톡 팀 이름"),
                                fieldWithPath("mostCheerTalksTeam.logoImageUrl").type(JsonFieldType.STRING).description("최다 치어톡 팀 로고 이미지 URL"),
                                fieldWithPath("mostCheerTalksTeam.sizeOfTeamPlayers").type(JsonFieldType.NUMBER).description("최다 치어톡 팀 선수 수"),
                                fieldWithPath("mostCheerTalksTeam.cheerCount").type(JsonFieldType.NUMBER).description("최다 치어톡 팀 응원 수"),
                                fieldWithPath("mostCheerTalksTeam.cheerTalksCount").type(JsonFieldType.NUMBER).description("최다 치어톡 팀 치어톡 수")
                        )
                ));
    }

    @Test
    void 리그_득점왕을_조회한다() throws Exception {
        // given
        Long leagueId = 1L;
        
        List<LeagueTopScorerResponse> responses = List.of(
                new LeagueTopScorerResponse(1L, "진승희", "202101001", 1, 5),
                new LeagueTopScorerResponse(2L, "이동규", "202101002", 2, 3),
                new LeagueTopScorerResponse(3L, "이현제", "202202001", 3, 2)
        );
        
        given(leagueQueryService.findTop20ScorersByLeagueId(leagueId))
                .willReturn(responses);

        // when
        ResultActions result = mockMvc.perform(get("/leagues/{leagueId}/top-scorers", leagueId)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isOk())
                .andDo(restDocsHandler.document(
                        pathParameters(
                                parameterWithName("leagueId").description("리그의 ID")
                        ),
                        responseFields(
                                fieldWithPath("[].playerId").type(JsonFieldType.NUMBER).description("선수 ID"),
                                fieldWithPath("[].playerName").type(JsonFieldType.STRING).description("선수 이름"),
                                fieldWithPath("[].studentNumber").type(JsonFieldType.STRING).description("선수 학번"),
                                fieldWithPath("[].ranking").type(JsonFieldType.NUMBER).description("득점 순위"),
                                fieldWithPath("[].goalCount").type(JsonFieldType.NUMBER).description("득점 수")
                        )
                ));
    }

    @Test
    void 리그를_하나_조회한다() throws Exception {
        // given
        Long leagueId = 1L;
        given(leagueQueryService.findLeagueDetail(leagueId))
                .willReturn(new LeagueDetailResponse(
                        "삼건물대회",
                        LocalDateTime.of(2024, 3, 25, 0, 0, 0),
                        LocalDateTime.of(2024, 3, 26, 0, 0, 0),
                        16,
                        4,
                        "진행 중",
                        3
                ));

        // when
        ResultActions result = mockMvc.perform(get("/leagues/{leagueId}", leagueId)
                .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        result.andExpect((status().isOk()))
                .andDo(restDocsHandler.document(
                        pathParameters(
                                parameterWithName("leagueId").description("리그의 ID")
                        ),
                        responseFields(
                                fieldWithPath("name").type(JsonFieldType.STRING).description("리그 이름"),
                                fieldWithPath("startAt").type(JsonFieldType.STRING).description("리그 시작 시간"),
                                fieldWithPath("endAt").type(JsonFieldType.STRING).description("리그 종료 시간"),
                                fieldWithPath("inProgressRound").type(JsonFieldType.NUMBER).description("리그의 현재 라운드"),
                                fieldWithPath("maxRound").type(JsonFieldType.NUMBER).description("리그 총 라운드"),
                                fieldWithPath("leagueProgress").type(JsonFieldType.STRING).description("현재 대회 진행 상태"),
                                fieldWithPath("leagueTeamCount").type(JsonFieldType.NUMBER).description("대회에 참여중인 팀의 수")
                        )
                ));
    }

    @Test
    void 리그팀의_모든_선수를_조회한다() throws Exception {
        // given
        Long leagueTeamId = 1L;

        List<PlayerResponse> responses = List.of(
                new PlayerResponse(1L, null, "봄동나물진승희", "202022222", 10, null, null),
                new PlayerResponse(2L, null, "가을전어이동규", "202022221", 7, null, null)
        );

        given(leagueQueryService.findPlayersByLeagueTeam(leagueTeamId))
                .willReturn(responses);

        // when
        ResultActions result = mockMvc.perform(get("/leagues/teams/{leagueTeamId}/players", leagueTeamId)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect((status().isOk()))
                .andDo(restDocsHandler.document(
                        pathParameters(
                                parameterWithName("leagueTeamId").description("리그 팀의 ID")
                        ),
                        responseFields(
                                fieldWithPath("[].playerId").type(JsonFieldType.NUMBER).description("대회 팀 선수 ID"),
                                fieldWithPath("[].name").type(JsonFieldType.STRING).description("대회 팀 선수 이름"),
                                fieldWithPath("[].studentNumber").type(JsonFieldType.STRING).description("대회 팀 선수 학번"),
                                fieldWithPath("[].jerseyNumber").type(JsonFieldType.NUMBER).description("대회 팀 선수 등번호(nullable)")
                                //TODO: 선수의 리그 내 총 골 개수 추가
                        )
                ));
    }

    @Test
    void 매니저가_생성한_모든_리그와_진행중_경기를_조회한다() throws Exception {

        // given
        List<GameTeamResponse> gameTeams = List.of(
                new GameTeamResponse(1L, "경영 야생마", "이미지 이미지", 1, 0),
                new GameTeamResponse(2L, "서어 뼤데뻬", "이미지 이미지", 0, 0)
        );

        LocalDateTime fixedDateTime = LocalDateTime.of(2024, 9, 11, 12, 0, 0);

        // 진행 중인 경기만
        List<GameDetailResponse> inProgressGames = List.of(
                new GameDetailResponse(1L, "PLAYING", fixedDateTime, false, gameTeams)
        );

        List<LeagueResponseWithInProgressGames> responses = List.of(
                new LeagueResponseWithInProgressGames(1L, "삼건물 대회", "진행 중", inProgressGames));

        Cookie cookie = new Cookie(COOKIE_NAME, "temp-cookie");

        given(leagueQueryService.findLeaguesByManager(any(Member.class)))
                .willReturn(responses);

        // when
        ResultActions result = mockMvc.perform(get("/leagues/manager")
                .cookie(cookie)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isOk())
                .andDo(restDocsHandler.document(
                        requestCookies(
                                cookieWithName(COOKIE_NAME).description("로그인을 통해 얻은 토큰")
                        ),
                        responseFields(
                                fieldWithPath("[].id").type(JsonFieldType.NUMBER).description("리그의 ID"),
                                fieldWithPath("[].name").type(JsonFieldType.STRING).description("리그의 이름"),
                                fieldWithPath("[].state").type(JsonFieldType.STRING)
                                        .description("리그의 진행 상태 ex. 진행 중, 종료"),
                                fieldWithPath("[].inProgressGames").type(JsonFieldType.ARRAY).description("진행 중인 게임들"),
                                fieldWithPath("[].inProgressGames[].id").type(JsonFieldType.NUMBER)
                                        .description("진행 중인 게임의 ID"),
                                fieldWithPath("[].inProgressGames[].state").type(JsonFieldType.STRING)
                                        .description("진행 중인 게임의 상태"),
                                fieldWithPath("[].inProgressGames[].startTime").type(JsonFieldType.STRING)
                                        .description("진행 중인 게임의 시작 시간"),
                                fieldWithPath("[].inProgressGames[].isPkTaken").type(JsonFieldType.BOOLEAN)
                                        .description("승부차기 진출 여부"),
                                fieldWithPath("[].inProgressGames[].gameTeams").type(JsonFieldType.ARRAY)
                                        .description("게임에 속한 팀들"),
                                fieldWithPath("[].inProgressGames[].gameTeams[].gameTeamId").type(JsonFieldType.NUMBER)
                                        .description("게임 팀의 ID"),
                                fieldWithPath("[].inProgressGames[].gameTeams[].gameTeamName").type(
                                        JsonFieldType.STRING).description("게임 팀의 이름"),
                                fieldWithPath("[].inProgressGames[].gameTeams[].logoImageUrl").type(
                                        JsonFieldType.STRING).description("게임 팀의 로고 이미지 URL"),
                                fieldWithPath("[].inProgressGames[].gameTeams[].score").type(JsonFieldType.NUMBER)
                                        .description("게임 팀의 점수"),
                                fieldWithPath("[].inProgressGames[].gameTeams[].pkScore").type(JsonFieldType.NUMBER)
                                        .description("게임 팀의 승부차기 점수")
                        )
                ));
    }

    @Test
    void 매니저가_생성한_모든_리그를_조회한다() throws Exception {

        // given
        LocalDateTime fixedDateTime = LocalDateTime.of(2024, 9, 11, 12, 0, 0);
        List<LeagueResponseToManage> responses = List.of(
                new LeagueResponseToManage(1L, "삼건물 대회", "진행 중", 2, 16, fixedDateTime,
                        fixedDateTime),
                new LeagueResponseToManage(2L, "탁구 대회", "시작 전", 2, 16, fixedDateTime,
                        fixedDateTime));

        Cookie cookie = new Cookie(COOKIE_NAME, "temp-cookie");

        given(leagueQueryService.findLeaguesByManagerToManage(any(Member.class)))
                .willReturn(responses);

        // when
        ResultActions result = mockMvc.perform(get("/leagues/manager/manage")
                .cookie(cookie)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isOk())
                .andDo(restDocsHandler.document(
                        requestCookies(
                                cookieWithName(COOKIE_NAME).description("로그인을 통해 얻은 토큰")
                        ),
                        responseFields(
                                fieldWithPath("[].id").type(JsonFieldType.NUMBER).description("리그의 ID"),
                                fieldWithPath("[].name").type(JsonFieldType.STRING).description("리그의 이름"),
                                fieldWithPath("[].leagueProgress").type(JsonFieldType.STRING)
                                        .description("리그의 진행 상태 ex. 진행 중, 종료"),
                                fieldWithPath("[].sizeOfLeagueTeams").type(JsonFieldType.NUMBER).description("리그 팀의 수"),
                                fieldWithPath("[].maxRound").type(JsonFieldType.NUMBER).description("리그의 최대 라운드"),
                                fieldWithPath("[].startAt").type(JsonFieldType.STRING).description("리그 시작 날짜"),
                                fieldWithPath("[].endAt").type(JsonFieldType.STRING).description("리그 종료 날짜")
                        )
                ));
    }

    @Test
    void 리그의_정보와_리그에_속한_모든_경기를_조회한다() throws Exception {
        // given
        Long leagueId = 1L;

        List<LeagueResponseWithGames.GameDetail.GameTeam> playingGameTeams = List.of(
                new GameTeam(1L, "게임팀1", "이미지url", 1, 0),
                new GameTeam(2L, "게임팀2", "이미지url", 1, 0)
        );
        List<LeagueResponseWithGames.GameDetail.GameTeam> scheduledGameTeams = List.of(
                new GameTeam(3L, "게임팀3", "이미지url", 1, 0),
                new GameTeam(4L, "게임팀4", "이미지url", 1, 0)
        );
        List<LeagueResponseWithGames.GameDetail.GameTeam> finishedGameTeams = List.of(
                new GameTeam(5L, "게임팀5", "이미지url", 1, 0),
                new GameTeam(6L, "게임팀6", "이미지url", 1, 0)
        );
        List<LeagueResponseWithGames.GameDetail> playingGames = List.of(
                new GameDetail(1L, "PLAYING", LocalDateTime.of(2024, 8, 11, 13, 30), false,
                        playingGameTeams)
        );
        List<LeagueResponseWithGames.GameDetail> finishedGames = List.of(
                new GameDetail(2L, "FINISHED", LocalDateTime.of(2024, 8, 11, 13, 30), false,
                        finishedGameTeams)
        );
        List<LeagueResponseWithGames.GameDetail> scheduledGames = List.of(
                new GameDetail(3L, "SCHEDULED", LocalDateTime.of(2024, 8, 11, 13, 30), false,
                        scheduledGameTeams)
        );
        LeagueResponseWithGames response = new LeagueResponseWithGames(
                1L, "첫번째 리그", 6, 16, LocalDateTime.of(2024, 8, 11, 13, 30), LocalDateTime.of(2024, 8, 30, 13, 30),
                playingGames, scheduledGames, finishedGames
        );

        given(leagueQueryService.findLeagueAndGames(leagueId))
                .willReturn(response);

        // when
        ResultActions result = mockMvc.perform(get("/leagues/{leagueId}/games", leagueId)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect((status().isOk()))
                .andDo(restDocsHandler.document(
                        pathParameters(
                                parameterWithName("leagueId").description("리그의 Id")
                        ),
                        responseFields(
                                fieldWithPath("id").type(JsonFieldType.NUMBER).description("리그 ID"),
                                fieldWithPath("name").type(JsonFieldType.STRING).description("리그 이름"),
                                fieldWithPath("sizeOfLeagueTeams").type(JsonFieldType.NUMBER).description("리그 팀 수"),
                                fieldWithPath("maxRound").type(JsonFieldType.NUMBER).description("리그 최대 라운드"),
                                fieldWithPath("startAt").type(JsonFieldType.STRING).description("리그 시작 시간"),
                                fieldWithPath("endAt").type(JsonFieldType.STRING).description("리그 종료 시간"),
                                fieldWithPath("playingGames").type(JsonFieldType.ARRAY).description("진행 중인 경기 목록"),
                                fieldWithPath("playingGames[].id").type(JsonFieldType.NUMBER).description("경기 ID"),
                                fieldWithPath("playingGames[].state").type(JsonFieldType.STRING).description("경기 상태"),
                                fieldWithPath("playingGames[].startTime").type(JsonFieldType.STRING)
                                        .description("경기 시작 시간"),
                                fieldWithPath("playingGames[].isPkTaken").type(JsonFieldType.BOOLEAN)
                                        .description("승부차기 진출 여부"),
                                fieldWithPath("playingGames[].gameTeams").type(JsonFieldType.ARRAY)
                                        .description("경기 팀 목록"),
                                fieldWithPath("playingGames[].gameTeams[].gameTeamId").type(JsonFieldType.NUMBER)
                                        .description("경기 팀 ID"),
                                fieldWithPath("playingGames[].gameTeams[].gameTeamName").type(JsonFieldType.STRING)
                                        .description("경기 팀 이름"),
                                fieldWithPath("playingGames[].gameTeams[].logoImageUrl").type(JsonFieldType.STRING)
                                        .description("경기 팀 로고 이미지 URL"),
                                fieldWithPath("playingGames[].gameTeams[].score").type(JsonFieldType.NUMBER)
                                        .description("경기 팀 점수"),
                                fieldWithPath("playingGames[].gameTeams[].pkScore").type(JsonFieldType.NUMBER)
                                        .description("경기 팀 승부차기 점수"),

                                fieldWithPath("scheduledGames").type(JsonFieldType.ARRAY).description("예정된 경기 목록"),
                                fieldWithPath("scheduledGames[].id").type(JsonFieldType.NUMBER).description("경기 ID"),
                                fieldWithPath("scheduledGames[].state").type(JsonFieldType.STRING).description("경기 상태"),
                                fieldWithPath("scheduledGames[].startTime").type(JsonFieldType.STRING)
                                        .description("경기 시작 시간"),
                                fieldWithPath("scheduledGames[].isPkTaken").type(JsonFieldType.BOOLEAN)
                                        .description("승부차기 진출 여부"),
                                fieldWithPath("scheduledGames[].gameTeams").type(JsonFieldType.ARRAY)
                                        .description("경기 팀 목록"),
                                fieldWithPath("scheduledGames[].gameTeams[].gameTeamId").type(JsonFieldType.NUMBER)
                                        .description("경기 팀 ID"),
                                fieldWithPath("scheduledGames[].gameTeams[].gameTeamName").type(JsonFieldType.STRING)
                                        .description("경기 팀 이름"),
                                fieldWithPath("scheduledGames[].gameTeams[].logoImageUrl").type(JsonFieldType.STRING)
                                        .description("경기 팀 로고 이미지 URL"),
                                fieldWithPath("scheduledGames[].gameTeams[].score").type(JsonFieldType.NUMBER)
                                        .description("경기 팀 점수"),
                                fieldWithPath("scheduledGames[].gameTeams[].pkScore").type(JsonFieldType.NUMBER)
                                        .description("경기 팀 승부차기 점수"),

                                fieldWithPath("finishedGames").type(JsonFieldType.ARRAY).description("완료된 경기 목록"),
                                fieldWithPath("finishedGames[].id").type(JsonFieldType.NUMBER).description("경기 ID"),
                                fieldWithPath("finishedGames[].state").type(JsonFieldType.STRING).description("경기 상태"),
                                fieldWithPath("finishedGames[].startTime").type(JsonFieldType.STRING)
                                        .description("경기 시작 시간"),
                                fieldWithPath("finishedGames[].isPkTaken").type(JsonFieldType.BOOLEAN)
                                        .description("승부차기 진출 여부"),
                                fieldWithPath("finishedGames[].gameTeams").type(JsonFieldType.ARRAY)
                                        .description("경기 팀 목록"),
                                fieldWithPath("finishedGames[].gameTeams[].gameTeamId").type(JsonFieldType.NUMBER)
                                        .description("경기 팀 ID"),
                                fieldWithPath("finishedGames[].gameTeams[].gameTeamName").type(JsonFieldType.STRING)
                                        .description("경기 팀 이름"),
                                fieldWithPath("finishedGames[].gameTeams[].logoImageUrl").type(JsonFieldType.STRING)
                                        .description("경기 팀 로고 이미지 URL"),
                                fieldWithPath("finishedGames[].gameTeams[].score").type(JsonFieldType.NUMBER)
                                        .description("경기 팀 점수"),
                                fieldWithPath("finishedGames[].gameTeams[].pkScore").type(JsonFieldType.NUMBER)
                                        .description("경기 팀 승부차기 점수")
                        )
                ));
    }

}

