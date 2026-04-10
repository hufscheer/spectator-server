package com.sports.server.command.team.acceptance;

import com.sports.server.command.team.domain.Team;
import com.sports.server.command.team.domain.TeamPlayer;
import com.sports.server.command.team.domain.TeamPlayerRepository;
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
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Sql("/team-fixture.sql")
public class TeamAcceptanceTest extends AcceptanceTest {

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private TeamPlayerRepository teamPlayerRepository;

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
                "경영대학",
                "#FF0000",
                playersToRegister,
                null
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

        TeamRequest.Update request = new TeamRequest.Update(
                "국제통상학과 무역풍",
                originPrefix + "logo-url",
                "경영대학",
                "#FFFFFF",
                null
        );
        configureMockJwtForEmail("john@example.com");

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .cookie(COOKIE_NAME, mockToken)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request)
                .patch("/teams/{teamId}", savedTeam.getId())
                .then().log().all()
                .extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    void 팀_정보를_수정할_때_선수를_upsert한다() {
        // given
        // fixture: teamId=1, 기존 선수 3L(등번호 9), 4L(등번호 11)
        // 3L → 등번호 수정, 1L → 신규 추가
        TeamRequest.Update request = new TeamRequest.Update(
                null,
                null,
                null,
                null,
                List.of(
                        new TeamRequest.TeamPlayerRegister(3L, 99),
                        new TeamRequest.TeamPlayerRegister(1L, 10)
                )
        );
        configureMockJwtForEmail("john@example.com");

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .cookie(COOKIE_NAME, mockToken)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request)
                .patch("/teams/{teamId}", 1L)
                .then().log().all()
                .extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());

        List<TeamPlayer> teamPlayers = teamPlayerRepository.findTeamPlayersWithPlayerByTeamId(1L);
        assertThat(teamPlayers.size()).isEqualTo(3);

        Optional<TeamPlayer> updatedPlayer = teamPlayers.stream()
                .filter(tp -> tp.getPlayer().getId().equals(3L))
                .findFirst();
        assertThat(updatedPlayer.isPresent()).isTrue();
        assertThat(updatedPlayer.get().getJerseyNumber()).isEqualTo(99);

        Optional<TeamPlayer> newPlayer = teamPlayers.stream()
                .filter(tp -> tp.getPlayer().getId().equals(1L))
                .findFirst();
        assertThat(newPlayer.isPresent()).isTrue();
        assertThat(newPlayer.get().getJerseyNumber()).isEqualTo(10);
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
        assertThat(response.statusCode()).isEqualTo(HttpStatus.NO_CONTENT.value());
    }
}
