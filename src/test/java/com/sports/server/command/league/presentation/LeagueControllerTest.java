package com.sports.server.command.league.presentation;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.cookies.CookieDocumentation.cookieWithName;
import static org.springframework.restdocs.cookies.CookieDocumentation.requestCookies;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.ResultActions;

import com.sports.server.command.league.dto.LeagueRequest;
import com.sports.server.command.member.domain.Member;
import com.sports.server.support.DocumentationTest;

import jakarta.servlet.http.Cookie;

class LeagueControllerTest extends DocumentationTest {

	@Test
	void 리그를_생성한다() throws Exception {
		// given
		LocalDateTime fixedDateTime = LocalDateTime.of(2024, 9, 11, 12, 0, 0);
		LeagueRequest.Register request = new LeagueRequest.Register("우물정 제기차기 대회", 4, fixedDateTime, fixedDateTime, List.of(1L, 2L));

        doNothing().when(leagueService).register(any(Member.class), any(LeagueRequest.Register.class));

		// when
		ResultActions result = mockMvc.perform(post("/leagues", request)
			.contentType(MediaType.APPLICATION_JSON)
			.content(objectMapper.writeValueAsString(request))
			.cookie(new Cookie(COOKIE_NAME, "temp-cookie")));

        // then
        result.andExpect(status().isCreated())
                .andDo(restDocsHandler.document(
                                requestFields(
                                        fieldWithPath("name").type(JsonFieldType.STRING).description("대회 이름"),
                                        fieldWithPath("maxRound").type(JsonFieldType.NUMBER).description("대회의 총 라운드 수 (결승 -> 2)"),
                                        fieldWithPath("startAt").type(JsonFieldType.STRING).description("대회 시작 시간"),
                                        fieldWithPath("endAt").type(JsonFieldType.STRING).description("대회 종료 시간"),
										fieldWithPath("teamIds").type(JsonFieldType.ARRAY).description("대회 참가 팀들의 teamId 배열 (없다면 빈 배열)")
                                ),
                                requestCookies(
                                        cookieWithName(COOKIE_NAME).description("로그인을 통해 얻은 토큰")
                                )
                        )
                );
    }

    @Test
    void 리그를_삭제한다() throws Exception {
        // given
        Cookie cookie = new Cookie(COOKIE_NAME, "temp-cookie");

        doNothing().when(leagueService).delete(any(Member.class), any());

        // when
        ResultActions result = mockMvc.perform(delete("/leagues/{leagueId}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .cookie(cookie)
        );

        // then
        result.andExpect(status().isOk())
                .andDo(restDocsHandler.document(
                                pathParameters(
                                        parameterWithName("leagueId").description("삭제할 리그의 ID")),
                                requestCookies(
                                        cookieWithName(COOKIE_NAME).description("로그인을 통해 얻은 토큰")
                                )
                        )
                );
    }

	@Test
	void 리그를_수정한다() throws Exception {
		// given
		Long leagueId = 5124L;
		LocalDateTime fixedDateTime = LocalDateTime.of(2024, 9, 11, 12, 0, 0);
		LeagueRequest.Update request = new LeagueRequest.Update("훕치치배 망고 빨리먹기 대회", 16, fixedDateTime,
				fixedDateTime);

		doNothing().when(leagueService).update(any(Member.class), any(LeagueRequest.Update.class), anyLong());

		// when
		ResultActions result = mockMvc.perform(put("/leagues/{leagueId}", leagueId)
			.contentType(MediaType.APPLICATION_JSON_VALUE)
			.content(objectMapper.writeValueAsString(request))
			.cookie(new Cookie(COOKIE_NAME, "temp-cookie")));

		// then
		result.andExpect(status().isOk())
			.andDo(restDocsHandler.document(
				requestFields(
					fieldWithPath("name").type(JsonFieldType.STRING).description("변경할 대회의 이름"),
					fieldWithPath("startAt").type(JsonFieldType.STRING).description("변경할 대회 시작시간"),
					fieldWithPath("endAt").type(JsonFieldType.STRING).description("변경할 대회 종료시간"),
					fieldWithPath("maxRound").type(JsonFieldType.NUMBER).description("변경할 대회의 총 라운드 수")
				),
				requestCookies(
					cookieWithName(COOKIE_NAME).description("로그인을 통해 얻은 토큰")
				)
			));
	}

	@Test
	void 리그에_참가하는_팀들을_추가한다() throws Exception {
		// given
		Long leagueId = 1L;
		LeagueRequest.Teams teamsRequest = new LeagueRequest.Teams(List.of(1L, 2L, 3L, 4L));

		when(leagueService.addTeams(any(Member.class), anyLong(), any(LeagueRequest.Teams.class))).thenReturn(null);

		// when
		ResultActions result = mockMvc.perform(post("/leagues/{leagueId}/teams", leagueId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(teamsRequest))
				.cookie(new Cookie(COOKIE_NAME, "temp-cookie")));

		// then
		result.andExpect(status().isCreated())
				.andDo(restDocsHandler.document(
								pathParameters(
										parameterWithName("leagueId").description("팀을 추가할 리그의 ID")
								),
								requestFields(
										fieldWithPath("teamIds").type(JsonFieldType.ARRAY).description("대회에 추가할 참가 팀들의 ID 리스트")
								),
								requestCookies(
										cookieWithName(COOKIE_NAME).description("로그인을 통해 얻은 토큰")
								)
						)
				);
	}

	@Test
	void 리그에_참가하는_팀들을_삭제한다() throws Exception {
		// given
		Long leagueId = 1L;
		LeagueRequest.Teams teamsRequest = new LeagueRequest.Teams(List.of(1L, 2L));

		doNothing().when(leagueService).removeTeams(any(Member.class), anyLong(), any(LeagueRequest.Teams.class));

		// when
		ResultActions result = mockMvc.perform(delete("/leagues/{leagueId}/teams", leagueId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(teamsRequest))
				.cookie(new Cookie(COOKIE_NAME, "temp-cookie")));

		// then
		result.andExpect(status().isOk())
				.andDo(restDocsHandler.document(
								pathParameters(
										parameterWithName("leagueId").description("팀을 삭제할 리그의 ID")
								),
								requestFields(
										fieldWithPath("teamIds").type(JsonFieldType.ARRAY).description("대회에서 삭제할 참가 팀들의 ID 리스트")
								),
								requestCookies(
										cookieWithName(COOKIE_NAME).description("로그인을 통해 얻은 토큰")
								)
						)
				);
	}
}