package com.sports.server.query.acceptance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.sports.server.query.dto.response.LeagueTeamResponse;
import com.sports.server.support.AcceptanceTest;
import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;

@Sql(scripts = "/game-fixture.sql")
public class LeagueTeamQueryAcceptanceTest extends AcceptanceTest {

    @Test
    void 리그의_모든_리그팀을_조회한다() {
        // given
        Long threeBuildingCup = 1L;

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .when()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .get("/leagues/{leagueId}/teams", threeBuildingCup)
                .then().log().all()
                .extract();

        // then
        List<LeagueTeamResponse> actual = toResponses(response, LeagueTeamResponse.class);
        assertAll(
                () -> assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value()),
                () -> assertThat(actual)
                        .map(LeagueTeamResponse::teamName)
                        .containsExactly("팀 A", "팀 B", "팀 C", "팀 D", "팀 F", "팀 G"),
                () -> assertThat(actual)
                        .map(LeagueTeamResponse::teamId)
                        .containsExactly(1L, 2L, 3L, 4L, 6L, 7L),
                () -> assertThat(actual)
                        .map(LeagueTeamResponse::leagueTeamId)
                        .containsExactly(1L, 2L, 3L, 4L, 5L, 6L)


        );
    }

    @Test
    void 라운드에_진출한_리그팀만_조회한다() {
        // given
        Long threeBuildingCup = 1L;
        int finalRound = 2;

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .when()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .param("round", finalRound)
                .get("/leagues/{leagueId}/teams", threeBuildingCup)
                .then().log().all()
                .extract();

        // then
        List<LeagueTeamResponse> actual = toResponses(response, LeagueTeamResponse.class);
        assertAll(
                () -> assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value()),
                () -> assertThat(actual)
                        .map(LeagueTeamResponse::teamName)
                        .containsExactlyInAnyOrder("팀 B", "팀 D"),
                () -> assertThat(actual)
                        .map(LeagueTeamResponse::teamId)
                        .containsExactly(2L, 4L),
                () -> assertThat(actual)
                        .map(LeagueTeamResponse::leagueTeamId)
                        .containsExactly(2L, 4L)
        );
    }

    /**
     * 3·4위전은 결승과 라운드 번호가 같아, 번호만으로는 결승 진출팀이 잡힌다.
     */
    @Test
    void 삼사위전에_진출한_리그팀만_조회한다() {
        // given (리그 4: 결승은 팀 A·팀 C, 3·4위전은 팀 B·팀 D)
        Long roundFilterLeague = 4L;

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .when()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .param("round", 2)
                .param("third_place_match", true)
                .get("/leagues/{leagueId}/teams", roundFilterLeague)
                .then().log().all()
                .extract();

        // then
        assertAll(
                () -> assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value()),
                () -> assertThat(toResponses(response, LeagueTeamResponse.class))
                        .map(LeagueTeamResponse::teamName)
                        .containsExactlyInAnyOrder("팀 B", "팀 D")
        );
    }

    @Test
    void 같은_라운드_번호라도_삼사위전_플래그가_없으면_결승_진출팀이_조회된다() {
        // given
        Long roundFilterLeague = 4L;

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .when()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .param("round", 2)
                .get("/leagues/{leagueId}/teams", roundFilterLeague)
                .then().log().all()
                .extract();

        // then
        assertAll(
                () -> assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value()),
                () -> assertThat(toResponses(response, LeagueTeamResponse.class))
                        .map(LeagueTeamResponse::teamName)
                        .containsExactlyInAnyOrder("팀 A", "팀 C")
        );
    }

    @Test
    void 라운드_없이_삼사위전만_지정해도_조회된다() {
        // given
        Long roundFilterLeague = 4L;

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .when()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .param("third_place_match", true)
                .get("/leagues/{leagueId}/teams", roundFilterLeague)
                .then().log().all()
                .extract();

        // then
        assertAll(
                () -> assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value()),
                () -> assertThat(toResponses(response, LeagueTeamResponse.class))
                        .map(LeagueTeamResponse::teamName)
                        .containsExactlyInAnyOrder("팀 B", "팀 D")
        );
    }
}
