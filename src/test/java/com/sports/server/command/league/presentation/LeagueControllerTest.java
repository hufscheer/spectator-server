package com.sports.server.command.league.presentation;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.springframework.restdocs.cookies.CookieDocumentation.cookieWithName;
import static org.springframework.restdocs.cookies.CookieDocumentation.requestCookies;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.ResultActions;

import com.sports.server.command.league.dto.LeagueRequestDto;
import com.sports.server.command.member.domain.Member;
import com.sports.server.support.DocumentationTest;

import jakarta.servlet.http.Cookie;

class LeagueControllerTest extends DocumentationTest {

	@Test
	void 리그를_생성한다() throws Exception {
		// given
		LeagueRequestDto.Register request = new LeagueRequestDto.Register(1L, "우물정 제기차기 대회", "4강", LocalDateTime.now(), LocalDateTime.now());

        doNothing().when(leagueService).register(any(Member.class), any(LeagueRequestDto.Register.class));

		// when
		ResultActions result = mockMvc.perform(post("/leagues", request)
			.contentType(MediaType.APPLICATION_JSON)
			.content(objectMapper.writeValueAsString(request))
			.cookie(new Cookie(COOKIE_NAME, "temp-cookie")));

        // then
        result.andExpect(status().isOk())
                .andDo(restDocsHandler.document(
                                requestFields(
                                        fieldWithPath("organizationId").type(JsonFieldType.NUMBER).description("조직 id"),
                                        fieldWithPath("name").type(JsonFieldType.STRING).description("대회 이름"),
                                        fieldWithPath("maxRound").type(JsonFieldType.STRING).description("대회 진행 라운드 수"),
                                        fieldWithPath("startAt").type(JsonFieldType.STRING).description("대회 시작 시간"),
                                        fieldWithPath("endAt").type(JsonFieldType.STRING).description("대회 종료 시간")
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
		LeagueRequestDto.Update request = new LeagueRequestDto.Update("훕치치배 망고 빨리먹기 대회", LocalDateTime.now(),
			LocalDateTime.now(), "16강");

		doNothing().when(leagueService).update(any(Member.class), any(LeagueRequestDto.Update.class), anyLong());

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
					fieldWithPath("maxRound").type(JsonFieldType.STRING).description("변경할 대회의 총 라운드 수")
				),
				requestCookies(
					cookieWithName(COOKIE_NAME).description("로그인을 통해 얻은 토큰")
				)
			));
	}
}