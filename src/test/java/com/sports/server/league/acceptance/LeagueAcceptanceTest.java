package com.sports.server.league.acceptance;

import com.sports.server.support.AcceptanceTest;
import com.sports.server.league.dto.response.LeagueResponse;
import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@Sql(scripts = "/league-fixture.sql")
public class LeagueAcceptanceTest extends AcceptanceTest {

    @Test
    void 모든_리그를_조회한다() {
        // given

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .when()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .get("/leagues")
                .then().log().all()
                .extract();

        // then
        List<LeagueResponse> actual = toResponses(response, LeagueResponse.class);
        assertAll(
                () -> assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value()),
                () -> assertThat(actual)
                        .map(LeagueResponse::name)
                        .containsExactly("삼건물 대회", "농구대잔치", "롤 대회")
        );
    }
}
