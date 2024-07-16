package com.sports.server.command.league.presentation;

import static org.mockito.Mockito.*;
import static org.springframework.restdocs.cookies.CookieDocumentation.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;

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
		LeagueRequest request = new LeagueRequest(1L, "우물정 제기차기 대회", 4, LocalDateTime.now(), LocalDateTime.now());
		Cookie cookie = new Cookie(COOKIE_NAME, "temp-cookie");

		doNothing().when(leagueService).register(any(Member.class), any(LeagueRequest.class));

		// when
		ResultActions result = mockMvc.perform(post("/leagues", request)
			.contentType(MediaType.APPLICATION_JSON)
			.content(objectMapper.writeValueAsString(request))
			.cookie(cookie));

		// then
		result.andExpect(status().isOk())
			.andDo(restDocsHandler.document(
					requestFields(
						fieldWithPath("organizationId").type(JsonFieldType.NUMBER).description("조직 id"),
						fieldWithPath("name").type(JsonFieldType.STRING).description("대회 이름"),
						fieldWithPath("maxRound").type(JsonFieldType.NUMBER).description("대회 진행 라운드 수"),
						fieldWithPath("startAt").type(JsonFieldType.STRING).description("대회 시작 시간"),
						fieldWithPath("endAt").type(JsonFieldType.STRING).description("대회 종료 시간")
					),
					requestCookies(
						cookieWithName(COOKIE_NAME).description("로그인을 통해 얻은 토큰")
					)
				)
			);
	}
}