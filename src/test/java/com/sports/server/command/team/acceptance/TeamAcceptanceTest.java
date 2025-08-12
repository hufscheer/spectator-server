package com.sports.server.command.team.acceptance;

import com.sports.server.command.team.domain.Team;
import com.sports.server.command.team.domain.TeamRepository;
import com.sports.server.command.team.domain.Unit;
import com.sports.server.command.team.dto.TeamRequest;
import com.sports.server.support.AcceptanceTest;
import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ActiveProfiles("test")
@Sql("/member-fixture.sql")
public class TeamAcceptanceTest extends AcceptanceTest {

    @Autowired
    private TeamRepository teamRepository;

    @Value("${image.origin-prefix}")
    private String originPrefix;

    @Test
    void 팀을_생성한다() {
        // given
        List<TeamRequest.TeamPlayerRegister> playersToRegister = List.of(
                new TeamRequest.TeamPlayerRegister(1L, 10),
                new TeamRequest.TeamPlayerRegister(2L, 7)
        );

        TeamRequest.Register request = new TeamRequest.Register(
                "경영 야생마",
                originPrefix + "logo-url",
                Unit.BUSINESS,
                "#FF0000",
                playersToRegister
        );

        configureMockJwtForEmail("john@example.com");

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .cookie(COOKIE_NAME, mockToken)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request)
                .post("/teams")
                .then().log().all()
                .extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value());
    }

    @Test
    void 팀_정보를_수정한다() {
        // given
        Team savedTeam = teamRepository.save(Team.builder()
                .name("정치외교학과 PSD")
                .teamColor("team color")
                .unit(Unit.SOCIAL_SCIENCES)
                .logoImageUrl(originPrefix + "logo-url").build());

        List<TeamRequest.TeamPlayerRegister> playersToUpdate = List.of(
                new TeamRequest.TeamPlayerRegister(3L, 99)
        );

        TeamRequest.Update request = new TeamRequest.Update(
                "국제통상학과 무역풍",
                originPrefix + "logo-url",
                Unit.BUSINESS,
                "#FFFFFF",
                playersToUpdate
        );
        configureMockJwtForEmail("john@example.com");

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .cookie(COOKIE_NAME, mockToken)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request)
                .put("/teams/{teamId}", savedTeam.getId())
                .then().log().all()
                .extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    void 팀을_삭제한다() {
        // given
        Team savedTeam = teamRepository.save(Team.builder()
                .name("정치외교학과 PSD")
                .teamColor("team color")
                .unit(Unit.SOCIAL_SCIENCES)
                .logoImageUrl(originPrefix + "logo-url").build());

        configureMockJwtForEmail("john@example.com");

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .cookie(COOKIE_NAME, mockToken)
                .pathParam("teamId", savedTeam.getId())
                .delete("/teams/{teamId}")
                .then().log().all()
                .extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    }
}
