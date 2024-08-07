package com.sports.server.command.timeline.acceptance;

import com.sports.server.command.timeline.dto.TimelineRequest;
import com.sports.server.support.AcceptanceTest;
import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;

import static org.assertj.core.api.Assertions.assertThat;

@Sql(scripts = "/timeline-fixture.sql")
public class TimelineAcceptanceTest extends AcceptanceTest {
    private final Long gameId = 1L;
    private final Long quarterId = 3L;
    private final Long team1Id = 1L;
    private final Long team1PlayerId = 1L;

    @BeforeEach
    void configureAuth() {
        configureMockJwtForEmail("john.doe@example.com");
    }

    @Test
    void 득점_타임라인을_생성한다() {
        TimelineRequest.RegisterScore request = new TimelineRequest.RegisterScore(
                team1Id,
                quarterId,
                team1PlayerId,
                3
        );

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .when()
                .cookie(COOKIE_NAME, mockToken)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request)
                .post("/games/{gameId}/timelines/score", gameId)
                .then().log().all()
                .extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value());
    }

    @Test
    void 교체_타임라인을_생성한다() {
        // given
        long replacedPlayerId = 2L;
        TimelineRequest.RegisterReplacement request = new TimelineRequest.RegisterReplacement(
                team1Id,
                quarterId,
                team1PlayerId,
                replacedPlayerId,
                10
        );

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .when()
                .cookie(COOKIE_NAME, mockToken)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request)
                .post("/games/{gameId}/timelines/replacement", gameId)
                .then().log().all()
                .extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value());
    }
}
