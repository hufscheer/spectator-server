package com.sports.server.query.acceptance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.sports.server.command.timeline.domain.GameProgressType;
import com.sports.server.query.dto.response.PkRecordResponse;
import com.sports.server.query.dto.response.ProgressRecordResponse;
import com.sports.server.query.dto.response.RecordResponse;
import com.sports.server.query.dto.response.ReplacementRecordResponse;
import com.sports.server.query.dto.response.ScoreRecordResponse;
import com.sports.server.query.dto.response.TimelineResponse;
import com.sports.server.support.AcceptanceTest;
import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;

@Sql(scripts = "/timeline-fixture.sql")
public class TimelineQueryAcceptanceTest extends AcceptanceTest {


    private static final String QUARTER1 = "1쿼터";
    private static final String QUARTER2 = "2쿼터";

    private static final String TEAM_A = "팀A";
    public static final String TEAM_A_IMAGE_URL = "http://example.com/logo_a.png";
    private static final String TEAM_B = "팀B";
    public static final String TEAM_B_IMAGE_URL = "http://example.com/logo_b.png";

    private static final String SCORE_TYPE = "SCORE";
    private static final String REPLACEMENT_TYPE = "REPLACEMENT";
    private static final String PROGRESS_TYPE = "GAME_PROGRESS";
    private static final String PK_TYPE = "PK";

    @Test
    void 게임의_타임라인을_조회한다() {
        // given
        Long baseballId = 1L;

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .when()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .get("/games/{gameId}/timeline", baseballId)
                .then().log().all()
                .extract();

        // then
        List<TimelineResponse> actual = toResponses(response, TimelineResponse.class);
        assertAll(
                () -> assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value()),

                () -> assertThat(actual).isEqualTo(List.of(
                        new TimelineResponse(
                                "2쿼터", List.of(
                                new RecordResponse(
                                        null, 6L, "GAME_PROGRESS",
                                        20,
                                        null,
                                        null,
                                        null,
                                        null,
                                        null,
                                        null,
                                        new ProgressRecordResponse(GameProgressType.GAME_END),
                                        null
                                ),
                                new RecordResponse(
                                        null, 5L, "SCORE",
                                        13,
                                        "선수10",
                                        2L,
                                        "팀B",
                                        "http://example.com/logo_b.png",
                                        new ScoreRecordResponse(5L, 3, List.of(
                                                new ScoreRecordResponse.Snapshot(
                                                        "팀A", "http://example.com/logo_a.png", 2),
                                                new ScoreRecordResponse.Snapshot(
                                                        "팀B", "http://example.com/logo_b.png", 3)
                                        )),
                                        null,
                                        null, null
                                ),
                                new RecordResponse(
                                        null, 7L, "PK",
                                        10,
                                        "선수10",
                                        2L,
                                        "팀B",
                                        "http://example.com/logo_b.png",
                                        null,
                                        null,
                                        null,
                                        new PkRecordResponse(7L, true)
                                ),
                                new RecordResponse(
                                        null, 4L, "REPLACEMENT",
                                        10,
                                        "선수2",
                                        1L,
                                        "팀A",
                                        "http://example.com/logo_a.png",
                                        null,
                                        new ReplacementRecordResponse(4L, "선수3"),
                                        null, null
                                )
                        )),
                        new TimelineResponse(
                                "1쿼터", List.of(
                                new RecordResponse(
                                        null, 3L, "REPLACEMENT",
                                        24,
                                        "선수6",
                                        2L,
                                        "팀B",
                                        "http://example.com/logo_b.png",
                                        null,
                                        new ReplacementRecordResponse(3L, "선수7"),
                                        null, null
                                ),
                                new RecordResponse(
                                        null, 2L, "SCORE",
                                        22,
                                        "선수2",
                                        1L,
                                        "팀A",
                                        "http://example.com/logo_a.png",
                                        new ScoreRecordResponse(2L, 2, List.of(
                                                new ScoreRecordResponse.Snapshot(
                                                        "팀A", "http://example.com/logo_a.png", 2),
                                                new ScoreRecordResponse.Snapshot(
                                                        "팀B", "http://example.com/logo_b.png", 0)
                                        )),
                                        null,
                                        null, null
                                ),
                                new RecordResponse(
                                        null, 1L, "GAME_PROGRESS",
                                        0,
                                        null,
                                        null,
                                        null,
                                        null,
                                        null,
                                        null,
                                        new ProgressRecordResponse(GameProgressType.QUARTER_START), null
                                )
                        ))
                ))
        );
    }

}
