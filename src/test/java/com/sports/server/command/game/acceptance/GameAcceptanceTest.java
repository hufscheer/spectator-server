package com.sports.server.command.game.acceptance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.sports.server.command.game.domain.LineupPlayerState;
import com.sports.server.command.game.dto.CheerCountUpdateRequest;
import com.sports.server.command.game.dto.GameRequestDto;
import com.sports.server.query.dto.response.GameTeamCheerResponseDto;
import com.sports.server.query.dto.response.LineupPlayerResponse;
import com.sports.server.support.AcceptanceTest;
import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import java.time.LocalDateTime;
import java.util.List;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
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

    @Test
    void 새로운_경기를_등록한다() {

        //given
        Long leagueId = 1L;
        Long idOfTeam1 = 1L;
        Long idOfTeam2 = 2L;
        GameRequestDto.Register requestDto = new GameRequestDto.Register("경기 이름", "16강", "전반전", "SCHEDULED",
                LocalDateTime.now(), idOfTeam1, idOfTeam2, null);

        configureMockJwtForEmail("john.doe@example.com");

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .cookie(COOKIE_NAME, mockToken)
                .pathParam("leagueId", leagueId)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(requestDto)
                .post("/leagues/{leagueId}/games", leagueId)
                .then().log().all()
                .extract();

        // then
        AssertionsForClassTypes.assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value());
    }

    @Test
    void 라인업_선수를_주장으로_등록한다() throws Exception {

        //given
        Long gameId = 1L;
        Long gameTeamId = 1L;
        Long lineupPlayerId = 2L;

        // when
        RestAssured.given().log().all()
                .when()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .patch("/games/{gameId}/{gameTeamId}/lineup-players/{lineupPlayerId}/captain/register", gameId,
                        gameTeamId,
                        lineupPlayerId)
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
                () -> assertThat(actual.get(0).isCaptain()).isEqualTo(true)
        );
    }

    @Test
    void 라인업_선수를_주장에서_해제한다() throws Exception {

        //given
        Long gameId = 1L;
        Long gameTeamId = 1L;
        Long lineupPlayerId = 2L;

        // when

        // 주장으로 등록
        RestAssured.given().log().all()
                .when()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .patch("/games/{gameId}/{gameTeamId}/lineup-players/{lineupPlayerId}/captain/register", gameId,
                        gameTeamId,
                        lineupPlayerId);

        RestAssured.given().log().all()
                .when()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .patch("/games/{gameId}/{gameTeamId}/lineup-players/{lineupPlayerId}/captain/revoke", gameId,
                        gameTeamId,
                        lineupPlayerId);

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
                () -> assertThat(actual.get(0).isCaptain()).isEqualTo(false)
        );
    }

}
