package com.sports.server.command.league.acceptance;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.sports.server.command.bracket.dto.BracketRequest;
import com.sports.server.command.league.dto.LeagueRequest;
import com.sports.server.support.AcceptanceTest;
import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;

@Sql("/league-fixture.sql")
public class LeagueAcceptanceTest extends AcceptanceTest {

    @Test
    void 대회를_저장한다() {
        // given
        List<Long> teamIds = List.of(4L, 5L);
        BracketRequest.Save bracket = new BracketRequest.Save(4,
                List.of(new BracketRequest.Entry(1, 4L), new BracketRequest.Entry(4, 5L)));
        LeagueRequest.Register request = new LeagueRequest.Register(
                "우물정 제기차기 대회",
                4,
                LocalDateTime.of(2025, 1, 1, 0, 0),
                LocalDateTime.of(2025, 1, 15, 0, 0),
                teamIds,
                null,
                bracket
        , false, null);

        configureMockJwtForEmail("john.doe@example.com");

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .cookie(COOKIE_NAME, mockToken)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request)
                .post("/leagues")
                .then().log().all()
                .extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value());
    }

    @Test
    void 대회를_삭제한다() {
        // given
        Long leagueId = 1L;

        configureMockJwtForEmail("john.doe@example.com");

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .cookie(COOKIE_NAME, mockToken)
                .pathParam("leagueId", leagueId)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .delete("/leagues/{leagueId}")
                .then().log().all()
                .extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    void 대회를_수정한다() {
        // given
        Long leagueId = 1L;
        LeagueRequest.Update request = new LeagueRequest.Update(
                "라임즙 많이 먹기 대회",
                16,
                LocalDateTime.of(24, 12, 11, 0, 0, 0),
                LocalDateTime.of(24, 12, 13, 0, 0, 0), false, null);

        configureMockJwtForEmail(MOCK_EMAIL);

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .cookie(COOKIE_NAME, mockToken)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request)
                .put("/leagues/{leagueId}", leagueId)
                .then().log().all()
                .extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    }

    // 날짜는 NOT NULL 컬럼이다. 요청에서 빠지면 null 로 덮어써 DB 제약 위반(500)이 났다
    @Test
    void 대회를_수정할_때_시작일을_빼면_어떤_값이_빠졌는지_알려준다() {
        // given
        Map<String, Object> request = new HashMap<>();
        request.put("name", "라임즙 많이 먹기 대회");
        request.put("maxRound", 16);
        request.put("endAt", "2024-12-13T00:00:00");
        request.put("thirdPlaceMatchEnabled", false);

        configureMockJwtForEmail(MOCK_EMAIL);

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .cookie(COOKIE_NAME, mockToken)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request)
                .put("/leagues/{leagueId}", 1L)
                .then().log().all()
                .extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.jsonPath().getList("fieldErrors.field", String.class)).isEqualTo(List.of("startAt"));
    }

    // int 라 빠지면 0 이 되어 "해당 라운드는 존재하지 않습니다" 로 실패했다
    @Test
    void 대회를_수정할_때_최대_라운드를_빼면_어떤_값이_빠졌는지_알려준다() {
        // given
        Map<String, Object> request = new HashMap<>();
        request.put("name", "라임즙 많이 먹기 대회");
        request.put("startAt", "2024-12-11T00:00:00");
        request.put("endAt", "2024-12-13T00:00:00");

        configureMockJwtForEmail(MOCK_EMAIL);

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .cookie(COOKIE_NAME, mockToken)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request)
                .put("/leagues/{leagueId}", 1L)
                .then().log().all()
                .extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.jsonPath().getList("fieldErrors.field", String.class)).isEqualTo(List.of("maxRound"));
    }

    @Test
    void 대회에_참가할_팀들을_추가한다() {
        // given
        Long leagueId = 1L;
        LeagueRequest.Teams teamsRequest = new LeagueRequest.Teams(List.of(4L, 5L));

        configureMockJwtForEmail(MOCK_EMAIL);

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .cookie(COOKIE_NAME, mockToken)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(teamsRequest)
                .post("/leagues/{leagueId}/teams", leagueId)
                .then().log().all()
                .extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value());
    }

    @Test
    void 대회에_참가할_팀들을_삭제한다() {
        // given
        Long leagueId = 1L;
        LeagueRequest.Teams teamsRequest = new LeagueRequest.Teams(List.of(1L, 2L));

        configureMockJwtForEmail(MOCK_EMAIL);

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .cookie(COOKIE_NAME, mockToken)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(teamsRequest)
                .delete("/leagues/{leagueId}/teams", leagueId)
                .then().log().all()
                .extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    }
}
