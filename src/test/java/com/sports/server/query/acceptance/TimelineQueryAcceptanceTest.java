package com.sports.server.query.acceptance;

import com.sports.server.query.dto.response.RecordResponse;
import com.sports.server.query.dto.response.ReplacementRecordResponse;
import com.sports.server.query.dto.response.ScoreRecordResponse;
import com.sports.server.query.dto.response.TimelineResponse;
import com.sports.server.support.AcceptanceTest;
import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@Sql(scripts = "/record-fixture.sql")
public class TimelineQueryAcceptanceTest extends AcceptanceTest {


    private static final String QUARTER1 = "1쿼터";
    private static final String QUARTER2 = "2쿼터";

    private static final String TEAM_A = "팀A";
    public static final String TEAM_A_IMAGE_URL = "http://example.com/logo_a.png";
    private static final String TEAM_B = "팀B";
    public static final String TEAM_B_IMAGE_URL = "http://example.com/logo_b.png";

    private static final String SCORE_TYPE = "SCORE";
    private static final String REPLACEMENT_TYPE = "REPLACEMENT";

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
                                QUARTER2, List.of(
                                new RecordResponse(
                                        null, SCORE_TYPE,
                                        13,
                                        "선수10",
                                        TEAM_B,
                                        TEAM_B_IMAGE_URL,
                                        new ScoreRecordResponse(3, List.of(
                                                new ScoreRecordResponse.History(
                                                        TEAM_A, TEAM_A_IMAGE_URL, 2),
                                                new ScoreRecordResponse.History(
                                                        TEAM_B, TEAM_B_IMAGE_URL, 3)
                                        )),
                                        null
                                ),
                                new RecordResponse(
                                        null, REPLACEMENT_TYPE,
                                        10,
                                        "선수2",
                                        TEAM_A,
                                        TEAM_A_IMAGE_URL,
                                        null,
                                        new ReplacementRecordResponse("선수3")
                                )
                        )),
                        new TimelineResponse(
                                QUARTER1, List.of(
                                new RecordResponse(
                                        null, REPLACEMENT_TYPE,
                                        4,
                                        "선수6",
                                        TEAM_B,
                                        TEAM_B_IMAGE_URL,
                                        null,
                                        new ReplacementRecordResponse("선수7")
                                ),
                                new RecordResponse(
                                        null, SCORE_TYPE,
                                        2,
                                        "선수2",
                                        TEAM_A,
                                        TEAM_A_IMAGE_URL,
                                        new ScoreRecordResponse(2, List.of(
                                                new ScoreRecordResponse.History(
                                                        TEAM_A, TEAM_A_IMAGE_URL, 2),
                                                new ScoreRecordResponse.History(
                                                        TEAM_B, TEAM_B_IMAGE_URL, 0)
                                        )),
                                        null
                                )
                        ))
                ))
        );
    }
}