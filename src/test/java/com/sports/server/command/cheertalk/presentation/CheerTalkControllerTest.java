package com.sports.server.command.cheertalk.presentation;

import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.patch;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sports.server.command.cheertalk.dto.CheerTalkRequest;
import com.sports.server.support.DocumentationTest;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.ResultActions;

class CheerTalkControllerTest extends DocumentationTest {

    @Test
    void 응원톡을_저장한다() throws Exception {
        // given
        CheerTalkRequest request = new CheerTalkRequest("응원해요~", 1L);

        // when
        ResultActions result = mockMvc.perform(post("/cheer-talks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        );

        result.andExpect((status().isOk()))
                .andDo(restDocsHandler.document(
                        requestFields(
                                fieldWithPath("content").type(JsonFieldType.STRING).description("응원톡의 내용"),
                                fieldWithPath("gameTeamId").type(JsonFieldType.NUMBER).description("응원하는 게임팀")
                        )
                ));
    }

    @Test
    void 리그_응원톡을_가린다() throws Exception {
        // given
        Long leagueId = 1L;
        Long cheerTalkId = 1L;

        Cookie cookie = new Cookie(COOKIE_NAME, "temp-cookie");

        // when
        ResultActions result = mockMvc.perform(patch("/cheer-talks/{leagueId}/{cheerTalkId}/block", leagueId, cheerTalkId)
                .contentType(MediaType.APPLICATION_JSON)
                .cookie(cookie)
        );

        // then
        result.andExpect((status().isOk()))
                .andDo(restDocsHandler.document(
                        pathParameters(
                                parameterWithName("leagueId").description("리그의 ID"),
                                parameterWithName("cheerTalkId").description("응원톡의 ID")
                        )
                ));
    }

    @Test
    void 리그_응원톡을_가리기_취소한다() throws Exception {
        // given
        Long leagueId = 1L;
        Long cheerTalkId = 1L;

        Cookie cookie = new Cookie(COOKIE_NAME, "temp-cookie");

        // when
        ResultActions result = mockMvc.perform(patch("/cheer-talks/{leagueId}/{cheerTalkId}/unblock", leagueId, cheerTalkId)
                .contentType(MediaType.APPLICATION_JSON)
                .cookie(cookie)
        );

        // then
        result.andExpect((status().isOk()))
                .andDo(restDocsHandler.document(
                        pathParameters(
                                parameterWithName("leagueId").description("리그의 ID"),
                                parameterWithName("cheerTalkId").description("응원톡의 ID")
                        )
                ));
    }

}
