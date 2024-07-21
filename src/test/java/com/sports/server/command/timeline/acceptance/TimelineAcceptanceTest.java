package com.sports.server.command.timeline.acceptance;

import com.sports.server.command.timeline.TimelineDto;
import com.sports.server.support.AcceptanceTest;
import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;

import static org.assertj.core.api.Assertions.assertThat;

@Sql(scripts = "/timeline-fixture.sql")
public class TimelineAcceptanceTest extends AcceptanceTest {
    private final Long gameId = 1L;
    private final Long quarterId = 3L;

    @Test
    void 득점_타임라인을_생성한다() {
        // given
        Long team1Id = 1L;
        Long team1PlayerId = 1L;

        TimelineDto.RegisterScore request = new TimelineDto.RegisterScore(
                team1Id,
                quarterId,
                team1PlayerId,
                3
        );

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .when()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request)
                .post("/games/{gameId}/timelines/score", gameId)
                .then().log().all()
                .extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value());
    }
}
