package com.sports.server.game.acceptance;

import com.sports.server.game.dto.response.GameLineupResponse;
import com.sports.server.support.AcceptanceTest;
import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;


@Sql(scripts = "/game-fixture.sql")
public class GameTeamPlayerAcceptanceTest extends AcceptanceTest {

    @Test
    void 경기_라인업을_조회한다() {
        // given
        Long basketBallGameId = 1L;
        Long teamAId = 1L;
        Long teamBId = 2L;

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .when()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .get("/games/{gameId}/lineup", basketBallGameId)
                .then().log().all()
                .extract();

        // then
        List<GameLineupResponse> actual = toResponses(response, GameLineupResponse.class);
        GameLineupResponse teamA = actual.stream()
                .filter(lineup -> lineup.gameTeamId().equals(teamAId))
                .toList()
                .get(0);
        GameLineupResponse teamB = actual.stream()
                .filter(lineup -> lineup.gameTeamId().equals(teamBId))
                .toList()
                .get(0);
        assertAll(
                () -> assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value()),
                () -> assertThat(teamA.teamName()).isEqualTo("팀 A"),
                () -> assertThat(teamA.gameTeamPlayers())
                        .map(GameLineupResponse.PlayerResponse::playerName)
                        .containsExactly("선수1", "선수2", "선수3", "선수4", "선수5"),
                () -> assertThat(teamA.gameTeamPlayers())
                        .map(GameLineupResponse.PlayerResponse::description)
                        .containsExactly("센터", "파워 포워드", "슈팅 가드", "포인트 가드", "스몰 포워드"),

                () -> assertThat(teamB.teamName()).isEqualTo("팀 B"),
                () -> assertThat(teamB.gameTeamPlayers())
                        .map(GameLineupResponse.PlayerResponse::playerName)
                        .containsExactly("선수6", "선수7", "선수8", "선수9", "선수10"),
                () -> assertThat(teamB.gameTeamPlayers())
                        .map(GameLineupResponse.PlayerResponse::description)
                        .containsExactly("센터", "파워 포워드", "슈팅 가드", "포인트 가드", "스몰 포워드")
        );
    }
}
