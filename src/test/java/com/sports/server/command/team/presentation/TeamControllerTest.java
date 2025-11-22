package com.sports.server.command.team.presentation;

import com.sports.server.command.team.domain.Unit;
import com.sports.server.command.team.dto.TeamRequest;
import com.sports.server.support.DocumentationTest;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.springframework.restdocs.cookies.CookieDocumentation.cookieWithName;
import static org.springframework.restdocs.cookies.CookieDocumentation.requestCookies;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class TeamControllerTest extends DocumentationTest {

    @Test
    void 팀을_생성한다() throws Exception {
        // given
        List<TeamRequest.TeamPlayerRegister> teamPlayersRequest = List.of(
                new TeamRequest.TeamPlayerRegister(1L, 1),
                new TeamRequest.TeamPlayerRegister(2L, 7),
                new TeamRequest.TeamPlayerRegister(3L, 10)
        );

        TeamRequest.Register request = new TeamRequest.Register(
                "정치외교학과 PSD",
                "logo-image-url",
                "사회과학대학",
                "team-color",
                teamPlayersRequest
        );

        Mockito.doNothing().when(teamService).register(Mockito.any(TeamRequest.Register.class));

        // when
        ResultActions result = mockMvc.perform(post("/teams", request)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .cookie(new Cookie(COOKIE_NAME, "temp-cookie")));

        // then
        result.andExpect(status().isCreated())
                .andDo(restDocsHandler.document(
                                requestFields(
                                        fieldWithPath("name").type(JsonFieldType.STRING).description("팀 이름"),
                                        fieldWithPath("logoImageUrl").type(JsonFieldType.STRING).description("팀의 로고 이미지 url"),
                                        fieldWithPath("unit").type(JsonFieldType.STRING).description("팀의 소속 (영어대학, 서양어대학, 아시아언어문화대학, 중국학대학," +
                                                " 일본어대학, 사회과학대학, 상경대학, 경영대학, 사범대학, AI융합대학, 국제학부, LD학부, LT학부, KFL학부, 자유전공학부, 기타)"),
                                        fieldWithPath("teamColor").type(JsonFieldType.STRING).description("팀의 대표 색의 hexCode"),
                                        fieldWithPath("teamPlayers").type(JsonFieldType.ARRAY).description("팀에 추가할 선수들 목록 (없다면 빈 리스트)"),
                                        fieldWithPath("teamPlayers[].playerId").type(JsonFieldType.NUMBER).description("추가할 선수의 Id"),
                                        fieldWithPath("teamPlayers[].jerseyNumber").type(JsonFieldType.NUMBER).description("추가할 선수의 등번호 (nullable)")
                                        ),
                                requestCookies(
                                        cookieWithName(COOKIE_NAME).description("로그인을 통해 얻은 토큰")
                                )
                        )
                );
    }

    @Test
    void 팀을_삭제한다() throws Exception {
        // given
        Long teamId = 1L;
        Cookie cookie = new Cookie(COOKIE_NAME, "temp-cookie");

        doNothing().when(teamService).delete(anyLong());

        // when
        ResultActions result = mockMvc.perform(delete("/teams/{teamId}", teamId)
                .contentType(MediaType.APPLICATION_JSON)
                .cookie(cookie)
        );

        // then
        result.andExpect(status().isNoContent())
                .andDo(restDocsHandler.document(
                                pathParameters(
                                        parameterWithName("teamId").description("팀의 ID")),
                                requestCookies(
                                        cookieWithName(COOKIE_NAME).description("로그인을 통해 얻은 토큰")
                                )
                        )
                );
    }

    @Test
    void 팀_정보를_수정한다() throws Exception {
        // given
        Long teamId = 1L;
        TeamRequest.Update request = new TeamRequest.Update(
                "국제통상학과 무역풍",
                "logo-image-url",
                "경영대학",
                "team-color"
        );

        doNothing().when(teamService).update(any(TeamRequest.Update.class), anyLong());

        // when
        ResultActions result = mockMvc.perform(patch("/teams/{teamId}", teamId)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(request))
                .cookie(new Cookie(COOKIE_NAME, "temp-cookie")));

        // then
        result.andExpect(status().isOk())
                .andDo(restDocsHandler.document(
                            pathParameters(
                                    parameterWithName("teamId").description("팀의 ID")
                            ),
                                requestFields(
                                        fieldWithPath("name").type(JsonFieldType.STRING).description("변경할 팀 이름"),
                                        fieldWithPath("logoImageUrl").type(JsonFieldType.STRING).description("변경할 팀의 로고 이미지 url"),
                                        fieldWithPath("unit").type(JsonFieldType.STRING).description("변경할 팀의 소속"),
                                        fieldWithPath("teamColor").type(JsonFieldType.STRING).description("팀의 대표 색의 hexCode")
                                ),
                                requestCookies(
                                        cookieWithName(COOKIE_NAME).description("로그인을 통해 얻은 토큰")
                                )
                        )
                );
    }

    @Test
    void 팀에_선수들을_추가한다() throws Exception {
        // given
        Long teamId = 1L;

        List<TeamRequest.TeamPlayerRegister> request = List.of(
                new TeamRequest.TeamPlayerRegister(1L, 10),
                new TeamRequest.TeamPlayerRegister(2L, 7)
        );

        doNothing().when(teamService).addPlayersToTeam(anyLong(), any());

        // when
        ResultActions result = mockMvc.perform(post("/teams/{teamId}/players", teamId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .cookie(new Cookie(COOKIE_NAME, "temp-cookie")));

        // then
        result.andExpect(status().isOk())
                .andDo(restDocsHandler.document(
                        pathParameters(
                                parameterWithName("teamId").description("선수를 추가할 팀의 ID")
                        ),
                        requestFields(
                                fieldWithPath("[]").type(JsonFieldType.ARRAY).description("추가할 선수 정보 목록"),
                                fieldWithPath("[].playerId").type(JsonFieldType.NUMBER).description("추가할 선수의 ID"),
                                fieldWithPath("[].jerseyNumber").type(JsonFieldType.NUMBER).description("추가할 선수의 등번호")
                        ),
                        requestCookies(
                                cookieWithName(COOKIE_NAME).description("로그인을 통해 얻은 토큰")
                        )
                ));
    }

    @Test
    void 팀에서_선수를_제거한다() throws Exception {
        // given
        Long teamPlayerId = 1L;

        doNothing().when(teamService).deleteTeamPlayer(anyLong());

        // when
        ResultActions result = mockMvc.perform(delete("/team-players/{teamPlayerId}", teamPlayerId)
                .cookie(new Cookie(COOKIE_NAME, "temp-cookie")));

        // then
        result.andExpect(status().isNoContent())
                .andDo(restDocsHandler.document(
                        pathParameters(
                                parameterWithName("teamPlayerId").description("제거할 팀선수 ID")
                        ),
                        requestCookies(
                                cookieWithName(COOKIE_NAME).description("로그인을 통해 얻은 토큰")
                        )
                ));
    }
}
