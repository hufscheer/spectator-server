package com.sports.server.query.acceptance;

import static org.assertj.core.api.Assertions.assertThat;

import com.sports.server.command.game.exception.GameErrorMessages;
import com.sports.server.support.AcceptanceTest;
import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.jdbc.Sql;

@Sql(scripts = "/cheer-talk-fixture.sql")
@Sql(statements = "UPDATE leagues SET is_deleted = 1 WHERE id = 1")
class DeletedLeagueGameQueryAcceptanceTest extends AcceptanceTest {

    private static final long DELETED_LEAGUE_GAME_ID = 1L;
    private static final long ACTIVE_LEAGUE_GAME_ID = 2L;

    @BeforeEach
    void authenticate() {
        configureMockJwtForEmail(MOCK_EMAIL);
    }

    @DisplayName("삭제된 대회의 경기는 /games/{id}/** 조회가 모두 경기 단건 조회와 같은 404 이다")
    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = {
            "video", "cheer", "lineup", "lineup/playing",
            "timeline", "available-progress", "quarter-scores",
            "cheer-talks", "cheer-talks/reported", "cheer-talks/blocked"
    })
    void 삭제된_대회의_경기는_404_이다(String subPath) {
        // when
        ExtractableResponse<Response> response = RestAssured.given()
                .cookie(COOKIE_NAME, mockToken)
                .when()
                .get("/games/{gameId}/" + subPath, DELETED_LEAGUE_GAME_ID)
                .then().log().ifValidationFails()
                .extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(response.jsonPath().getString("message"))
                .isEqualTo(GameErrorMessages.GAME_NOT_FOUND_EXCEPTION);
    }

    @Test
    void 경기_단건_조회도_같은_404_이다() {
        ExtractableResponse<Response> response = RestAssured.given()
                .when()
                .get("/games/{gameId}", DELETED_LEAGUE_GAME_ID)
                .then().extract();

        assertThat(response.statusCode()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(response.jsonPath().getString("message"))
                .isEqualTo(GameErrorMessages.GAME_NOT_FOUND_EXCEPTION);
    }

    @DisplayName("살아 있는 대회의 경기는 계속 200 이다")
    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = {
            "video", "cheer", "lineup", "lineup/playing",
            "timeline", "available-progress", "quarter-scores", "cheer-talks"
    })
    void 살아있는_대회의_경기는_200_이다(String subPath) {
        ExtractableResponse<Response> response = RestAssured.given()
                .when()
                .get("/games/{gameId}/" + subPath, ACTIVE_LEAGUE_GAME_ID)
                .then().log().ifValidationFails()
                .extract();

        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    }
}
