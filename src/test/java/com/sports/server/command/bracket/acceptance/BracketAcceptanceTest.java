package com.sports.server.command.bracket.acceptance;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.sports.server.command.bracket.dto.BracketRequest;
import com.sports.server.command.game.dto.GameRequest;
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

    @Test
    void 대진표_조회에_준결승_패자가_배정된_3_4위전이_함께_내려온다() {
        // given (리그 4: 준결승 두 경기 종료 → 패자는 2번·4번 팀)
        Long leagueId = 4L;

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .get("/leagues/{leagueId}/bracket", leagueId)
                .then().log().all()
                .extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.jsonPath().getList("rounds").size()).isEqualTo(2);
        assertThat(response.jsonPath().getLong("thirdPlaceMatch.team1.teamId")).isEqualTo(2L);
        assertThat(response.jsonPath().getLong("thirdPlaceMatch.team2.teamId")).isEqualTo(4L);
        assertThat(response.jsonPath().getObject("thirdPlaceMatch.gameId", Long.class)).isNull();
    }

    @Test
    void 준결승_패자로_3_4위전_경기를_만들면_대진표에_연결된다() {
        // given
        Long leagueId = 4L;
        configureMockJwtForEmail(MOCK_EMAIL);

        // when
        ExtractableResponse<Response> createResponse = RestAssured.given().log().all()
                .cookie(COOKIE_NAME, mockToken)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(thirdPlaceGameRequest(2L, 4L))
                .post("/leagues/{leagueId}/games", leagueId)
                .then().log().all()
                .extract();

        // then
        assertThat(createResponse.statusCode()).isEqualTo(HttpStatus.CREATED.value());

        ExtractableResponse<Response> bracket = RestAssured.given()
                .get("/leagues/{leagueId}/bracket", leagueId)
                .then().extract();
        assertThat(bracket.jsonPath().getObject("thirdPlaceMatch.gameId", Long.class)).isNotNull();
    }

    @Test
    void 준결승_패자가_아닌_팀으로는_3_4위전_경기를_만들_수_없다() {
        // given (1번 팀은 준결승 승자)
        Long leagueId = 4L;
        configureMockJwtForEmail(MOCK_EMAIL);

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .cookie(COOKIE_NAME, mockToken)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(thirdPlaceGameRequest(1L, 4L))
                .post("/leagues/{leagueId}/games", leagueId)
                .then().log().all()
                .extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void 옵션이_꺼진_대회에서는_3_4위전_경기를_만들_수_없다() {
        // given (리그 2는 옵션이 꺼져 있다)
        Long leagueId = 2L;
        configureMockJwtForEmail(MOCK_EMAIL);

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .cookie(COOKIE_NAME, mockToken)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(thirdPlaceGameRequest(1L, 2L))
                .post("/leagues/{leagueId}/games", leagueId)
                .then().log().all()
                .extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    private GameRequest.Register thirdPlaceGameRequest(final Long teamId1, final Long teamId2) {
        return new GameRequest.Register("3·4위전", 4, "경기전", "SCHEDULED",
                LocalDateTime.of(2025, 8, 12, 18, 0), null,
                new GameRequest.TeamLineupRequest(teamId1, List.of()),
                new GameRequest.TeamLineupRequest(teamId2, List.of()),
                true);
    }

    @Test
    void 대진표에_3_4위전이_없으면_null로_내려온다() {
        // given
        Long leagueId = 3L;

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .get("/leagues/{leagueId}/bracket", leagueId)
                .then().log().all()
                .extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.jsonPath().getObject("thirdPlaceMatch", Object.class)).isNull();
    }
}
