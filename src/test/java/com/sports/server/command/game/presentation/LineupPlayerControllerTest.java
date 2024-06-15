package com.sports.server.command.game.presentation;

import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.ResultActions;

import com.sports.server.command.game.dto.LineupPlayerStateUpdateRequest;
import com.sports.server.support.DocumentationTest;

public class LineupPlayerControllerTest extends DocumentationTest {

	@Test
	void 라인업_선수의_상태를_변경한다() throws Exception {
		//given
		LineupPlayerStateUpdateRequest request = new LineupPlayerStateUpdateRequest("STARTER", List.of(1L, 2L));

		//when
		ResultActions result = mockMvc.perform(
			put("/lineup-players/state")
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(objectMapper.writeValueAsString(request))
		);

		//then
		result.andExpect(status().isOk())
			.andDo(restDocsHandler.document(
					requestFields(
						fieldWithPath("state").type(JsonFieldType.STRING).description("변경할 라인업 선수 상태"),
						fieldWithPath("lineupPlayerIds").type(JsonFieldType.ARRAY).description("변경할 라인업 선수들의 ID 배열")
					)
				)
			);
	}
}