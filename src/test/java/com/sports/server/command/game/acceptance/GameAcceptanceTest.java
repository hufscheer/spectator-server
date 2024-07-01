package com.sports.server.command.game.acceptance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import com.sports.server.command.game.domain.LineupPlayerState;
import com.sports.server.command.game.dto.CheerCountUpdateRequest;
import com.sports.server.query.dto.response.GameTeamCheerResponseDto;
import com.sports.server.query.dto.response.LineupPlayerResponse;
import com.sports.server.support.AcceptanceTest;
import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;

@Sql(scripts = "/game-fixture.sql")
public class GameAcceptanceTest extends AcceptanceTest {

    @Test
    void 경기에_참여하는_팀을_응원한다() {

        //given
        Long gameId = 1L;
        Long gameTeamId = 1L;
        int cheerCountBeforePost = 1;
        CheerCountUpdateRequest cheerRequestDto = new CheerCountUpdateRequest(gameTeamId, 1);

        // when
        RestAssured.given().log().all()
                .when()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(cheerRequestDto)
                .post("/games/{gameId}/cheer", gameId)
                .then().log().all()
                .extract();

        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .when()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .get("/games/{gameId}/cheer", gameId)
                .then().log().all()
                .extract();

        // then
        List<GameTeamCheerResponseDto> actual = toResponses(response, GameTeamCheerResponseDto.class).stream()
                .filter(team -> team.gameTeamId()
                        .equals(gameTeamId)).toList();
        assertThat(actual.get(0).cheerCount())
                .isEqualTo(cheerCountBeforePost + 1);
    }

    @Test
    void 라인업_선수의_상태를_선발로_변경한다() throws Exception {

        // given
        Long gameId = 1L;
        Long gameTeamId = 1L;
        Long lineupPlayerId = 1L;

        // when
        RestAssured.given().log().all()
            .when()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .patch("/games/{gameId}/lineup-players/{lineupPlayerId}/starter", gameId, lineupPlayerId)
            .then().log().all()
            .extract();

        ExtractableResponse<Response> response = RestAssured.given().log().all()
            .when()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .get("/games/{gameId}/lineup", gameId)
            .then().log().all()
            .extract();

        // then
        List<LineupPlayerResponse> lineupPlayerResponses = toResponses(response, LineupPlayerResponse.class).stream()
            .filter(lineupPlayerResponse -> lineupPlayerResponse.gameTeamId().equals(gameTeamId))
            .toList();

        List<LineupPlayerResponse.PlayerResponse> actual = lineupPlayerResponses.get(0).gameTeamPlayers().stream()
            .filter(playerResponse -> playerResponse.id().equals(lineupPlayerId))
            .toList();

        assertAll(
            () -> assertThat(lineupPlayerResponses.get(0).gameTeamId().equals(gameTeamId)),
            () -> assertThat(actual.get(0).id().equals(lineupPlayerId)),
            () -> assertThat(actual.get(0).state().equals(LineupPlayerState.STARTER))
        );
    }

    @Test
    void 라인업_선수의_상태를_후보로_변경한다() throws Exception {

        //given
        Long gameId = 1L;
        Long gameTeamId = 1L;
        Long lineupPlayerId = 2L;

        // when
        RestAssured.given().log().all()
            .when()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .patch("/games/{gameId}/lineup-players/{lineupPlayerId}/starter", gameId, lineupPlayerId)
            .then().log().all()
            .extract();

        ExtractableResponse<Response> response = RestAssured.given().log().all()
            .when()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .get("/games/{gameId}/lineup", gameId)
            .then().log().all()
            .extract();

        // then
        List<LineupPlayerResponse> lineupPlayerResponses = toResponses(response, LineupPlayerResponse.class).stream()
            .filter(lineupPlayerResponse -> lineupPlayerResponse.gameTeamId().equals(gameTeamId))
            .toList();

        List<LineupPlayerResponse.PlayerResponse> actual = lineupPlayerResponses.get(0).gameTeamPlayers().stream()
            .filter(playerResponse -> playerResponse.id().equals(lineupPlayerId))
            .toList();

        assertAll(
            () -> assertThat(lineupPlayerResponses.get(0).gameTeamId().equals(gameTeamId)),
            () -> assertThat(actual.get(0).id().equals(lineupPlayerId)),
            () -> assertThat(actual.get(0).state().equals(LineupPlayerState.CANDIDATE))
        );
    }
}
