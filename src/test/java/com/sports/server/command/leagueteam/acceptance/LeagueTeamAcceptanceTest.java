package com.sports.server.command.leagueteam.acceptance;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.sports.server.command.leagueteam.dto.LeagueTeamRegisterRequest;
import com.sports.server.command.leagueteam.dto.LeagueTeamRegisterRequest.LeagueTeamPlayerRegisterRequest;
import com.sports.server.support.AcceptanceTest;
import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;


@Sql("/league-fixture.sql")
public class LeagueTeamAcceptanceTest extends AcceptanceTest {

    @Test
    void 리그팀이_저장된다() {
        // given
        Long leagueId = 1L;
        List<LeagueTeamPlayerRegisterRequest> playerRegisterRequests = List.of(
                new LeagueTeamPlayerRegisterRequest("name-a", 1),
                new LeagueTeamPlayerRegisterRequest("name-b", 2));
        LeagueTeamRegisterRequest request = new LeagueTeamRegisterRequest(
                "name", "logo-image-url", playerRegisterRequests);

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
}