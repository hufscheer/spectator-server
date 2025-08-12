package com.sports.server.command.cheertalk.acceptance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.sports.server.command.cheertalk.dto.CheerTalkRequest;
import com.sports.server.support.AcceptanceTest;
import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

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
    void 응원톡을_가린다() {
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
        // todo : 가려진 응원톡 전체조회 기능 머지 후 테스트 추가 예정
//        ExtractableResponse<Response> getResponse = RestAssured.given().log().all()
//                .when()
//                .contentType(MediaType.APPLICATION_JSON_VALUE)
//                .get("")
//                .then().log().all()
//                .extract();
        assertAll(
                () -> assertThat(patchResponse.statusCode()).isEqualTo(HttpStatus.OK.value())
        );
    }

    @Test
    void 응원톡을_가리기_취소한다() {
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
        // 리그의 응원톡 전체조회 기능 머지 후 테스트 추가 예정
//        ExtractableResponse<Response> getResponse = RestAssured.given().log().all()
//                .when()
//                .contentType(MediaType.APPLICATION_JSON_VALUE)
//                .get("/leagues/{leagueId}/cheer-talks", leagueId)
//                .then().log().all()
//                .extract();
        assertAll(
                () -> assertThat(patchResponse.statusCode()).isEqualTo(HttpStatus.OK.value())
        );
    }
}
