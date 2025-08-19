package com.sports.server.query.acceptance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.sports.server.command.league.domain.LeagueProgress;
import com.sports.server.query.dto.response.*;
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
                        .containsExactly(2L, 1L, 3L),
                () -> assertThat(actual)
                        .map(LeagueResponse::name)
                        .containsExactly("2025 훕치치 농구대잔치", "2025 훕치치 풋살 챔피언십", "지난 탁구 대회"),
                () -> assertThat(actual)
                        .map(LeagueResponse::maxRound)
                        .containsExactly(4, 8, 16),
                () -> assertThat(actual)
                        .map(LeagueResponse::inProgressRound)
                        .containsExactly(4, 8, 16)
        );
    }

    @Test
    void 특정_연도의_리그를_조회한다() {
        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .when()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .param("year", 2025)
                .get("/leagues")
                .then().log().all()
                .extract();

        // then
        List<LeagueResponse> actual = toResponses(response, LeagueResponse.class);
        assertAll(
                () -> assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value()),
                () -> assertThat(actual)
                        .map(LeagueResponse::leagueId)
                        .containsExactly(2L, 1L)
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
                () -> assertThat(actual.name()).isEqualTo("2025 훕치치 풋살 챔피언십"),
                () -> assertThat(actual.startAt()).isEqualTo(LocalDateTime.of(2025, 8, 1, 10, 0, 0)),
                () -> assertThat(actual.endAt()).isEqualTo(LocalDateTime.of(2025, 8, 15, 22, 0, 0)),
                () -> assertThat(actual.inProgressRound()).isEqualTo(8),
                () -> assertThat(actual.maxRound()).isEqualTo(8),
                () -> assertThat(actual.leagueProgress()).isEqualTo(LeagueProgress.FINISHED.getDescription())
        );
    }

    @Test
    void 리그팀의_모든_선수를_조회한다() {
        // given
        Long leagueTeamId = 3L;

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .when()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .get("/leagues/teams/{leagueTeamId}/players", leagueTeamId)
                .then().log().all()
                .extract();

        // then
        List<PlayerResponse> actual = toResponses(response, PlayerResponse.class);
        assertAll(
                () -> assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value()),
                () -> assertThat(actual).hasSize(2),
                () -> assertThat(actual).map(PlayerResponse::name)
                        .containsExactlyInAnyOrder("진승희", "이동규"),
                () -> assertThat(actual).map(PlayerResponse::playerId)
                        .containsExactlyInAnyOrder(1L, 2L)
        );
    }

    @Test
    void 매니저가_생성한_리그를_진행_중인_경기와_함께_모두_조회한다() {

        // given
        configureMockJwtForEmail("john.doe@example.com");

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .cookie(COOKIE_NAME, mockToken)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .get("/leagues/manager")
                .then().log().all()
                .extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    void 매니저가_생성한_리그를_모두_조회한다() {

        // given
        configureMockJwtForEmail("john.doe@example.com");

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .cookie(COOKIE_NAME, mockToken)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .get("/leagues/manager/manage")
                .then().log().all()
                .extract();

        // then
        List<LeagueResponseToManage> actual = toResponses(response, LeagueResponseToManage.class);
        assertAll(
                () -> assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value()),
                () -> assertThat(actual.size()).isEqualTo(2)
        );

    }

    @Test
    void 리그의_모든_경기를_조회한다() {
        // given
        Long leagueId = 1L;

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .pathParam("leagueId", leagueId)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .get("/leagues/{leagueId}/games")
                .then().log().all()
                .extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    }

}
