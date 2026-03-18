package com.sports.server.command.nl.acceptance;

import static org.assertj.core.api.Assertions.assertThat;

import com.sports.server.command.nl.application.NlClient;
import com.sports.server.command.nl.dto.NlParseResult;
import com.sports.server.command.nl.dto.NlParseResult.ParsedPlayer;
import com.sports.server.support.AcceptanceTest;
import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

@Sql(scripts = "/league-fixture.sql")
public class NlAcceptanceTest extends AcceptanceTest {

    @MockBean
    private NlClient nlClient;

    @BeforeEach
    void configureAuth() {
        configureMockJwtForEmail(MOCK_EMAIL);
    }

    @Test
    void 선수_정보를_파싱하여_프리뷰를_반환한다() {
        // given
        given(nlClient.parsePlayers(anyString(), anyList()))
                .willReturn(NlParseResult.ofPlayers(List.of(
                        new ParsedPlayer("홍길동", "202600001", 10),
                        new ParsedPlayer("김철수", "202600002", 7)
                )));

        Map<String, Object> request = Map.of(
                "leagueId", 1,
                "teamId", 1,
                "history", List.of(),
                "message", "홍길동 202600001 10\n김철수 202600002 7"
        );

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .when()
                .cookie(COOKIE_NAME, mockToken)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request)
                .post("/nl/process")
                .then().log().all()
                .extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.jsonPath().getString("preview.type")).isEqualTo("REGISTER_PLAYERS_BULK");
        assertThat(response.jsonPath().getList("preview.players")).hasSize(2);
        assertThat(response.jsonPath().getString("preview.players[0].status")).isEqualTo("NEW");
    }

    @Test
    void 팀_컨텍스트_없이_선수_정보를_파싱한다() {
        // given
        given(nlClient.parsePlayers(anyString(), anyList()))
                .willReturn(NlParseResult.ofPlayers(List.of(
                        new ParsedPlayer("홍길동", "202600001", 10),
                        new ParsedPlayer("김철수", "202600002", 7)
                )));

        Map<String, Object> request = Map.of(
                "history", List.of(),
                "message", "홍길동 202600001 10\n김철수 202600002 7"
        );

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .when()
                .cookie(COOKIE_NAME, mockToken)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request)
                .post("/nl/parse")
                .then().log().all()
                .extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.jsonPath().getList("preview.players")).hasSize(2);
        assertThat(response.jsonPath().getInt("preview.total")).isEqualTo(2);
    }

    @Test
    void 신규_선수를_등록하고_팀에_배정한다() {
        // given
        Map<String, Object> request = Map.of(
                "leagueId", 1,
                "teamId", 1,
                "players", List.of(
                        Map.of("name", "홍길동", "studentNumber", "202600001", "jerseyNumber", 10)
                )
        );

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .when()
                .cookie(COOKIE_NAME, mockToken)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request)
                .post("/nl/execute")
                .then().log().all()
                .extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.jsonPath().getInt("result.created")).isEqualTo(1);
        assertThat(response.jsonPath().getInt("result.assigned")).isEqualTo(1);
    }

    @Test
    void 인증_없이_호출하면_실패한다() {
        // given
        Map<String, Object> request = Map.of(
                "leagueId", 1,
                "teamId", 1,
                "history", List.of(),
                "message", "홍길동 202600001 10"
        );

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .when()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request)
                .post("/nl/process")
                .then().log().all()
                .extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    void 이미_팀에_소속된_선수는_스킵한다() {
        // given: team 1에는 player 3(이현제, 202202001)이 이미 소속
        Map<String, Object> request = Map.of(
                "leagueId", 1,
                "teamId", 1,
                "players", List.of(
                        Map.of("name", "이현제", "studentNumber", "202202001", "jerseyNumber", 9)
                )
        );

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .when()
                .cookie(COOKIE_NAME, mockToken)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request)
                .post("/nl/execute")
                .then().log().all()
                .extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.jsonPath().getInt("result.created")).isEqualTo(0);
        assertThat(response.jsonPath().getInt("result.skipped")).isEqualTo(1);
    }

    @Test
    void 기존_선수를_다른_팀에_배정한다() {
        // given: player 1(진승희, 202101001)은 team 3에 소속, team 1에는 미소속
        given(nlClient.parsePlayers(anyString(), anyList()))
                .willReturn(NlParseResult.ofPlayers(List.of(
                        new ParsedPlayer("진승희", "202101001", 5)
                )));

        Map<String, Object> processRequest = Map.of(
                "leagueId", 1,
                "teamId", 1,
                "history", List.of(),
                "message", "진승희 202101001 5"
        );

        // when - process
        ExtractableResponse<Response> processResponse = RestAssured.given().log().all()
                .when()
                .cookie(COOKIE_NAME, mockToken)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(processRequest)
                .post("/nl/process")
                .then().log().all()
                .extract();

        // then - EXISTS로 분류
        assertThat(processResponse.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(processResponse.jsonPath().getString("preview.players[0].status")).isEqualTo("EXISTS");

        // when - execute
        Map<String, Object> executeRequest = Map.of(
                "leagueId", 1,
                "teamId", 1,
                "players", List.of(
                        Map.of("name", "진승희", "studentNumber", "202101001", "jerseyNumber", 5)
                )
        );

        ExtractableResponse<Response> executeResponse = RestAssured.given().log().all()
                .when()
                .cookie(COOKIE_NAME, mockToken)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(executeRequest)
                .post("/nl/execute")
                .then().log().all()
                .extract();

        // then - 생성 없이 배정만
        assertThat(executeResponse.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(executeResponse.jsonPath().getInt("result.created")).isEqualTo(0);
        assertThat(executeResponse.jsonPath().getInt("result.assigned")).isEqualTo(1);
    }
}
