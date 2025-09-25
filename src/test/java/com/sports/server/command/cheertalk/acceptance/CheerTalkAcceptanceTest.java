package com.sports.server.command.cheertalk.acceptance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.sports.server.command.cheertalk.dto.CheerTalkRequest;
import com.sports.server.query.dto.response.CheerTalkResponse;
import com.sports.server.support.AcceptanceTest;
import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

@Sql("/cheer-talk-fixture.sql")
public class CheerTalkAcceptanceTest extends AcceptanceTest {

    @Test
    void 새로운_응원톡이_저장된다() {
        // given
        String content = "파이팅!";
        Long gameTeamId = 1L;
        CheerTalkRequest cheerTalkRequest = new CheerTalkRequest(content, gameTeamId);

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .when()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(cheerTalkRequest)
                .post("/cheer-talks")
                .then().log().all()
                .extract();

        //then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    void 리그_응원톡을_가린다() {
        // given
        Long leagueId = 1L;
        Long cheerTalkId = 1L;

        configureMockJwtForEmail(MOCK_EMAIL);

        // when
        ExtractableResponse<Response> patchResponse = RestAssured.given().log().all()
                .when()
                .cookie(COOKIE_NAME, mockToken)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .patch("/cheer-talks/{leagueId}/{cheerTalkId}/block", leagueId, cheerTalkId)
                .then().log().all()
                .extract();

        // then
        // 차단 후 해당 응원톡이 차단된 응원톡 목록에 포함되는지 확인
        ExtractableResponse<Response> getResponse = RestAssured.given().log().all()
                .when()
                .cookie(COOKIE_NAME, mockToken)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .get("/leagues/{leagueId}/cheer-talks/blocked", leagueId)
                .then().log().all()
                .extract();

        List<CheerTalkResponse.ForManager> blockedCheerTalks = toResponses(getResponse, CheerTalkResponse.ForManager.class);

        // then
        assertAll(
                () -> assertThat(patchResponse.statusCode()).isEqualTo(HttpStatus.OK.value()),
                () -> assertThat(getResponse.statusCode()).isEqualTo(HttpStatus.OK.value()),
                () -> assertThat(blockedCheerTalks)
                        .map(CheerTalkResponse.ForManager::cheerTalkId)
                        .contains(cheerTalkId)
        );
    }

    @Test
    void 리그_응원톡을_가리기_취소한다() {
        // given
        Long leagueId = 1L;
        Long cheerTalkId = 14L;

        configureMockJwtForEmail(MOCK_EMAIL);

        // when
        ExtractableResponse<Response> patchResponse = RestAssured.given().log().all()
                .when()
                .cookie(COOKIE_NAME, mockToken)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .patch("/cheer-talks/{leagueId}/{cheerTalkId}/unblock", leagueId, cheerTalkId)
                .then().log().all()
                .extract();

        // then
        // 차단 해제 후 해당 응원톡이 차단되지 않은 응원톡 목록에 포함되는지 확인
        ExtractableResponse<Response> getResponse = RestAssured.given().log().all()
                .when()
                .cookie(COOKIE_NAME, mockToken)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .get("/leagues/{leagueId}/cheer-talks", leagueId)
                .then().log().all()
                .extract();

        List<CheerTalkResponse.ForManager> unblockedCheerTalks = toResponses(getResponse, CheerTalkResponse.ForManager.class);

        assertAll(
                () -> assertThat(patchResponse.statusCode()).isEqualTo(HttpStatus.OK.value()),
                () -> assertThat(getResponse.statusCode()).isEqualTo(HttpStatus.OK.value()),
                () -> assertThat(unblockedCheerTalks)
                        .map(CheerTalkResponse.ForManager::cheerTalkId)
                        .contains(cheerTalkId),
                () -> assertThat(unblockedCheerTalks)
                        .map(CheerTalkResponse.ForManager::isBlocked)
                        .containsOnly(false)
        );
    }

    @Test
    void 응원톡을_차단한다() {
        // given
        Long cheerTalkId = 1L;
        configureMockJwtForEmail(MOCK_EMAIL);

        // when
        ExtractableResponse<Response> patchResponse = RestAssured.given().log().all()
                .cookie(COOKIE_NAME, mockToken)
                .when()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .patch("/cheer-talks/{cheerTalkId}/block", cheerTalkId)
                .then().log().all()
                .extract();

        // then
        // 차단 후 해당 응원톡이 전체 차단된 응원톡 목록에 포함되는지 확인
        ExtractableResponse<Response> getResponse = RestAssured.given().log().all()
                .when()
                .cookie(COOKIE_NAME, mockToken)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .get("/cheer-talks/blocked")
                .then().log().all()
                .extract();

        List<CheerTalkResponse.ForManager> blockedCheerTalks = toResponses(getResponse, CheerTalkResponse.ForManager.class);

        assertAll(
                () -> assertThat(patchResponse.statusCode()).isEqualTo(HttpStatus.OK.value()),
                () -> assertThat(getResponse.statusCode()).isEqualTo(HttpStatus.OK.value()),
                () -> assertThat(blockedCheerTalks)
                        .map(CheerTalkResponse.ForManager::cheerTalkId)
                        .contains(cheerTalkId),
                () -> assertThat(blockedCheerTalks)
                        .map(CheerTalkResponse.ForManager::isBlocked)
                        .containsOnly(true)
        );
    }

    @Test
    void 응원톡_차단을_해제한다() {
        // given
        Long cheerTalkId = 14L;
        configureMockJwtForEmail(MOCK_EMAIL);

        // when
        ExtractableResponse<Response> patchResponse = RestAssured.given().log().all()
                .when()
                .cookie(COOKIE_NAME, mockToken)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .patch("/cheer-talks/{cheerTalkId}/unblock", cheerTalkId)
                .then().log().all()
                .extract();

        // then
        // 차단 해제 후 해당 응원톡이 전체 차단되지 않은 응원톡 목록에 포함되는지 확인
        ExtractableResponse<Response> getResponse = RestAssured.given().log().all()
                .when()
                .cookie(COOKIE_NAME, mockToken)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .get("/cheer-talks")
                .then().log().all()
                .extract();

        List<CheerTalkResponse.ForManager> unblockedCheerTalks = toResponses(getResponse, CheerTalkResponse.ForManager.class);

        assertAll(
                () -> assertThat(patchResponse.statusCode()).isEqualTo(HttpStatus.OK.value()),
                () -> assertThat(getResponse.statusCode()).isEqualTo(HttpStatus.OK.value()),
                () -> assertThat(unblockedCheerTalks)
                        .map(CheerTalkResponse.ForManager::cheerTalkId)
                        .contains(cheerTalkId),
                () -> assertThat(unblockedCheerTalks)
                        .map(CheerTalkResponse.ForManager::isBlocked)
                        .containsOnly(false)
        );
    }
}
