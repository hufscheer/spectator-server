package com.sports.server.query.acceptance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.sports.server.query.dto.response.CheerTalkResponse;
import com.sports.server.query.dto.response.CheerTalkResponse.ForManager;
import com.sports.server.support.AcceptanceTest;
import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;

@Sql(scripts = "/cheer-talk-fixture.sql")
class CheerTalkQueryAcceptanceTest extends AcceptanceTest {

    @DisplayName("게임의 응원톡을 조회할 때")
    @Nested
    class GetCommentsTest {

        @Test
        void 커서와_사이즈가_없으면_최신_10개가_조회된다() {
            // given
            Long gameId = 1L;

            // when
            ExtractableResponse<Response> response = RestAssured.given().log().all()
                    .when()
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .get("/games/{gameId}/cheer-talks", gameId)
                    .then().log().all()
                    .extract();

            // then
            List<CheerTalkResponse.ForSpectator> actual = toResponses(response, CheerTalkResponse.ForSpectator.class);
            assertAll(
                    () -> assertThat(actual).hasSize(10),
                    () -> assertThat(actual.get(0))
                            .isEqualTo(new CheerTalkResponse.ForSpectator(
                                    14L,
                                    null,
                                    1L,
                                    LocalDateTime.of(2023, 1, 2, 16, 0, 0),
                                    true
                            )),
                    () -> assertThat(actual)
                            .map(CheerTalkResponse.ForSpectator::cheerTalkId)
                            .containsExactly(14L, 15L, 16L, 17L, 18L, 19L, 20L, 21L, 22L, 23L)
            );
        }

        @Test
        void 커서가_있으면_커서_다음부터_10개가_조회된다() {
            // given
            Long gameId = 1L;
            Long cursor = 20L;

            // when
            ExtractableResponse<Response> response = RestAssured.given().log().all()
                    .when()
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .queryParam("cursor", cursor)
                    .get("/games/{gameId}/cheer-talks", gameId)
                    .then().log().all()
                    .extract();

            // then
            List<CheerTalkResponse.ForSpectator> actual = toResponses(response, CheerTalkResponse.ForSpectator.class);
            assertAll(
                    () -> assertThat(actual).hasSize(10),

                    () -> assertThat(actual)
                            .map(CheerTalkResponse.ForSpectator::cheerTalkId)
                            .containsExactly(6L, 8L, 12L, 13L, 14L, 15L, 16L, 17L, 18L, 19L)
            );
        }

        @Test
        void 사이즈만큼_조회된다() {
            // given
            Long gameId = 1L;
            int size = 5;

            // when
            ExtractableResponse<Response> response = RestAssured.given().log().all()
                    .when()
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .queryParam("size", size)
                    .get("/games/{gameId}/cheer-talks", gameId)
                    .then().log().all()
                    .extract();

            // then
            List<CheerTalkResponse.ForSpectator> actual = toResponses(response, CheerTalkResponse.ForSpectator.class);
            assertAll(
                    () -> assertThat(actual).hasSize(size),

                    () -> assertThat(actual)
                            .map(CheerTalkResponse.ForSpectator::cheerTalkId)
                            .containsExactly( 19L, 20L, 21L, 22L, 23L)
            );
        }

        @Test
        void 커서_다음부터_사이즈만큼_조회된다() {
            // given
            Long gameId = 1L;
            int size = 4;
            Long cursor = 8L;

            // when
            ExtractableResponse<Response> response = RestAssured.given().log().all()
                    .when()
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .queryParam("size", size)
                    .queryParam("cursor", cursor)
                    .get("/games/{gameId}/cheer-talks", gameId)
                    .then().log().all()
                    .extract();

            // then
            List<CheerTalkResponse.ForSpectator> actual = toResponses(response, CheerTalkResponse.ForSpectator.class);
            assertAll(
                    () -> assertThat(actual).hasSize(size),

                    () -> assertThat(actual)
                            .map(CheerTalkResponse.ForSpectator::cheerTalkId)
                            .containsExactly(1L, 4L, 5L, 6L)
            );
        }

        @Test
        void 블락된_응원톡은_null을_표시한다() {
            // given
            Long gameId = 1L;

            // when
            ExtractableResponse<Response> response = RestAssured.given().log().all()
                    .when()
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .get("/games/{gameId}/cheer-talks", gameId)
                    .then().log().all()
                    .extract();

            // then
            List<CheerTalkResponse.ForSpectator> actual = toResponses(response, CheerTalkResponse.ForSpectator.class);
            assertThat(actual)
                    .filteredOn(CheerTalkResponse.ForSpectator::isBlocked)
                    .map(CheerTalkResponse.ForSpectator::content)
                    .containsOnlyNulls();
        }
    }

    @Test
    void 리그의_신고된_응원톡을_조회한다() {

        // given
        Long leagueId = 1L;

        configureMockJwtForEmail(MOCK_EMAIL);

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .when()
                .cookie(COOKIE_NAME, mockToken)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .get("/leagues/{leagueId}/cheer-talks/reported", leagueId)
                .then().log().all()
                .extract();

        // then
        List<CheerTalkResponse.ForManager> actual = toResponses(response, ForManager.class);
        assertAll(
                () -> assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value()),
                () -> assertThat(actual)
                        .map(CheerTalkResponse.ForManager::cheerTalkId)
                        .containsExactly(18L, 1L),
                () -> assertThat(actual)
                        .map(CheerTalkResponse.ForManager::content)
                        .containsExactly("응원톡18", "응원톡1")
        );
    }

    @Test
    void 리그의_차단되지_않은_응원톡을_모두_조회한다() {

        // given
        Long leagueId = 1L;

        configureMockJwtForEmail(MOCK_EMAIL);

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .when()
                .cookie(COOKIE_NAME, mockToken)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .get("/leagues/{leagueId}/cheer-talks", leagueId)
                .then().log().all()
                .extract();

        // then
        List<CheerTalkResponse.ForManager> actual = toResponses(response, ForManager.class);
        assertAll(
                () -> assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value()),
                () -> assertThat(actual)
                        .map(CheerTalkResponse.ForManager::cheerTalkId)
                        .containsExactly(22L, 21L, 20L, 19L, 18L, 17L, 16L, 15L, 13L, 12L),
                () -> assertThat(actual)
                        .map(CheerTalkResponse.ForManager::content)
                        .containsExactly(
                                "응원톡22", "응원톡21", "응원톡20", "응원톡19", "응원톡18", "응원톡17", "응원톡16", "응원톡15", "응원톡13", "응원톡12"),
                () -> assertThat(actual)
                        .map(CheerTalkResponse.ForManager::isBlocked)
                        .containsOnly(false)
        );
    }

    @Test
    void 리그의_가려진_응원톡을_조회한다() {
    	// given
    	Long leagueId = 1L;

        configureMockJwtForEmail(MOCK_EMAIL);

    	// when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
            .when()
            .cookie(COOKIE_NAME, mockToken)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .get("/leagues/{leagueId}/cheer-talks/blocked", leagueId)
            .then().log().all()
            .extract();

        // then
        List<CheerTalkResponse.ForManager> actual = toResponses(response, CheerTalkResponse.ForManager.class);
        assertAll(
            () -> assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value()),
            () -> assertThat(actual).map(CheerTalkResponse.ForManager::cheerTalkId)
                .containsExactly( 23L, 14L),
            () -> assertThat(actual).map(CheerTalkResponse.ForManager::content).containsExactly("응원톡23", "블락된 응원톡")
        );
    }

    @Test
    void 모든_차단되지_않은_응원톡을_조회한다() {
        // given
        configureMockJwtForEmail("john.doe@example.com");

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .cookie(COOKIE_NAME, mockToken)
                .when()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .get("/cheer-talks")
                .then().log().all()
                .extract();

        // then
        List<CheerTalkResponse.ForManager> actual = toResponses(response, ForManager.class);
        assertAll(
                () -> assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value()),
                () -> assertThat(actual)
                        .map(CheerTalkResponse.ForManager::isBlocked)
                        .containsOnly(false)
        );
    }

    @Test
    void 모든_차단된_응원톡을_조회한다() {
        // given
        configureMockJwtForEmail("john.doe@example.com");

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .cookie(COOKIE_NAME, mockToken)
                .when()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .get("/cheer-talks/blocked")
                .then().log().all()
                .extract();

        // then
        List<CheerTalkResponse.ForManager> actual = toResponses(response, ForManager.class);
        assertAll(
                () -> assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value()),
                () -> assertThat(actual)
                        .map(CheerTalkResponse.ForManager::isBlocked)
                        .containsOnly(true)
        );
    }
}
