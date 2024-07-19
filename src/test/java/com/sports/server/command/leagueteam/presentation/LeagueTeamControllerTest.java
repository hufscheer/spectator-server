package com.sports.server.command.leagueteam.presentation;


import static org.springframework.restdocs.cookies.CookieDocumentation.cookieWithName;
import static org.springframework.restdocs.cookies.CookieDocumentation.requestCookies;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.put;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sports.server.command.leagueteam.dto.LeagueTeamRequest;
import com.sports.server.support.DocumentationTest;
import jakarta.servlet.http.Cookie;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.ResultActions;

public class LeagueTeamControllerTest extends DocumentationTest {

    @Test
    void 리그팀을_등록한다() throws Exception {

        // given
        Long leagueId = 1L;
        List<LeagueTeamRequest.LeagueTeamPlayerRequest> playerRegisterRequests = List.of(
                new LeagueTeamRequest.LeagueTeamPlayerRequest("name-a", 1),
                new LeagueTeamRequest.LeagueTeamPlayerRequest("name-b", 2));
        LeagueTeamRequest.Register request = new LeagueTeamRequest.Register(
                "name", "logo-image-url", playerRegisterRequests);
        Cookie cookie = new Cookie(COOKIE_NAME, "temp-cookie");

        Mockito.doNothing().when(leagueTeamService).register(Mockito.anyLong(), Mockito.any(), Mockito.any());

        // when
        ResultActions result = mockMvc.perform(post("/leagues/{leagueId}/teams", leagueId, request)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .cookie(cookie)
        );

        // then
        result.andExpect(status().isOk())
                .andDo(restDocsHandler.document(
                        pathParameters(
                                parameterWithName("leagueId").description("리그의 ID")
                        ),
                        requestFields(
                                fieldWithPath("name").type(JsonFieldType.STRING).description("리그팀의 이름"),
                                fieldWithPath("logoImageUrl").type(JsonFieldType.STRING).description("팀 로고 이미지 url"),
                                fieldWithPath("players").type(JsonFieldType.ARRAY).description("리그팀 선수 목록"),
                                fieldWithPath("players[].name").type(JsonFieldType.STRING).description("선수의 이름"),
                                fieldWithPath("players[].number").type(JsonFieldType.NUMBER).description("선수의 번호")
                        ),
                        requestCookies(
                                cookieWithName(COOKIE_NAME).description("로그인을 통해 얻은 토큰")
                        )
                ));
    }

    @Test
    void 리그팀을_수정한다() throws Exception {

        // given
        Long leagueId = 1L;
        Long teamId = 3L;
        List<LeagueTeamRequest.LeagueTeamPlayerRequest> playerRegisterRequests = List.of(
                new LeagueTeamRequest.LeagueTeamPlayerRequest("name-a", 1),
                new LeagueTeamRequest.LeagueTeamPlayerRequest("name-b", 2));
        LeagueTeamRequest.Update request = new LeagueTeamRequest.Update(
                "name", "logo-image-url", playerRegisterRequests, List.of(1L, 2L));
        Cookie cookie = new Cookie(COOKIE_NAME, "temp-cookie");

        Mockito.doNothing().when(leagueTeamService)
                .update(Mockito.anyLong(), Mockito.any(), Mockito.any(), Mockito.anyLong());

        // when
        ResultActions result = mockMvc.perform(put("/leagues/{leagueId}/teams/{teamId}", leagueId, teamId, request)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .cookie(cookie)
        );

        // then
        result.andExpect(status().isOk())
                .andDo(restDocsHandler.document(
                        pathParameters(
                                parameterWithName("leagueId").description("리그의 ID"),
                                parameterWithName("teamId").description("리그팀의 ID")
                        ),
                        requestFields(
                                fieldWithPath("name").type(JsonFieldType.STRING).description("리그팀의 이름"),
                                fieldWithPath("logoImageUrl").type(JsonFieldType.STRING).description("팀 로고 이미지 url"),
                                fieldWithPath("addPlayers").type(JsonFieldType.ARRAY).description("리그팀 선수 목록"),
                                fieldWithPath("addPlayers[].name").type(JsonFieldType.STRING).description("선수의 이름"),
                                fieldWithPath("addPlayers[].number").type(JsonFieldType.NUMBER).description("선수의 번호"),
                                fieldWithPath("deletedPlayerIds").type(JsonFieldType.ARRAY)
                                        .description("삭제할 리그팀 선수의 ID")

                        ),
                        requestCookies(
                                cookieWithName(COOKIE_NAME).description("로그인을 통해 얻은 토큰")
                        )
                ));
    }

}
