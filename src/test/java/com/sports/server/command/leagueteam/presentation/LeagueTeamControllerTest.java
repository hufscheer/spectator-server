package com.sports.server.command.leagueteam.presentation;


import com.sports.server.command.team.dto.TeamRequest;
import com.sports.server.support.DocumentationTest;
import jakarta.servlet.http.Cookie;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import static org.springframework.restdocs.cookies.CookieDocumentation.cookieWithName;
import static org.springframework.restdocs.cookies.CookieDocumentation.requestCookies;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.patch;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import org.springframework.restdocs.payload.JsonFieldType;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import org.springframework.test.web.servlet.ResultActions;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class LeagueTeamControllerTest extends DocumentationTest {

    @Test
    void 리그팀을_등록한다() throws Exception {

        // given
        Long leagueId = 1L;
        List<LeagueTeamPlayerRequest.Register> playerRegisterRequests = List.of(
                new LeagueTeamPlayerRequest.Register("name-a", 1, "202000001"),
                new LeagueTeamPlayerRequest.Register("name-b", 2, "202000002"));
        TeamRequest.Register request = new TeamRequest.Register(
                "name", "logo-image-url", playerRegisterRequests, "color code");
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
                                fieldWithPath("teamColor").type(JsonFieldType.STRING).description("팀의 색에 대한 hexcode"),
                                fieldWithPath("players").type(JsonFieldType.ARRAY).description("리그팀 선수 목록"),
                                fieldWithPath("players[].name").type(JsonFieldType.STRING).description("선수의 이름"),
                                fieldWithPath("players[].number").type(JsonFieldType.NUMBER).description("선수의 번호"),
                                fieldWithPath("players[].studentNumber").type(JsonFieldType.STRING).description("선수의 학번")
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
        List<LeagueTeamPlayerRequest.Register> playerRegisterRequests = List.of(
                new LeagueTeamPlayerRequest.Register("name-a", 1, "202000001"),
                new LeagueTeamPlayerRequest.Register("name-b", 2, "202000002"));
        List<LeagueTeamPlayerRequest.Update> playerUpdateRequests = List.of(
                new LeagueTeamPlayerRequest.Update(1L, "여름수박진승희", 0, "202000003")
        );
        TeamRequest.Update request = new TeamRequest.Update(
                "name", "logo-image-url", playerRegisterRequests, playerUpdateRequests, List.of(5L));

        Cookie cookie = new Cookie(COOKIE_NAME, "temp-cookie");

        Mockito.doNothing().when(leagueTeamService)
                .update(Mockito.anyLong(), Mockito.any(), Mockito.any(), Mockito.anyLong());

        // when
        ResultActions result = mockMvc.perform(patch("/leagues/{leagueId}/teams/{teamId}", leagueId, teamId, request)
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
                                fieldWithPath("newPlayers").type(JsonFieldType.ARRAY).description("등록할 리그팀 선수 목록"),
                                fieldWithPath("newPlayers[].name").type(JsonFieldType.STRING).description("등록할 선수의 이름"),
                                fieldWithPath("newPlayers[].number").type(JsonFieldType.NUMBER)
                                        .description("등록할 선수의 번호"),
                                fieldWithPath("newPlayers[].studentNumber").type(JsonFieldType.STRING)
                                        .description("등록할 선수의 학번"),
                                fieldWithPath("updatedPlayers").type(JsonFieldType.ARRAY).description("수정할 리그팀 선수 목록"),
                                fieldWithPath("updatedPlayers[].id").type(JsonFieldType.NUMBER)
                                        .description("수정할 리그팀 선수의 ID"),
                                fieldWithPath("updatedPlayers[].name").type(JsonFieldType.STRING)
                                        .description("수정하고자 하는 이름"),
                                fieldWithPath("updatedPlayers[].number").type(JsonFieldType.NUMBER)
                                        .description("수정하고자 하는 번호"),
                                fieldWithPath("updatedPlayers[].studentNumber").type(JsonFieldType.STRING)
                                        .description("수정하고자 하는 학번"),
                                fieldWithPath("deletedPlayerIds").type(JsonFieldType.ARRAY)
                                        .description("삭제할 리그팀 선수의 ID")

                        ),
                        requestCookies(
                                cookieWithName(COOKIE_NAME).description("로그인을 통해 얻은 토큰")
                        )
                ));
    }

    @Test
    void 리그팀을_삭제한다() throws Exception {

        // given
        Long leagueId = 1L;
        Long teamId = 3L;
        Cookie cookie = new Cookie(COOKIE_NAME, "temp-cookie");

        Mockito.doNothing().when(leagueTeamService)
                .delete(Mockito.anyLong(), Mockito.any(), Mockito.anyLong());

        // when
        ResultActions result = mockMvc.perform(delete("/leagues/{leagueId}/teams/{teamId}", leagueId, teamId)
                .contentType(MediaType.APPLICATION_JSON)
                .cookie(cookie)
        );

        // then
        result.andExpect(status().isOk())
                .andDo(restDocsHandler.document(
                        pathParameters(
                                parameterWithName("leagueId").description("리그의 ID"),
                                parameterWithName("teamId").description("리그팀의 ID")
                        ),
                        requestCookies(
                                cookieWithName(COOKIE_NAME).description("로그인을 통해 얻은 토큰")
                        )
                ));
    }

}
