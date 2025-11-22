package com.sports.server.command.league.acceptance;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.sports.server.command.league.dto.LeagueRequest;
import com.sports.server.support.AcceptanceTest;
import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import java.time.LocalDateTime;
import java.util.List;

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
        LeagueRequest.Register request = new LeagueRequest.Register(
                "우물정 제기차기 대회",
                4,
                LocalDateTime.of(2025, 1, 1, 0, 0),
                LocalDateTime.of(2025, 1, 15, 0, 0),
                teamIds
        );

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
                LocalDateTime.of(24, 12, 13, 0, 0, 0));

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
