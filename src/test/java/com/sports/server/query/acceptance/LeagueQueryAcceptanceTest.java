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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlMergeMode;

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
                () -> assertThat(actual.topScorers())
                        .extracting(LeagueRecentSummaryResponse.TopScorer::playerName)
                        .containsExactly("고병룡", "박주장"),
                () -> assertThat(actual.topScorers())
                        .extracting(LeagueRecentSummaryResponse.TopScorer::unit)
                        .containsExactly("경영대학", "경영대학"),
                () -> assertThat(actual.topScorers())
                        .extracting(LeagueRecentSummaryResponse.TopScorer::totalGoals)
                        .containsExactly(4, 2)
        );
    }

    @Test
    void 리그의_활성_응원톡_수를_조회한다() {
        // given
        Long leagueId = 1L;

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .pathParam("leagueId", leagueId)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .get("/leagues/{leagueId}/cheer-count")
                .then().log().all()
                .extract();

        // then
        LeagueCheerTalkCountResponse actual = toResponse(response, LeagueCheerTalkCountResponse.class);
        assertAll(
                () -> assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value()),
                () -> assertThat(actual.cheerTalkCount()).isEqualTo(6L)
        );
    }

    @Nested
    @DisplayName("최근 리그 게임 조회")
    @SqlMergeMode(SqlMergeMode.MergeMode.OVERRIDE)
    class FindRecentLeagueGames {

        private ExtractableResponse<Response> 최근_리그_게임_조회() {
            return RestAssured.given().log().all()
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .get("/leagues/recent/games")
                    .then().log().all()
                    .extract();
        }

        @Test
        @Sql(scripts = "/recent-league-games-in-progress-fixture.sql")
        void 진행_중인_리그가_있으면_진행_중인_리그의_게임만_반환한다() {
            // when
            ExtractableResponse<Response> response = 최근_리그_게임_조회();

            // then
            List<Long> leagueIds = response.jsonPath().getList("leagueId", Long.class);
            assertAll(
                    () -> assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value()),
                    () -> assertThat(leagueIds).containsExactly(1L),
                    () -> assertThat(leagueIds).doesNotContain(2L, 3L)
            );
        }

        @Test
        @Sql(scripts = "/recent-league-games-ended-fixture.sql")
        void 진행_중인_리그가_없으면_가장_최근_종료된_리그들의_게임을_반환한다() {
            // when
            ExtractableResponse<Response> response = 최근_리그_게임_조회();

            // then
            List<Long> leagueIds = response.jsonPath().getList("leagueId", Long.class);
            assertAll(
                    () -> assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value()),
                    () -> assertThat(leagueIds).containsExactlyInAnyOrder(1L, 2L),
                    () -> assertThat(leagueIds).doesNotContain(3L)
            );
        }

        @Test
        @Sql(scripts = "/recent-league-games-no-games-fixture.sql")
        void 경기가_없어도_리그_정보는_반환된다() {
            // when
            ExtractableResponse<Response> response = 최근_리그_게임_조회();

            // then
            List<Long> leagueIds = response.jsonPath().getList("leagueId", Long.class);
            assertAll(
                    () -> assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value()),
                    () -> assertThat(leagueIds).containsExactly(1L)
            );
        }
    }

    @Test
    void 존재하지_않는_엔드포인트_요청시_404를_반환한다() {
        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .get("/not-exists")
                .then().log().all()
                .extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

}
