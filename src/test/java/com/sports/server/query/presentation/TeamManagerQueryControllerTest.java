package com.sports.server.query.presentation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.cookies.CookieDocumentation.cookieWithName;
import static org.springframework.restdocs.cookies.CookieDocumentation.requestCookies;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sports.server.command.league.domain.SportType;
import com.sports.server.command.member.domain.Member;
import com.sports.server.query.dto.response.TeamResponse;
import com.sports.server.query.dto.response.UnitResponse;
import com.sports.server.support.DocumentationTest;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

public class TeamManagerQueryControllerTest extends DocumentationTest {

    @Test
    void 매니저의_소속_기준으로_단과대별_팀_유무를_조회한다() throws Exception {
        // given
        List<UnitResponse> response = List.of(
                new UnitResponse(1L, "영어대학", true),
                new UnitResponse(2L, "서양어대학", false),
                new UnitResponse(3L, "사회과학대학", true),
                new UnitResponse(4L, "경영대학", true),
                new UnitResponse(5L, "기타", false)
        );

        Cookie cookie = new Cookie(COOKIE_NAME, "temp-cookie");

        given(teamQueryService.getUnitsWithTeams(any(SportType.class), any(Member.class))).willReturn(response);

        // when
        ResultActions result = mockMvc.perform(get("/manager/teams/units")
                .param("sportType", "SOCCER")
                .cookie(cookie)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isOk())
                .andDo(restDocsHandler.document(
                        requestCookies(
                                cookieWithName(COOKIE_NAME).description("로그인을 통해 얻은 토큰")
                        ),
                        queryParameters(
                                parameterWithName("sportType").description("종목 필터 (SOCCER, BASKETBALL)").optional()
                        ),
                        responseFields(
                                fieldWithPath("[].id").type(JsonFieldType.NUMBER).description("단과대 ID"),
                                fieldWithPath("[].unitName").type(JsonFieldType.STRING).description("단과대 이름"),
                                fieldWithPath("[].hasTeam").type(JsonFieldType.BOOLEAN).description("해당 단과대에 팀 존재 여부")
                        )
                ));
    }

    @Test
    void 매니저의_소속_기준으로_모든_팀을_조회한다() throws Exception {
        // given
        List<TeamResponse> response = List.of(
                new TeamResponse(1L, "정치외교학과 PSD", "s3:logoImageUrl1", "사회과학대학", "#F7CAC9", "SOCCER"),
                new TeamResponse(2L, "국제통상학과 무역풍", "s3:logoImageUrl2", "사회과학대학", "#92A8D1", "SOCCER"),
                new TeamResponse(3L, "영어영문학과", "s3:logoImageUrl2", "영어대학", "#92A8D1", "SOCCER")
        );

        Cookie cookie = new Cookie(COOKIE_NAME, "temp-cookie");

        given(teamQueryService.getAllTeamsByUnits(any(), any(), any(Member.class))).willReturn(response);

        // when
        ResultActions result = mockMvc.perform(get("/manager/teams")
                .param("units", "사회과학대학", "영어대학")
                .param("sportType", "SOCCER")
                .cookie(cookie)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isOk())
                .andDo(restDocsHandler.document(
                        requestCookies(
                                cookieWithName(COOKIE_NAME).description("로그인을 통해 얻은 토큰")
                        ),
                        queryParameters(
                                parameterWithName("units").description("필터링할 소속 리스트").optional(),
                                parameterWithName("sportType").description("종목 필터 (SOCCER, BASKETBALL)").optional()
                        ),
                        responseFields(
                                fieldWithPath("[].id").type(JsonFieldType.NUMBER).description("팀의 ID"),
                                fieldWithPath("[].name").type(JsonFieldType.STRING).description("팀의 이름"),
                                fieldWithPath("[].logoImageUrl").type(JsonFieldType.STRING).description("팀의 로고 이미지 URL"),
                                fieldWithPath("[].unit").type(JsonFieldType.STRING).description("팀의 소속 단위"),
                                fieldWithPath("[].teamColor").type(JsonFieldType.STRING).description("팀의 대표 색상"),
                                fieldWithPath("[].sportType").type(JsonFieldType.STRING).description("팀의 종목 (SOCCER, BASKETBALL)")
                        )
                ));
    }
}
