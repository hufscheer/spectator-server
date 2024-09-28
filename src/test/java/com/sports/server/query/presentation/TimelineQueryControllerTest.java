package com.sports.server.query.presentation;

import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sports.server.command.timeline.domain.GameProgressType;
import com.sports.server.query.dto.response.PkRecordResponse;
import com.sports.server.query.dto.response.ProgressRecordResponse;
import com.sports.server.query.dto.response.RecordResponse;
import com.sports.server.query.dto.response.ReplacementRecordResponse;
import com.sports.server.query.dto.response.ScoreRecordResponse;
import com.sports.server.query.dto.response.TimelineResponse;
import com.sports.server.support.DocumentationTest;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.ResultActions;

public class TimelineQueryControllerTest extends DocumentationTest {

    private static final String QUARTER2 = "2쿼터";

    private static final String TEAM_A = "팀A";
    public static final String TEAM_A_IMAGE_URL = "http://example.com/logo_a.png";
    private static final String TEAM_B = "팀B";
    public static final String TEAM_B_IMAGE_URL = "http://example.com/logo_b.png";

    private static final String SCORE_TYPE = "SCORE";
    private static final String REPLACEMENT_TYPE = "REPLACEMENT";


    @Test
    void 타임라인을_조회한다() throws Exception {
        // given
        Long gameId = 1L;
        BDDMockito.given(timelineQueryService.getTimelines(gameId))
                .willReturn(List.of(
                        new TimelineResponse(
                                QUARTER2, List.of(
                                new RecordResponse(
                                        null, 1L, SCORE_TYPE,
                                        13,
                                        "선수10",
                                        1L,
                                        TEAM_B,
                                        TEAM_B_IMAGE_URL,
                                        new ScoreRecordResponse(1L, 3, List.of(
                                                new ScoreRecordResponse.Snapshot(
                                                        TEAM_A, TEAM_A_IMAGE_URL, 2),
                                                new ScoreRecordResponse.Snapshot(
                                                        TEAM_B, TEAM_B_IMAGE_URL, 3)
                                        )),
                                        new ReplacementRecordResponse(1L, "선수3"),
                                        new ProgressRecordResponse(GameProgressType.QUARTER_START),
                                        new PkRecordResponse(1L, true)
                                ),
                                new RecordResponse(
                                        null, 1L, REPLACEMENT_TYPE,
                                        10,
                                        "선수2",
                                        1L,
                                        TEAM_A,
                                        TEAM_A_IMAGE_URL,
                                        new ScoreRecordResponse(1L, 2, List.of(
                                                new ScoreRecordResponse.Snapshot(
                                                        TEAM_A, TEAM_A_IMAGE_URL, 2),
                                                new ScoreRecordResponse.Snapshot(
                                                        TEAM_B, TEAM_B_IMAGE_URL, 0)
                                        )),
                                        new ReplacementRecordResponse(1L, "선수3"),
                                        new ProgressRecordResponse(GameProgressType.QUARTER_END),
                                        new PkRecordResponse(4L, false)
                                )
                        ))
                ));

        // when
        ResultActions result = mockMvc.perform(get("/games/{gameId}/timeline", gameId)
                .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        result.andExpect((status().isOk()))
                .andDo(restDocsHandler.document(
                        pathParameters(
                                parameterWithName("gameId").description("게임의 ID")
                        ),
                        responseFields(
                                fieldWithPath("[].gameQuarter").type(JsonFieldType.STRING).description("쿼터의 이름"),
                                fieldWithPath("[].records[].recordId").type(JsonFieldType.NUMBER).description("기록의 ID"),
                                fieldWithPath("[].records[].type").type(JsonFieldType.STRING).description("기록의 타입"),
                                fieldWithPath("[].records[].recordedAt").type(JsonFieldType.NUMBER)
                                        .description("기록된 시간"),
                                fieldWithPath("[].records[].playerName").type(JsonFieldType.STRING)
                                        .description("기록의 대상 선수 이름"),
                                fieldWithPath("[].records[].gameTeamId").type(JsonFieldType.NUMBER)
                                        .description("기록의 대상 게임 팀 ID"),
                                fieldWithPath("[].records[].teamName").type(JsonFieldType.STRING)
                                        .description("기록의 대상 팀 이름"),
                                fieldWithPath("[].records[].teamImageUrl").type(JsonFieldType.STRING)
                                        .description("기록의 대상 팀 이미지"),
                                fieldWithPath("[].records[].scoreRecord.score").type(JsonFieldType.NUMBER)
                                        .description("SCORE 타입일 때 득점한 점수"),
                                fieldWithPath("[].records[].scoreRecord.scoreRecordId").type(JsonFieldType.NUMBER)
                                        .description("SCORE 타입 기록의 ID"),
                                fieldWithPath("[].records[].scoreRecord.snapshot[].teamName").type(JsonFieldType.STRING)
                                        .description("SCORE 타입일 때 점수 스냅샷에 표시할 팀 이름"),
                                fieldWithPath("[].records[].scoreRecord.snapshot[].teamImageUrl").type(
                                                JsonFieldType.STRING)
                                        .description("SCORE 타입일 때 점수 스냅샷에 표시할 팀 이미지"),
                                fieldWithPath("[].records[].scoreRecord.snapshot[].score").type(JsonFieldType.NUMBER)
                                        .description("SCORE 타입일 때 점수 스냅샷에 표시할 점수"),
                                fieldWithPath("[].records[].replacementRecord.replacementRecordId").type(
                                                JsonFieldType.NUMBER)
                                        .description("REPLACEMENT 타입 기록의  ID"),
                                fieldWithPath("[].records[].replacementRecord.replacedPlayerName").type(
                                                JsonFieldType.STRING)
                                        .description("REPLACEMENT 타입일 때 교체되어 IN 되는 선수"),
                                fieldWithPath("[].records[].progressRecord.gameProgressType").type(JsonFieldType.STRING)
                                        .description(
                                                "PROGRESS 타입일 때 게임 진행 상태 타입 (GAME_START, QUARTER_START, QUARTER_END, GAME_END)"),
                                fieldWithPath("[].records[].pkRecord.pkRecordId").type(
                                                JsonFieldType.NUMBER)
                                        .description("PK 타입 기록의  ID"),
                                fieldWithPath("[].records[].pkRecord.isSuccess").type(
                                                JsonFieldType.BOOLEAN)
                                        .description("승부차기 득점 성공 여부")
                        )
                ));


    }
}
