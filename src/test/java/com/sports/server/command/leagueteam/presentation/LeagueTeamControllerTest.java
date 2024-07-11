package com.sports.server.command.leagueteam.presentation;


import static org.springframework.restdocs.cookies.CookieDocumentation.cookieWithName;
import static org.springframework.restdocs.cookies.CookieDocumentation.requestCookies;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sports.server.command.leagueteam.dto.LeagueTeamRegisterRequest;
import com.sports.server.command.leagueteam.dto.LeagueTeamRegisterRequest.LeagueTeamPlayerRegisterRequest;
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
        List<LeagueTeamPlayerRegisterRequest> playerRegisterRequests = List.of(
                new LeagueTeamPlayerRegisterRequest("name-a", 1),
                new LeagueTeamPlayerRegisterRequest("name-b", 2));
        LeagueTeamRegisterRequest request = new LeagueTeamRegisterRequest(
                "name", "logo-image-url", playerRegisterRequests);
        Cookie cookie = new Cookie(COOKIE_NAME, "temp-cookie");

        setupMockAuthentication();
        Mockito.doNothing().when(leagueTeamService).register(Mockito.anyLong(), Mockito.any(), Mockito.any());

        // when
        ResultActions result = mockMvc.perform(post("/manager/leagues/{leagueId}/teams", leagueId, request)
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
}
