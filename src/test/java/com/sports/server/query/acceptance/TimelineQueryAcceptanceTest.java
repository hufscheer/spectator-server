package com.sports.server.query.acceptance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.sports.server.query.dto.response.TimelineResponse;
import com.sports.server.support.AcceptanceTest;
import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;

@Sql(scripts = "/record-fixture.sql")
public class TimelineQueryAcceptanceTest extends AcceptanceTest {

    private static final List<TimelineResponse.RecordResponse> EXPECTED_RECORDS_3QUARTER = List.of(
            new TimelineResponse.RecordResponse(39, "선수5", "팀 A", 3),
            new TimelineResponse.RecordResponse(25, "선수9", "팀 B", 2),
            new TimelineResponse.RecordResponse(1, "선수4", "팀 A", 3)
    );

    private static final List<TimelineResponse.RecordResponse> EXPECTED_RECORDS_2QUARTER = List.of(
            new TimelineResponse.RecordResponse(30, "선수5", "팀 A", 3),
            new TimelineResponse.RecordResponse(20, "선수10", "팀 B", 2),
            new TimelineResponse.RecordResponse(5, "선수1", "팀 A", 2)
    );

    private static final List<TimelineResponse.RecordResponse> EXPECTED_RECORDS_1QUARTER = List.of(
            new TimelineResponse.RecordResponse(14, "선수2", "팀 A", 3),
            new TimelineResponse.RecordResponse(10, "선수6", "팀 B", 2),
            new TimelineResponse.RecordResponse(3, "선수3", "팀 A", 2)
    );

    @Disabled
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
        Map<String, List<TimelineResponse>> groupByQuarter = actual.stream()
                .collect(Collectors.groupingBy(TimelineResponse::gameQuarter));
        assertAll(
                () -> assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value()),
                () -> assertThat(actual)
                        .map(TimelineResponse::gameQuarter)
                        .containsExactly("3쿼터", "2쿼터", "1쿼터"),

                () -> assertThat(groupByQuarter.get("3쿼터"))
                        .map(TimelineResponse::records)
                        .containsExactly(EXPECTED_RECORDS_3QUARTER),
                () -> assertThat(groupByQuarter.get("2쿼터"))
                        .map(TimelineResponse::records)
                        .containsExactly(EXPECTED_RECORDS_2QUARTER),
                () -> assertThat(groupByQuarter.get("1쿼터"))
                        .map(TimelineResponse::records)
                        .containsExactly(EXPECTED_RECORDS_1QUARTER)
        );
    }
}
