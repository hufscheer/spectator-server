package com.sports.server.query.presentation;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.anyLong;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.ResultActions;

import com.sports.server.command.member.domain.Member;
import com.sports.server.common.dto.PageRequestDto;
import com.sports.server.query.dto.response.CheerTalkResponse;
import com.sports.server.support.DocumentationTest;

import jakarta.servlet.http.Cookie;

public class CheerTalkQueryControllerTest extends DocumentationTest {

	@Test
	void 응원톡을_조회한다() throws Exception {

		//given
		Long gameId = 1L;

		PageRequestDto pageRequestDto = new PageRequestDto(1L, 2);

		LocalDateTime createdAt = LocalDateTime.of(2024, 1, 21, 11, 46, 0);
		List<CheerTalkResponse.ForSpectator> response = List.of(
			new CheerTalkResponse.ForSpectator(
				2L, "응원해요", 1L, createdAt, false
			),
			new CheerTalkResponse.ForSpectator(
				3L, "파이팅", 2L, createdAt, false
			)
		);

		given(cheerTalkQueryService.getCheerTalksByGameId(gameId, pageRequestDto))
			.willReturn(response);

		// when
		ResultActions result = mockMvc.perform(get("/games/{gameId}/cheer-talks", gameId)
			.queryParam("cursor", String.valueOf(1))
			.queryParam("size", String.valueOf(2))
			.contentType(MediaType.APPLICATION_JSON)
		);

		result.andExpect((status().isOk()))
			.andDo(restDocsHandler.document(
				queryParameters(
					parameterWithName("cursor").description("마지막 응원톡의 ID"),
					parameterWithName("size").description("조회하고자 하는 응원톡의 개수")
				),
				pathParameters(
					parameterWithName("gameId").description("게임의 ID")
				),
				responseFields(
					fieldWithPath("[].cheerTalkId").type(JsonFieldType.NUMBER).description("응원톡의 ID"),
					fieldWithPath("[].content").type(JsonFieldType.STRING).description("응원톡의 내용"),
					fieldWithPath("[].gameTeamId").type(JsonFieldType.NUMBER)
						.description("응원톡에 해당하는 게임팀의 ID"),
					fieldWithPath("[].createdAt").type(JsonFieldType.STRING).description("생성된 날짜 및 시각"),
					fieldWithPath("[].isBlocked").type(JsonFieldType.BOOLEAN).description("응원톡의 블락 여부")
				)
			));
	}

	@Test
	void 리그의_신고된_응원톡을_조회한다() throws Exception {
		//given
		Long leagueId = 1L;

		PageRequestDto pageRequestDto = new PageRequestDto(1L, 2);

		LocalDateTime createdAt = LocalDateTime.of(2024, 1, 21, 11, 46, 0);
		List<CheerTalkResponse.Reported> response = List.of(
			new CheerTalkResponse.Reported(
				2L, 1L, 1L, "응원해요", 1L, createdAt, false, "게임 이름", "리그 이름"
			),
			new CheerTalkResponse.Reported(
				3L, 1L, 1L, "파이팅", 2L, createdAt, false, "게임 이름", "리그 이름"
			)
		);

		given(cheerTalkQueryService.getReportedCheerTalksByLeagueId(
			eq(leagueId),
			eq(pageRequestDto),
			any(Member.class))
		).willReturn(response);

		// when
		ResultActions result = mockMvc.perform(get("/leagues/{leagueId}/cheer-talks/reported", leagueId)
			.queryParam("cursor", String.valueOf(1))
			.queryParam("size", String.valueOf(2))
			.contentType(MediaType.APPLICATION_JSON)
			.cookie(new Cookie(COOKIE_NAME, "temp-cookie"))
		);

		result.andExpect((status().isOk()))
			.andDo(restDocsHandler.document(
				queryParameters(
					parameterWithName("cursor").description("마지막 응원톡의 ID"),
					parameterWithName("size").description("조회하고자 하는 응원톡의 개수")
				),
				pathParameters(
					parameterWithName("leagueId").description("리그의 ID")
				),
				responseFields(
					fieldWithPath("[].cheerTalkId").type(JsonFieldType.NUMBER).description("응원톡의 ID"),
					fieldWithPath("[].gameId").type(JsonFieldType.NUMBER).description("응원톡이 등록된 게임의 ID"),
					fieldWithPath("[].leagueId").type(JsonFieldType.NUMBER).description("응원톡이 등록된 리그의 ID"),
					fieldWithPath("[].content").type(JsonFieldType.STRING).description("응원톡의 내용"),
					fieldWithPath("[].gameTeamId").type(JsonFieldType.NUMBER)
						.description("응원톡에 해당하는 게임팀의 ID"),
					fieldWithPath("[].createdAt").type(JsonFieldType.STRING).description("생성된 날짜 및 시각"),
					fieldWithPath("[].isBlocked").type(JsonFieldType.BOOLEAN).description("응원톡의 블락 여부"),
					fieldWithPath("[].gameName").type(JsonFieldType.STRING).description("응원톡이 등록된 게임의 이름"),
					fieldWithPath("[].leagueName").type(JsonFieldType.STRING).description("응원톡이 등록된 리그의 이름")
				)
			));
	}

	@Test
	void 리그의_가려진_응원톡을_조회한다() throws Exception {
		// given
		Long leagueId = 1L;
		LocalDateTime createdAt = LocalDateTime.of(2024, 1, 21, 11, 46, 0);
		List<CheerTalkResponse.Blocked> responses = List.of(
			new CheerTalkResponse.Blocked(2L, 1L, 1L, "미안하다이거보여주려고어그로끌었다", createdAt, "제 1경기 학츄핑 vs 하츄핑",
				"매봉역 배 타코 빨리 먹기 대작전"),
			new CheerTalkResponse.Blocked(3L, 1L, 1L, "이학을국회로", createdAt, "제 2경기 학츄핑 vs 시진핑",
				"매봉역 배 타코 빨리 먹기 대작전"));

		doReturn(responses).when(cheerTalkQueryService)
			.getBlockedCheerTalksByLeagueId(anyLong(), any(PageRequestDto.class), any(Member.class));

		// when
		ResultActions result = mockMvc.perform(
			get("/leagues/{leagueId}/cheer-talks/blocked", leagueId)
				.contentType(MediaType.APPLICATION_JSON)
				.queryParam("cursor", String.valueOf(1))
				.queryParam("size", String.valueOf(2))
				.cookie(new Cookie(COOKIE_NAME, "temp-cookie")));

		// then
		result.andExpect((status().isOk()))
			.andDo(restDocsHandler.document(
				queryParameters(
					parameterWithName("cursor").description("마지막 응원톡의 ID"),
					parameterWithName("size").description("조회하고자 하는 응원톡의 개수")
				),
				pathParameters(
					parameterWithName("leagueId").description("리그의 ID")
				),
				responseFields(
					fieldWithPath("[].cheerTalkId").type(JsonFieldType.NUMBER).description("응원톡의 ID"),
					fieldWithPath("[].gameId").type(JsonFieldType.NUMBER).description("응원톡이 등록된 게임의 ID"),
					fieldWithPath("[].leagueId").type(JsonFieldType.NUMBER).description("응원톡이 등록된 리그의 ID"),
					fieldWithPath("[].content").type(JsonFieldType.STRING).description("응원톡의 내용"),
					fieldWithPath("[].createdAt").type(JsonFieldType.STRING).description("생성된 날짜 및 시각"),
					fieldWithPath("[].gameName").type(JsonFieldType.STRING).description("응원톡이 등록된 게임의 이름"),
					fieldWithPath("[].leagueName").type(JsonFieldType.STRING).description("응원톡이 등록된 리그의 이름")
				)));
	}
}
