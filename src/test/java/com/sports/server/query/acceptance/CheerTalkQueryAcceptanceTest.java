package com.sports.server.query.acceptance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.sports.server.query.dto.response.CheerTalkResponse;
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
                                    5L,
                                    "응원톡5",
                                    1L,
                                    LocalDateTime.of(2023, 1, 2, 14, 55, 0),
                                    false
                            )),
                    () -> assertThat(actual)
                            .map(CheerTalkResponse.ForSpectator::cheerTalkId)
                            .containsExactly(5L, 6L, 7L, 8L, 9L, 10L, 11L, 12L, 13L, 14L)
            );
        }

        @Test
        void 커서가_있으면_커서_다음부터_10개가_조회된다() {
            // given
            Long gameId = 1L;
            Long cursor = 12L;

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
                            .containsExactly(2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L, 11L)
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
                            .containsExactly(10L, 11L, 12L, 13L, 14L)
            );
        }

        @Test
        void 커서_다음부터_사이즈만큼_조회된다() {
            // given
            Long gameId = 1L;
            int size = 5;
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
                            .containsExactly(3L, 4L, 5L, 6L, 7L)
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
        List<CheerTalkResponse.Reported> actual = toResponses(response, CheerTalkResponse.Reported.class);
        assertAll(
                () -> assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value()),
                () -> assertThat(actual)
                        .map(CheerTalkResponse.Reported::cheerTalkId)
                        .containsExactly(1L),
                () -> assertThat(actual)
                        .map(CheerTalkResponse.Reported::content)
                        .containsExactly("응원톡1")
        );
    }

    @Test
    void 리그의_가려진_응원톡을_조회한다() throws Exception {
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
        List<CheerTalkResponse.Blocked> actual = toResponses(response, CheerTalkResponse.Blocked.class);
        assertAll(
            () -> assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value()),
            () -> assertThat(actual).map(CheerTalkResponse.Blocked::cheerTalkId)
                .containsExactly(19L, 14L),
            () -> assertThat(actual).map(CheerTalkResponse.Blocked::content).containsExactly("응원톡17", "블락된 응원톡")
        );
    }
}
