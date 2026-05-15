package com.sports.server.query.presentation;

import com.sports.server.common.dto.CursorPageResponse;
import com.sports.server.query.dto.response.PlayerResponse;
import com.sports.server.query.dto.response.TeamResponse;
import com.sports.server.support.DocumentationTest;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.cookies.CookieDocumentation.cookieWithName;
import static org.springframework.restdocs.cookies.CookieDocumentation.requestCookies;
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
                new TeamResponse(1L, "정치외교학과 PSD", "s3:logoImageUrl1", "사회과학대학", "#F7CAC9", "SOCCER"),
                new TeamResponse(2L, "국제통상학과 무역풍", "s3:logoImageUrl1", "사회과학대학", "#92A8D1", "SOCCER")
        );

        List<TeamResponse> teamResponses2 = List.of(
                new TeamResponse(1L, "정치외교학과 PSD", "s3:logoImageUrl1", "사회과학대학", "#F7CAC9", "SOCCER")
        );

        CursorPageResponse<PlayerResponse> response = new CursorPageResponse<>(List.of(
                new PlayerResponse(1L, null, "선수1", "202500001", null, 0, teamResponses1),
                new PlayerResponse(2L, null, "선수2", "202500002", null, 5, teamResponses2),
                new PlayerResponse(3L, null, "선수3", "202500003", null, 10, Collections.emptyList())
        ), null, false);

        given(playerQueryService.getAllPlayers(any(), any()))
                .willReturn(response);

        // when
        ResultActions result = mockMvc.perform(get("/players")
                .queryParam("cursor", "10")
                .queryParam("size", "10")
                .queryParam("name", "선수")
                .queryParam("studentNumber", "202500001")
                .cookie(new Cookie(COOKIE_NAME, "temp-cookie"))
                .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        result.andExpect((status().isOk()))
                .andDo(restDocsHandler.document(
                        queryParameters(
                                parameterWithName("cursor").description("마지막으로 조회한 선수의 ID (선택, 미입력 시 최신부터)").optional(),
                                parameterWithName("size").description("조회할 선수 수 (선택, default 10)").optional(),
                                parameterWithName("name").description("선수 이름 부분 일치 검색 (선택, 대소문자 무시)").optional(),
                                parameterWithName("studentNumber").description("학번 정확 일치 검색 (선택)").optional()
                        ),
                        requestCookies(
                                cookieWithName(COOKIE_NAME).description("로그인을 통해 얻은 토큰")
                        ),
                        responseFields(
                                fieldWithPath("content").type(JsonFieldType.ARRAY).description("선수 목록"),
                                fieldWithPath("content[].playerId").type(JsonFieldType.NUMBER).description("선수의 ID"),
                                fieldWithPath("content[].name").type(JsonFieldType.STRING).description("선수의 이름"),
                                fieldWithPath("content[].studentNumber").type(JsonFieldType.STRING).description("선수의 학번"),
                                fieldWithPath("content[].totalGoalCount").type(JsonFieldType.NUMBER).description("선수의 전체 골 개수"),
                                fieldWithPath("content[].teams").type(JsonFieldType.ARRAY).description("선수의 모든 소속팀 목록"),
                                fieldWithPath("content[].teams[].id").type(JsonFieldType.NUMBER).description("소속팀의 ID"),
                                fieldWithPath("content[].teams[].name").type(JsonFieldType.STRING).description("소속팀의 이름"),
                                fieldWithPath("content[].teams[].logoImageUrl").type(JsonFieldType.STRING).description("소속팀의 로고 이미지 url"),
                                fieldWithPath("content[].teams[].unit").type(JsonFieldType.STRING).description("소속팀의 소속 단위"),
                                fieldWithPath("content[].teams[].teamColor").type(JsonFieldType.STRING).description("소속팀의 대표 색상"),
                                fieldWithPath("content[].teams[].sportType").type(JsonFieldType.STRING).description("소속팀의 종목"),
                                fieldWithPath("nextCursor").type(JsonFieldType.NUMBER).optional().description("다음 페이지 커서"),
                                fieldWithPath("hasNext").type(JsonFieldType.BOOLEAN).description("다음 페이지 존재 여부")
                        )
                ));
    }

    @Test
    void 선수를_상세_조회한다() throws Exception {
        // given
        Long playerId = 1L;
        List<TeamResponse> teamResponses = List.of(
                new TeamResponse(1L, "정치외교학과 PSD", "s3:logoImageUrl1", "사회과학대학", "#F7CAC9", "SOCCER"),
                new TeamResponse(2L, "국제통상학과 무역풍", "s3:logoImageUrl1", "사회과학대학", "#92A8D1", "SOCCER")
        );
        PlayerResponse response = new PlayerResponse(playerId, null, "선수1", "202500001", null,  0, teamResponses);
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
                                fieldWithPath("teams[].teamColor").type(JsonFieldType.STRING).description("소속팀의 대표 색상"),
                                fieldWithPath("teams[].sportType").type(JsonFieldType.STRING).description("소속팀의 종목")
                        )
                ));
    }

}
