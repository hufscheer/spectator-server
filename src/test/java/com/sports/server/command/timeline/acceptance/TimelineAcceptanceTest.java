package com.sports.server.command.timeline.acceptance;

import static org.assertj.core.api.Assertions.assertThat;

import com.sports.server.command.timeline.domain.GameProgressType;
import com.sports.server.command.league.domain.SportType;
import com.sports.server.command.league.domain.SoccerQuarter;
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
    private final Long team1Id = 1L;
    private final Long team1PlayerId = 1L;

    @BeforeEach
    void configureAuth() {
        configureMockJwtForEmail(MOCK_EMAIL);
    }

    @Test
    void 득점_타임라인을_생성한다() {
        TimelineRequest.RegisterScore request = new TimelineRequest.RegisterScore(
                team1Id, SportType.SOCCER, SoccerQuarter.FIRST_HALF.name(),
                team1PlayerId,
                3,
                null
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
    void 어시스트와_함께_득점_타임라인을_생성한다() {
        // given
        long assistPlayerId = 2L; // 팀1 소속 선수

        TimelineRequest.RegisterScore request = new TimelineRequest.RegisterScore(
                team1Id, SportType.SOCCER, SoccerQuarter.FIRST_HALF.name(),
                team1PlayerId,
                3,
                assistPlayerId
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
    void 다른_팀_선수를_어시스트로_등록하면_400을_반환한다() {
        // given
        long team2PlayerId = 6L; // 팀2 소속 선수 (팀1 득점 타임라인에 어시스트로 등록 시도)

        TimelineRequest.RegisterScore request = new TimelineRequest.RegisterScore(
                team1Id, SportType.SOCCER, SoccerQuarter.FIRST_HALF.name(),
                team1PlayerId,
                3,
                team2PlayerId
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
        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void 교체_타임라인을_생성한다() {
        // given
        long replacedPlayerId = 2L;
        TimelineRequest.RegisterReplacement request = new TimelineRequest.RegisterReplacement(
                team1Id, SportType.SOCCER, SoccerQuarter.FIRST_HALF.name(),
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
                10, SportType.SOCCER, SoccerQuarter.SECOND_HALF.name(),
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
                10, SportType.SOCCER, SoccerQuarter.PENALTY_SHOOTOUT.name(),
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
                10, SportType.SOCCER, SoccerQuarter.FIRST_HALF.name(),
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
