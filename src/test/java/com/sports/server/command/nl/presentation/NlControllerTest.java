package com.sports.server.command.nl.presentation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.cookies.CookieDocumentation.cookieWithName;
import static org.springframework.restdocs.cookies.CookieDocumentation.requestCookies;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sports.server.command.nl.dto.NlExecuteResponse;
import com.sports.server.command.nl.dto.NlProcessResponse;
import com.sports.server.command.nl.dto.NlProcessResponse.FailedLine;
import com.sports.server.command.nl.dto.NlProcessResponse.ParsedPlayerPreview;
import com.sports.server.command.nl.dto.NlProcessResponse.Preview;
import com.sports.server.support.DocumentationTest;
import jakarta.servlet.http.Cookie;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.ResultActions;

public class NlControllerTest extends DocumentationTest {

    @Test
    void 선수_정보를_파싱하여_프리뷰를_반환한다() throws Exception {
        // given
        NlProcessResponse response = new NlProcessResponse(
                "2명의 선수가 인식되었습니다.",
                new Preview(
                        List.of(
                                new ParsedPlayerPreview("홍길동", "202600001", 10),
                                new ParsedPlayerPreview("김철수", "202600002", 7)
                        ),
                        2,
                        List.of()
                )
        );

        given(nlService.process(any())).willReturn(response);

        Map<String, Object> request = Map.of(
                "history", List.of(),
                "message", "홍길동 202600001 10\n김철수 202600002 7"
        );

        // when
        ResultActions result = mockMvc.perform(post("/nl/process")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .cookie(new Cookie(COOKIE_NAME, "temp-cookie"))
        );

        // then
        result.andExpect(status().isOk())
                .andDo(restDocsHandler.document(
                        requestCookies(
                                cookieWithName(COOKIE_NAME).description("로그인을 통해 얻은 토큰")
                        ),
                        requestFields(
                                fieldWithPath("history").type(JsonFieldType.ARRAY).description("대화 히스토리"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("선수 정보가 포함된 자연어 텍스트")
                        ),
                        responseFields(
                                fieldWithPath("displayMessage").type(JsonFieldType.STRING).description("사용자에게 표시할 메시지"),
                                fieldWithPath("preview.players[].name").type(JsonFieldType.STRING).description("선수 이름"),
                                fieldWithPath("preview.players[].studentNumber").type(JsonFieldType.STRING).description("학번"),
                                fieldWithPath("preview.players[].jerseyNumber").type(JsonFieldType.NUMBER).description("등번호"),
                                fieldWithPath("preview.total").type(JsonFieldType.NUMBER).description("인식된 선수 수"),
                                fieldWithPath("preview.parseFailedLines").type(JsonFieldType.ARRAY).description("파싱 실패 라인 목록")
                        )
                ));
    }

    @Test
    void 선수를_등록한다() throws Exception {
        // given
        NlExecuteResponse response = new NlExecuteResponse(
                "정치외교학과 DPS에 2명의 선수가 등록되었습니다.",
                new NlExecuteResponse.Result(1, 1, 0)
        );

        given(nlService.execute(any(), any())).willReturn(response);

        Map<String, Object> request = Map.of(
                "leagueId", 1,
                "teamId", 1,
                "players", List.of(
                        Map.of("name", "홍길동", "studentNumber", "202600001", "jerseyNumber", 10),
                        Map.of("name", "김철수", "studentNumber", "202600002", "jerseyNumber", 7)
                )
        );

        // when
        ResultActions result = mockMvc.perform(post("/nl/execute")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .cookie(new Cookie(COOKIE_NAME, "temp-cookie"))
        );

        // then
        result.andExpect(status().isOk())
                .andDo(restDocsHandler.document(
                        requestCookies(
                                cookieWithName(COOKIE_NAME).description("로그인을 통해 얻은 토큰")
                        ),
                        requestFields(
                                fieldWithPath("leagueId").type(JsonFieldType.NUMBER).description("리그 ID"),
                                fieldWithPath("teamId").type(JsonFieldType.NUMBER).description("팀 ID"),
                                fieldWithPath("players[].name").type(JsonFieldType.STRING).description("선수 이름"),
                                fieldWithPath("players[].studentNumber").type(JsonFieldType.STRING).description("학번"),
                                fieldWithPath("players[].jerseyNumber").type(JsonFieldType.NUMBER).description("등번호")
                        ),
                        responseFields(
                                fieldWithPath("displayMessage").type(JsonFieldType.STRING).description("사용자에게 표시할 메시지"),
                                fieldWithPath("result.created").type(JsonFieldType.NUMBER).description("신규 생성된 선수 수"),
                                fieldWithPath("result.assigned").type(JsonFieldType.NUMBER).description("팀에 배정된 선수 수"),
                                fieldWithPath("result.skipped").type(JsonFieldType.NUMBER).description("건너뛴 선수 수")
                        )
                ));
    }
}
