package com.sports.server.command.timeline.acceptance;

import static org.assertj.core.api.Assertions.assertThat;

import com.sports.server.command.timeline.domain.GameProgressType;
import com.sports.server.command.timeline.domain.WarningCardType;
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

@Sql(scripts = "/timeline-fixture.sql")
public class TimelineAcceptanceTest extends AcceptanceTest {
    private final Long gameId = 1L;
    private final Long quarterId = 3L;
    private final Long team1Id = 1L;
    private final Long team1PlayerId = 1L;

    @BeforeEach
    void configureAuth() {
        configureMockJwtForEmail(MOCK_EMAIL);
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

    @Test
    void 진행_타임라인을_생성한다() {
        // given
        TimelineRequest.RegisterProgress request = new TimelineRequest.RegisterProgress(
                10,
                quarterId,
                GameProgressType.QUARTER_START
        );

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .when()
                .cookie(COOKIE_NAME, mockToken)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request)
                .post("/games/{gameId}/timelines/progress", gameId)
                .then().log().all()
                .extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value());
    }

    @Test
    void 승부차기_타임라인을_생성한다() {
        // given
        TimelineRequest.RegisterPk request = new TimelineRequest.RegisterPk(
                10,
                quarterId,
                team1Id,
                team1PlayerId,
                true
        );

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .when()
                .cookie(COOKIE_NAME, mockToken)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request)
                .post("/games/{gameId}/timelines/pk", gameId)
                .then().log().all()
                .extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value());
    }

    @Test
    void 경고카드_타임라인을_생성한다(){
        //given
        TimelineRequest.RegisterWarningCard request = new TimelineRequest.RegisterWarningCard(
                10,
                quarterId,
                team1Id,
                team1PlayerId,
                WarningCardType.YELLOW
        );

        //when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .when()
                .cookie(COOKIE_NAME, mockToken)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request)
                .post("/games/{gameId}/timelines/warning-card", gameId)
                .then().log().all()
                .extract();

        //then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value());
    }
}
