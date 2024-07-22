package com.sports.server.command.leagueteam.acceptance;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.sports.server.command.leagueteam.dto.LeagueTeamPlayerRequest;
import com.sports.server.command.leagueteam.dto.LeagueTeamRequest;
import com.sports.server.support.AcceptanceTest;
import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;


@Sql("/league-fixture.sql")
public class LeagueTeamAcceptanceTest extends AcceptanceTest {

    @Value("${image.origin-prefix}")
    private String originPrefix;

    @Test
    void 리그팀이_저장된다() {
        // given
        Long leagueId = 1L;
        List<LeagueTeamPlayerRequest.Register> playerRegisterRequests = List.of(
                new LeagueTeamPlayerRequest.Register("name-a", 1),
                new LeagueTeamPlayerRequest.Register("name-b", 2));
        LeagueTeamRequest.Register request = new LeagueTeamRequest.Register(
                "name", originPrefix + "image", playerRegisterRequests);

        configureMockJwtForEmail("john.doe@example.com");

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .cookie(COOKIE_NAME, mockToken)
                .pathParam("leagueId", leagueId)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request)
                .post("/leagues/{leagueId}/teams")
                .then().log().all()
                .extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    void 리그팀을_수정한다() {
        // given
        Long leagueId = 1L;
        Long teamId = 3L;
        List<LeagueTeamRequest.LeagueTeamPlayerRequest> playerRegisterRequests = List.of(
                new LeagueTeamRequest.LeagueTeamPlayerRequest("name-a", 1),
                new LeagueTeamRequest.LeagueTeamPlayerRequest("name-b", 2));
        LeagueTeamRequest.Update request = new LeagueTeamRequest.Update(
                "name", originPrefix + "image", playerRegisterRequests, List.of(1L, 2L));

        configureMockJwtForEmail("john.doe@example.com");

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .cookie(COOKIE_NAME, mockToken)
                .pathParam("leagueId", leagueId)
                .pathParam("teamId", teamId)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request)
                .put("/leagues/{leagueId}/teams/{teamId}")
                .then().log().all()
                .extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    }
}