package com.sports.server.query.acceptance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.sports.server.command.league.domain.SoccerQuarter;
import com.sports.server.command.timeline.domain.GameProgressType;
import com.sports.server.support.AcceptanceTest;
import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;

@Sql(scripts = "/progress-transition-fixture.sql")
public class AvailableProgressAcceptanceTest extends AcceptanceTest {

    private static final long GAME_SECOND_HALF_STARTED = 4L;

    @Test
    void 가능한_경기_진행_액션을_조회한다() {
        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .when()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .get("/games/{gameId}/available-progress", GAME_SECOND_HALF_STARTED)
                .then().log().all()
                .extract();

        // then
        assertAll(
                () -> assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value()),
                () -> assertThat(response.jsonPath().getList("availableActions")).hasSize(2),
                () -> assertThat(response.jsonPath().getString("availableActions[0].quarter"))
                        .isEqualTo(SoccerQuarter.SECOND_HALF.name()),
                () -> assertThat(response.jsonPath().getString("availableActions[0].gameProgressType"))
                        .isEqualTo(GameProgressType.QUARTER_END.name()),
                () -> assertThat(response.jsonPath().getString("availableActions[0].displayName"))
                        .isEqualTo("후반전 종료"),
                () -> assertThat(response.jsonPath().getString("availableActions[1].quarter"))
                        .isEqualTo(SoccerQuarter.SECOND_HALF.name()),
                () -> assertThat(response.jsonPath().getString("availableActions[1].gameProgressType"))
                        .isEqualTo(GameProgressType.GAME_END.name()),
                () -> assertThat(response.jsonPath().getString("availableActions[1].displayName"))
                        .isEqualTo("경기 종료")
        );
    }
}
