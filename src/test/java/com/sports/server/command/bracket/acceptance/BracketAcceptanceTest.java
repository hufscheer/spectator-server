package com.sports.server.command.bracket.acceptance;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.sports.server.command.bracket.dto.BracketRequest;
import com.sports.server.support.AcceptanceTest;
import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;

@Sql("/bracket-fixture.sql")
public class BracketAcceptanceTest extends AcceptanceTest {

    @Test
    void 대진표를_배치한다() {
        // given
        Long leagueId = 1L;
        BracketRequest.Save request = new BracketRequest.Save(8, List.of(
                new BracketRequest.Entry(1, 1L),
                new BracketRequest.Entry(2, 2L),
                new BracketRequest.Entry(3, 3L),
                new BracketRequest.Entry(4, 4L),
                new BracketRequest.Entry(5, 5L),
                new BracketRequest.Entry(7, 6L)
        ));

        configureMockJwtForEmail(MOCK_EMAIL);

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .cookie(COOKIE_NAME, mockToken)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request)
                .put("/leagues/{leagueId}/bracket", leagueId)
                .then().log().all()
                .extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    void 대진표를_조회한다() {
        // given (리그 3: 4강 1매치가 종료된 경기와 연결되어 5번 팀이 결승 진출)
        Long leagueId = 3L;

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .get("/leagues/{leagueId}/bracket", leagueId)
                .then().log().all()
                .extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.jsonPath().getInt("size")).isEqualTo(4);
        assertThat(response.jsonPath().getList("rounds").size()).isEqualTo(2);
        assertThat(response.jsonPath().getLong("rounds[0].matches[0].winnerTeamId")).isEqualTo(5L);
        assertThat(response.jsonPath().getLong("rounds[1].matches[0].team1.teamId")).isEqualTo(5L);
    }

    @Test
    void 대진표가_없는_리그는_조회할_수_없다() {
        // given
        Long leagueId = 1L;

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .get("/leagues/{leagueId}/bracket", leagueId)
                .then().log().all()
                .extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }
}
