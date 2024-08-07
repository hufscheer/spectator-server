package com.sports.server.query.acceptance;

import com.sports.server.command.league.domain.LeagueProgress;
import com.sports.server.query.dto.response.LeagueDetailResponse;
import com.sports.server.query.dto.response.LeagueResponse;
import com.sports.server.query.dto.response.LeagueSportResponse;
import com.sports.server.query.dto.response.LeagueTeamPlayerResponse;
import com.sports.server.support.AcceptanceTest;
import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@Sql(scripts = "/league-fixture.sql")
public class LeagueQueryAcceptanceTest extends AcceptanceTest {

    @Test
    void 삭제되지_않은_모든_리그를_조회한다() {
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
                        .map(LeagueResponse::leagueId)
                        .containsExactly(3L, 2L, 1L, 7L, 6L, 5L),
                () -> assertThat(actual)
                        .map(LeagueResponse::name)
                        .containsExactly("롤 대회", "농구대잔치", "삼건물 대회", "롤 대회", "농구대잔치", "삼건물 대회"),
                () -> assertThat(actual)
                        .map(LeagueResponse::maxRound)
                        .containsExactly("8강", "8강", "16강", "8강", "8강", "16강"),
                () -> assertThat(actual)
                        .map(LeagueResponse::inProgressRound)
                        .containsExactly("8강", "결승", "8강", "8강", "결승", "8강")
        );
    }

    @Test
    void 특정_연도의_리그를_조회한다() {
        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .when()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .param("year", 2022)
                .get("/leagues")
                .then().log().all()
                .extract();

        // then
        List<LeagueResponse> actual = toResponses(response, LeagueResponse.class);
        assertAll(
                () -> assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value()),
                () -> assertThat(actual)
                        .map(LeagueResponse::leagueId)
                        .containsExactly(7L, 6L, 5L)
        );
    }

    @Test
    void 리그의_모든_스포츠를_조회한다() {
        // given
        Long threeBuildingCup = 1L;

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .when()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .get("/leagues/{leagueId}/sports", threeBuildingCup)
                .then().log().all()
                .extract();

        // then
        List<LeagueSportResponse> actual = toResponses(response, LeagueSportResponse.class);
        assertAll(
                () -> assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value()),
                () -> assertThat(actual)
                        .map(LeagueSportResponse::name)
                        .containsExactly("축구"),
                () -> assertThat(actual)
                        .map(LeagueSportResponse::sportId)
                        .containsExactly(threeBuildingCup)
        );
    }

    @Test
    void 리그를_하나_조회한다() {
        // given
        Long threeBuildingCup = 1L;

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .when()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .get("/leagues/{leagueId}", threeBuildingCup)
                .then().log().all()
                .extract();

        // then
        LeagueDetailResponse actual = toResponse(response, LeagueDetailResponse.class);
        assertAll(
                () -> assertThat(actual.name()).isEqualTo("삼건물 대회"),
                () -> assertThat(actual.startAt()).isEqualTo(LocalDateTime.of(2023, 11, 9, 0, 0, 0)),
                () -> assertThat(actual.endAt()).isEqualTo(LocalDateTime.of(2023, 11, 20, 0, 0, 0)),
                () -> assertThat(actual.inProgressRound()).isEqualTo("8강"),
                () -> assertThat(actual.maxRound()).isEqualTo("16강"),
                () -> assertThat(actual.leagueProgress()).isEqualTo(LeagueProgress.FINISHED.getDescription())
        );
    }

    @Test
    void 리그팀의_모든_선수를_조회한다() {
        // given
        Long soccerishThought = 3L;

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
            .when()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .get("/leagues/teams/{leagueTeamId}/players", soccerishThought)
            .then().log().all()
            .extract();

        // then
        List<LeagueTeamPlayerResponse> actual = toResponses(response, LeagueTeamPlayerResponse.class);
        assertAll(
            () -> assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value()),
            () -> assertThat(actual).map(LeagueTeamPlayerResponse::name)
                .containsExactly("가을전어이동규", "겨울붕어빵이현제", "봄동나물진승희", "여름수박고병룡"),
            () -> assertThat(actual).map(LeagueTeamPlayerResponse::id)
                .containsExactly(2L, 3L, 1L, 4L)
        );
    }
}
