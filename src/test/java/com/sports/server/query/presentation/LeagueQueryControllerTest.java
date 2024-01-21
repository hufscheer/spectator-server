package com.sports.server.query.presentation;

import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sports.server.query.dto.response.LeagueResponse;
import com.sports.server.query.dto.response.LeagueSportResponse;
import com.sports.server.support.DocumentationTest;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.ResultActions;

public class LeagueQueryControllerTest extends DocumentationTest {

    @Test
    void 리그_전체를_조회한다() throws Exception {

        // given
        List<LeagueResponse> responses = List.of(
                new LeagueResponse(1L, "리그 첫번쨰"),
                new LeagueResponse(2L, "리그 두번째")
        );

        given(leagueQueryService.findAll())
                .willReturn(responses);

        // when
        ResultActions result = mockMvc.perform(get("/leagues")
                .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        result.andExpect((status().isOk()))
                .andDo(RESULT_HANDLER.document(
                        responseFields(
                                fieldWithPath("[].leagueId").type(JsonFieldType.NUMBER).description("리그의 ID"),
                                fieldWithPath("[].name").type(JsonFieldType.STRING).description("리그의 이름")
                        )
                ));
    }

    @Test
    void 리그의_해당하는_스포츠_전체를_조회한다() throws Exception {

        // given
        Long leagueId = 1L;

        List<LeagueSportResponse> responses = List.of(
                new LeagueSportResponse(1L, "축구"),
                new LeagueSportResponse(2L, "농구")
        );

        given(leagueQueryService.findSportsByLeague(leagueId))
                .willReturn(responses);

        // when
        ResultActions result = mockMvc.perform(get("/leagues/{leagueId}/sports", leagueId)
                .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        result.andExpect((status().isOk()))
                .andDo(RESULT_HANDLER.document(
                        pathParameters(
                                parameterWithName("leagueId").description("리그의 ID")
                        ),
                        responseFields(
                                fieldWithPath("[].sportId").type(JsonFieldType.NUMBER).description("스포츠의 ID"),
                                fieldWithPath("[].name").type(JsonFieldType.STRING).description("스포츠의 이름")
                        )
                ));
    }
}
