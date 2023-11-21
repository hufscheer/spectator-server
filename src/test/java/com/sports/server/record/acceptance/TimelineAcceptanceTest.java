package com.sports.server.record.acceptance;

import com.sports.server.record.dto.response.TimelineResponse;
import com.sports.server.support.AcceptanceTest;
import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

public class TimelineAcceptanceTest extends AcceptanceTest {

    private static final List<TimelineResponse.RecordResponse> EXPECTED_RECORDS_3QUARTER = List.of(
            new TimelineResponse.RecordResponse(39, "선수 5", "팀 A", 3),
            new TimelineResponse.RecordResponse(25, "선수 9", "팀 B", 2),
            new TimelineResponse.RecordResponse(1, "선수 4", "팀 A", 3)
    );

    private static final List<TimelineResponse.RecordResponse> EXPECTED_RECORDS_2QUARTER = List.of(
            new TimelineResponse.RecordResponse(30, "선수 5", "팀 A", 3),
            new TimelineResponse.RecordResponse(20, "선수 10", "팀 B", 2),
            new TimelineResponse.RecordResponse(5, "선수 1", "팀 A", 2)
    );

    private static final List<TimelineResponse.RecordResponse> EXPECTED_RECORDS_1QUARTER = List.of(
            new TimelineResponse.RecordResponse(14, "선수 2", "팀 A", 3),
            new TimelineResponse.RecordResponse(10, "선수 6", "팀 B", 2),
            new TimelineResponse.RecordResponse(3, "선수 3", "팀 A", 2)
    );

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
