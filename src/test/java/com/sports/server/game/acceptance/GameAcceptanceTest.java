package com.sports.server.game.acceptance;

import com.sports.server.game.dto.response.GameDetailResponse;
import com.sports.server.support.AcceptanceTest;
import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@Sql(scripts = "/game-fixture.sql")
class GameAcceptanceTest extends AcceptanceTest {

    @Test
    void 게임을_상세_조회한다() {
        // given
        Long basketBallGameId = 1L;

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .when()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .get("/games/{gameId}", basketBallGameId)
                .then().log().all()
                .extract();

        // then
        GameDetailResponse game = toResponse(response, GameDetailResponse.class);
        List<GameDetailResponse.TeamResponse> teams = game.gameTeams();
        assertAll(
                () -> assertThat(game.gameName()).isEqualTo("농구 대전"),
                () -> assertThat(game.gameQuarter()).isEqualTo("1st Quarter"),
                () -> assertThat(game.videoId()).isEqualTo("abc123"),
                () -> assertThat(game.startTime()).isEqualTo(
                        LocalDateTime.of(2023, 11, 12, 10, 0, 0)
                ),

                () -> assertThat(teams).hasSize(2),
                () -> assertThat(teams)
                        .filteredOn(team -> team.gameTeamId().equals(1L))
                        .containsExactly(new GameDetailResponse.TeamResponse(
                                1L, "팀 A",
                                "http://example.com/logo_a.png", 1)
                        ),
                () -> assertThat(teams)
                        .filteredOn(team -> team.gameTeamId().equals(2L))
                        .containsExactly(new GameDetailResponse.TeamResponse(
                                2L,"팀 B",
                                "http://example.com/logo_b.png", 2)
                        )
        );
    }
}
