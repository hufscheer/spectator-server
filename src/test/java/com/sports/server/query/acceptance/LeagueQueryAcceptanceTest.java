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

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;

@Sql(scripts = "/league-fixture.sql")
public class LeagueQueryAcceptanceTest extends AcceptanceTest {

    @Nested
    class findLeagues{
        @Test
        void 삭제된_리그를_제외한_진행중인_대회들을_조회한다() {
            // when
            ExtractableResponse<Response> response = RestAssured.given().log().all()
                    .param("leagueProgress", "IN_PROGRESS")
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
                            .containsExactly(9L),
                    () -> assertThat(actual)
                            .map(LeagueResponse::name)
                            .containsExactly( "진행중인 축구대회"),
                    () -> assertThat(actual)
                            .map(LeagueResponse::maxRound)
                            .containsExactly(16),
                    () -> assertThat(actual)
                            .map(LeagueResponse::inProgressRound)
                            .containsExactly(16)
            );
        }

        @Test
        void 종료된_2025년_대회를_우승팀_정보와_함께_조회한다() {
            // when
            ExtractableResponse<Response> response = RestAssured.given().log().all()
                    .param("year", 2025)
                    .param("leagueProgress", "FINISHED")
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
                            .containsExactly(7L, 6L, 5L, 4L, 3L, 2L),
                    () -> assertThat(actual)
                            .map(LeagueResponse::name)
                            .containsExactly("종료된 축구대회 7", "종료된 축구대회 6", "종료된 축구대회 5", "종료된 축구대회 4", "종료된 축구대회 3", "종료된 축구대회 2"),
                    () -> assertThat(actual)
                            .map(LeagueResponse::maxRound)
                            .containsExactly(16, 8, 16, 16, 4, 4),
                    () -> assertThat(actual)
                            .map(LeagueResponse::inProgressRound)
                            .containsExactly(16, 8, 16, 16, 4, 4),
                    () -> assertThat(actual)
                            .map(LeagueResponse::winnerTeamName)
                            .containsExactly("서어 뻬데뻬", "경영 야생마", "컴공 독수리", "체교 불사조", "미컴 축구생각", "서어 뻬데뻬")
            );
        }
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
                () -> assertThat(actual.name()).isEqualTo("종료된 축구대회 1"),
                () -> assertThat(actual.startAt()).isEqualTo(LocalDateTime.of(2024, 3, 1, 10, 0, 0)),
                () -> assertThat(actual.endAt()).isEqualTo(LocalDateTime.of(2024, 3, 15, 22, 0, 0)),
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
                () -> assertThat(actual.size()).isEqualTo(8)
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

    @Test
    void 최근_대회_요약_정보를_조회한다() {
        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .param("year", 2025)
                .param("recordLimit", 5)
                .param("topScorerLimit", 5)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .get("/leagues/recent-summary")
                .then().log().all()
                .extract();

        // then
        LeagueRecentSummaryResponse actual = toResponse(response, LeagueRecentSummaryResponse.class);
        assertAll(
                () -> assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value()),
                () -> assertThat(actual.records()).hasSize(5),
                () -> assertThat(actual.records())
                        .extracting(LeagueRecentSummaryResponse.LeagueRecord::name)
                        .containsExactly("종료된 축구대회 7", "종료된 축구대회 6", "종료된 축구대회 5", "종료된 축구대회 4", "종료된 축구대회 3"),
                () -> assertThat(actual.topScorers()).hasSize(2),
                () -> assertThat(actual.topScorers().get(0).playerName()).isEqualTo("고병룡"),
                () -> assertThat(actual.topScorers().get(0).totalGoals()).isEqualTo(4)
        );
    }

}
