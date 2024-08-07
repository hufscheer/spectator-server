package com.sports.server.query.presentation;

import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sports.server.query.dto.response.LeagueDetailResponse;
import com.sports.server.query.dto.response.LeagueResponse;
import com.sports.server.query.dto.response.LeagueSportResponse;
import com.sports.server.query.dto.response.LeagueTeamPlayerResponse;
import com.sports.server.query.dto.response.LeagueTeamResponse;
import com.sports.server.support.DocumentationTest;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.ResultActions;

public class LeagueQueryControllerTest extends DocumentationTest {

    @Test
    void 리그_전체를_조회한다() throws Exception {

        // given
        List<LeagueResponse> responses = List.of(
                new LeagueResponse(1L, "리그 첫번째", "16강", "4강", "종료"),
                new LeagueResponse(2L, "리그 두번째", "32강", "32강", "진행 중")
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
                                fieldWithPath("[].maxRound").type(JsonFieldType.STRING).description("리그의 최대 라운드"),
                                fieldWithPath("[].inProgressRound").type(JsonFieldType.STRING)
                                        .description("현재 진행 중인 라운드"),
                                fieldWithPath("[].leagueProgress").type(JsonFieldType.STRING).description("현재 대회 진행 상태")
                        )
                ));
    }

    @Test
    void 리그의_해당하는_스포츠_전체를_조회한다() throws Exception {

        // given
        Long leagueId = 1L;

        List<LeagueSportResponse> responses = List.of(
                new LeagueSportResponse(1L, "축구"),
                new LeagueSportResponse(2L, "농구")
        );

        given(leagueQueryService.findSportsByLeague(leagueId))
                .willReturn(responses);

        // when
        ResultActions result = mockMvc.perform(get("/leagues/{leagueId}/sports", leagueId)
                .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        result.andExpect((status().isOk()))
                .andDo(restDocsHandler.document(
                        pathParameters(
                                parameterWithName("leagueId").description("리그의 ID")
                        ),
                        responseFields(
                                fieldWithPath("[].sportId").type(JsonFieldType.NUMBER).description("스포츠의 ID"),
                                fieldWithPath("[].name").type(JsonFieldType.STRING).description("스포츠의 이름")
                        )
                ));
    }

    @Test
    void 리그의_모든_리그팀을_라운드별로_조회한다() throws Exception {

        // given
        Long leagueId = 1L;

        List<LeagueTeamResponse> responses = List.of(
                new LeagueTeamResponse(1L, "경영 야생마", "s3:logoImageUrl1", 3),
                new LeagueTeamResponse(2L, "서어 뻬데뻬", "s3:logoImageUrl2", 6)
        );

        given(leagueQueryService.findTeamsByLeagueRound(leagueId, "결승"))
                .willReturn(responses);

        // when
        ResultActions result = mockMvc.perform(get("/leagues/{leagueId}/teams", leagueId)
                .queryParam("descriptionOfRound", "결승")
                .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        result.andExpect((status().isOk()))
                .andDo(restDocsHandler.document(
                        pathParameters(
                                parameterWithName("leagueId").description("리그의 ID")
                        ),
                        queryParameters(
                                parameterWithName("descriptionOfRound").description("라운드의 이름 ex. 4강, 결승")
                        ),
                        responseFields(
                                fieldWithPath("[].leagueTeamId").type(JsonFieldType.NUMBER).description("리그의 팀 ID"),
                                fieldWithPath("[].teamName").type(JsonFieldType.STRING).description("리그에 참여하는 팀의 이름"),
                                fieldWithPath("[].logoImageUrl").type(JsonFieldType.STRING)
                                        .description("리그의 팀 로고 이미지 URL®"),
                                fieldWithPath("[].sizeOfLeagueTeamPlayers").type(JsonFieldType.NUMBER)
                                        .description("리그팀 선수의 인원수")
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
                        "16강",
                        "4강",
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
                                fieldWithPath("inProgressRound").type(JsonFieldType.STRING).description("리그의 현재 라운드"),
                                fieldWithPath("maxRound").type(JsonFieldType.STRING).description("리그 총 라운드"),
                                fieldWithPath("leagueProgress").type(JsonFieldType.STRING).description("현재 대회 진행 상태"),
                                fieldWithPath("leagueTeamCount").type(JsonFieldType.NUMBER).description("대회에 참여중인 팀의 수")
                        )
                ));
    }

    @Test
    void 리그팀의_모든_선수를_조회한다() throws Exception {
        // given
        Long leagueTeamId = 1L;

        List<LeagueTeamPlayerResponse> responses = List.of(
                new LeagueTeamPlayerResponse(1L, "봄동나물진승희", "설명설명설명", 0),
                new LeagueTeamPlayerResponse(2L, "가을전어이동규", "설명설명설명", 2)
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
                                fieldWithPath("[].id").type(JsonFieldType.NUMBER).description("대회 팀 선수 ID"),
                                fieldWithPath("[].name").type(JsonFieldType.STRING).description("대회 팀 선수 이름"),
                                fieldWithPath("[].description").type(JsonFieldType.STRING).description("대회 팀 선수 설명"),
                                fieldWithPath("[].number").type(JsonFieldType.NUMBER).description("대회 팀 선수 점수")
                        )
                ));
    }
}
