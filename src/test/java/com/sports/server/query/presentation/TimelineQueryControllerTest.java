package com.sports.server.query.presentation;

import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sports.server.command.league.domain.BasketballQuarter;
import com.sports.server.command.league.domain.SoccerQuarter;
import com.sports.server.command.timeline.domain.GameProgressType;
import com.sports.server.command.timeline.domain.WarningCardType;
import com.sports.server.query.dto.response.*;
import com.sports.server.query.dto.response.AvailableProgressResponse.ProgressAction;
import com.sports.server.query.dto.response.QuarterResponse;
import com.sports.server.support.DocumentationTest;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.ResultActions;

public class TimelineQueryControllerTest extends DocumentationTest {

    private static final String QUARTER2 = "SECOND_HALF";
    private static final String QUARTER2_DISPLAY = "후반전";

    private static final String TEAM_A = "팀A";
    public static final String TEAM_A_IMAGE_URL = "http://example.com/logo_a.png";
    private static final String TEAM_B = "팀B";
    public static final String TEAM_B_IMAGE_URL = "http://example.com/logo_b.png";

    private static final String SCORE_TYPE = "SCORE";
    private static final String SOCCER_REPLACEMENT_TYPE = "SOCCER_REPLACEMENT";
    private static final String BASKETBALL_REPLACEMENT_TYPE = "BASKETBALL_REPLACEMENT";


    @Test
    void 타임라인을_조회한다() throws Exception {
        // given
        Long gameId = 1L;
        BDDMockito.given(timelineQueryService.getTimelines(gameId))
                .willReturn(new GameTimelineResponse(
                        new WinnerResponse(1L, TEAM_B, TEAM_B_IMAGE_URL),
                        List.of(
                                new TimelineResponse(
                                        new QuarterResponse(QUARTER2, QUARTER2_DISPLAY), List.of(
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
                                                ), null),
                                                new ReplacementRecordResponse(1L, "선수3", null),
                                                new ProgressRecordResponse(GameProgressType.QUARTER_START),
                                                new PkRecordResponse(1L, true),
                                                new WarningCardRecordResponse(WarningCardType.YELLOW)
                                        ),
                                        new RecordResponse(
                                                null, 1L, SOCCER_REPLACEMENT_TYPE,
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
                                                ), null),
                                                new ReplacementRecordResponse(1L, "선수3", null),
                                                new ProgressRecordResponse(GameProgressType.QUARTER_END),
                                                new PkRecordResponse(4L, false),
                                                new WarningCardRecordResponse(WarningCardType.RED)
                                        ),
                                        new RecordResponse(
                                                null, 1L, BASKETBALL_REPLACEMENT_TYPE,
                                                5,
                                                "선수4",
                                                1L,
                                                TEAM_A,
                                                TEAM_A_IMAGE_URL,
                                                new ScoreRecordResponse(1L, 2, List.of(
                                                        new ScoreRecordResponse.Snapshot(
                                                                TEAM_A, TEAM_A_IMAGE_URL, 2),
                                                        new ScoreRecordResponse.Snapshot(
                                                                TEAM_B, TEAM_B_IMAGE_URL, 0)
                                                ), null),
                                                new ReplacementRecordResponse(2L, "선수5", true),
                                                new ProgressRecordResponse(GameProgressType.QUARTER_END),
                                                new PkRecordResponse(4L, false),
                                                new WarningCardRecordResponse(WarningCardType.RED)
                                        )
                                ))
                        )
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
                                fieldWithPath("winner").type(JsonFieldType.OBJECT).description("승리 팀 정보 (미확정 시 null)").optional(),
                                fieldWithPath("winner.gameTeamId").type(JsonFieldType.NUMBER).description("승리 팀의 게임 팀 ID"),
                                fieldWithPath("winner.teamName").type(JsonFieldType.STRING).description("승리 팀 이름"),
                                fieldWithPath("winner.teamImageUrl").type(JsonFieldType.STRING).description("승리 팀 이미지 URL"),
                                fieldWithPath("timelines").type(JsonFieldType.ARRAY).description("쿼터별 타임라인 목록"),
                                fieldWithPath("timelines[].gameQuarter").type(JsonFieldType.OBJECT).description("쿼터 정보"),
                                fieldWithPath("timelines[].gameQuarter.key").type(JsonFieldType.STRING).description("쿼터의 키"),
                                fieldWithPath("timelines[].gameQuarter.label").type(JsonFieldType.STRING).description("쿼터 표시명"),
                                fieldWithPath("timelines[].records[].recordId").type(JsonFieldType.NUMBER).description("기록의 ID"),
                                fieldWithPath("timelines[].records[].type").type(JsonFieldType.STRING).description("기록의 타입"),
                                fieldWithPath("timelines[].records[].recordedAt").type(JsonFieldType.NUMBER)
                                        .description("기록된 시간"),
                                fieldWithPath("timelines[].records[].playerName").type(JsonFieldType.STRING)
                                        .description("기록의 대상 선수 이름"),
                                fieldWithPath("timelines[].records[].gameTeamId").type(JsonFieldType.NUMBER)
                                        .description("기록의 대상 게임 팀 ID"),
                                fieldWithPath("timelines[].records[].teamName").type(JsonFieldType.STRING)
                                        .description("기록의 대상 팀 이름"),
                                fieldWithPath("timelines[].records[].teamImageUrl").type(JsonFieldType.STRING)
                                        .description("기록의 대상 팀 이미지"),
                                fieldWithPath("timelines[].records[].scoreRecord.score").type(JsonFieldType.NUMBER)
                                        .description("SCORE 타입일 때 득점한 점수"),
                                fieldWithPath("timelines[].records[].scoreRecord.scoreRecordId").type(JsonFieldType.NUMBER)
                                        .description("SCORE 타입 기록의 ID"),
                                fieldWithPath("timelines[].records[].scoreRecord.snapshot[].teamName").type(JsonFieldType.STRING)
                                        .description("SCORE 타입일 때 점수 스냅샷에 표시할 팀 이름"),
                                fieldWithPath("timelines[].records[].scoreRecord.snapshot[].teamImageUrl").type(
                                                JsonFieldType.STRING)
                                        .description("SCORE 타입일 때 점수 스냅샷에 표시할 팀 이미지"),
                                fieldWithPath("timelines[].records[].scoreRecord.snapshot[].score").type(JsonFieldType.NUMBER)
                                        .description("SCORE 타입일 때 점수 스냅샷에 표시할 점수"),
                                fieldWithPath("timelines[].records[].scoreRecord.assistPlayerName").type(JsonFieldType.NULL)
                                        .description("SCORE 타입일 때 어시스트 선수 이름 (없으면 null)").optional(),
                                fieldWithPath("timelines[].records[].replacementRecord.replacementRecordId").type(
                                                JsonFieldType.NUMBER)
                                        .description("REPLACEMENT 타입 기록의  ID"),
                                fieldWithPath("timelines[].records[].replacementRecord.replacedPlayerName").type(
                                                JsonFieldType.STRING)
                                        .description("REPLACEMENT 타입일 때 교체되어 IN 되는 선수"),
                                fieldWithPath("timelines[].records[].replacementRecord.isFoulOut").type(JsonFieldType.VARIES)
                                        .description("BASKETBALL_REPLACEMENT 타입일 때 파울 아웃 여부 (true: 파울 아웃, false: 일반 교체, 축구 교체는 null)").optional(),
                                fieldWithPath("timelines[].records[].progressRecord.gameProgressType").type(JsonFieldType.STRING)
                                        .description(
                                                "PROGRESS 타입일 때 게임 진행 상태 타입 (GAME_START, QUARTER_START, QUARTER_END, GAME_END)"),
                                fieldWithPath("timelines[].records[].pkRecord.pkRecordId").type(
                                                JsonFieldType.NUMBER)
                                        .description("PK 타입 기록의  ID"),
                                fieldWithPath("timelines[].records[].pkRecord.isSuccess").type(
                                                JsonFieldType.BOOLEAN)
                                        .description("승부차기 득점 성공 여부"),
                                fieldWithPath("timelines[].records[].warningCardRecord.warningCardType").type(
                                                JsonFieldType.STRING)
                                        .description("WARNING_CARD 타입일 때 경고 카드 타입(YELLOW, RED)")
                        )
                ));
    }

    @Test
    void 쿼터별_득점을_조회한다() throws Exception {
        // given
        Long gameId = 5L;
        BDDMockito.given(timelineQueryService.getQuarterScores(gameId))
                .willReturn(List.of(
                        new QuarterScoreResponse(
                                BasketballQuarter.FIRST_QUARTER.name(),
                                BasketballQuarter.FIRST_QUARTER.getDisplayName(),
                                List.of(
                                        new QuarterScoreResponse.TeamScore(7L, 3),
                                        new QuarterScoreResponse.TeamScore(8L, 0)
                                )
                        ),
                        new QuarterScoreResponse(
                                BasketballQuarter.SECOND_QUARTER.name(),
                                BasketballQuarter.SECOND_QUARTER.getDisplayName(),
                                List.of(
                                        new QuarterScoreResponse.TeamScore(7L, 2),
                                        new QuarterScoreResponse.TeamScore(8L, 5)
                                )
                        )
                ));

        // when
        ResultActions result = mockMvc.perform(get("/games/{gameId}/quarter-scores", gameId)
                .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        result.andExpect(status().isOk())
                .andDo(restDocsHandler.document(
                        pathParameters(
                                parameterWithName("gameId").description("경기의 ID")
                        ),
                        responseFields(
                                fieldWithPath("[].quarter").type(JsonFieldType.STRING).description("쿼터 키 (FIRST_QUARTER, SECOND_QUARTER 등)"),
                                fieldWithPath("[].displayName").type(JsonFieldType.STRING).description("쿼터 표시명 (1쿼터, 2쿼터 등)"),
                                fieldWithPath("[].scores[].gameTeamId").type(JsonFieldType.NUMBER).description("경기 팀의 ID"),
                                fieldWithPath("[].scores[].score").type(JsonFieldType.NUMBER).description("해당 쿼터에서 득점한 점수")
                        )
                ));
    }

    @Test
    void 가능한_경기_진행_액션을_조회한다() throws Exception {
        // given
        Long gameId = 1L;
        BDDMockito.given(timelineQueryService.getAvailableProgress(gameId))
                .willReturn(new AvailableProgressResponse(List.of(
                        new ProgressAction(SoccerQuarter.SECOND_HALF.name(), GameProgressType.QUARTER_END, "후반전 종료"),
                        new ProgressAction(SoccerQuarter.SECOND_HALF.name(), GameProgressType.GAME_END, "경기 종료")
                )));

        // when
        ResultActions result = mockMvc.perform(get("/games/{gameId}/available-progress", gameId)
                .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        result.andExpect(status().isOk())
                .andDo(restDocsHandler.document(
                        pathParameters(
                                parameterWithName("gameId").description("경기의 ID")
                        ),
                        responseFields(
                                fieldWithPath("availableActions").type(JsonFieldType.ARRAY).description("가능한 경기 진행 액션 목록"),
                                fieldWithPath("availableActions[].quarter").type(JsonFieldType.STRING).description("쿼터 (FIRST_HALF, SECOND_HALF, EXTRA_TIME, PENALTY_SHOOTOUT 등)"),
                                fieldWithPath("availableActions[].gameProgressType").type(JsonFieldType.STRING).description("경기 진행 타입 (QUARTER_START, QUARTER_END, GAME_END)"),
                                fieldWithPath("availableActions[].displayName").type(JsonFieldType.STRING).description("사용자에게 표시할 액션 이름")
                        )
                ));
    }
}
