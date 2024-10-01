package com.sports.server.command.game.acceptance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.sports.server.command.game.domain.LineupPlayerState;
import com.sports.server.command.game.dto.CheerCountUpdateRequest;
import com.sports.server.command.game.dto.GameRequestDto;
import com.sports.server.command.league.domain.Round;
import com.sports.server.query.dto.response.GameDetailResponse;
import com.sports.server.query.dto.response.GameTeamCheerResponseDto;
import com.sports.server.query.dto.response.LineupPlayerResponse;
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
                () -> assertThat(lineupPlayerResponses.get(0).gameTeamId()).isEqualTo(gameTeamId),
                () -> assertThat(actual.get(0).id()).isEqualTo(lineupPlayerId),
                () -> assertThat(actual.get(0).state()).isEqualTo(LineupPlayerState.STARTER)
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
                .patch("/games/{gameId}/lineup-players/{lineupPlayerId}/candidate", gameId, lineupPlayerId)
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
                () -> assertThat(lineupPlayerResponses.get(0).gameTeamId()).isEqualTo(gameTeamId),
                () -> assertThat(actual.get(0).id()).isEqualTo(lineupPlayerId),
                () -> assertThat(actual.get(0).state()).isEqualTo(LineupPlayerState.CANDIDATE)
        );
    }

    @Test
    void 새로운_경기를_등록한다() {

        //given
        Long leagueId = 1L;
        Long idOfTeam1 = 1L;
        Long idOfTeam2 = 2L;
        GameRequestDto.Register requestDto = new GameRequestDto.Register("경기 이름", 16, "전반전", "SCHEDULED",
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
        assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value());
    }

    @Test
    void 경기_정보를_수정한_후_다시_가져와_확인한다() throws Exception {

        // given
        Long leagueId = 1L;
        Long gameId = 1L;
        String name = "경기 이름";
        int round = 16;
        String quarter = "후반전";
        String state = "PLAYING";
        LocalDateTime fixedLocalDateTime = LocalDateTime.of(2024, 9, 11, 12, 0, 0);
        String videoId = "videoId";
        GameRequestDto.Update request = new GameRequestDto.Update(name, round, quarter, state, fixedLocalDateTime, videoId);

        configureMockJwtForEmail(MOCK_EMAIL);

        // when
        ExtractableResponse<Response> putResponse = RestAssured.given().log().all()
                .cookie(COOKIE_NAME, mockToken)
                .pathParam("leagueId", leagueId)
                .pathParam("gameId", gameId)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request)
                .put("/leagues/{leagueId}/{gameId}")
                .then().log().all()
                .extract();
        ExtractableResponse<Response> getResponse = RestAssured.given().log().all()
                .when()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .get("/games/{gameId}", gameId)
                .then().log().all()
                .extract();

        // then
        GameDetailResponse updatedGame = toResponse(getResponse, GameDetailResponse.class);
        assertAll(
                () -> assertThat(putResponse.statusCode()).isEqualTo(HttpStatus.OK.value()),
                () -> assertThat(updatedGame.gameQuarter()).isEqualTo(quarter),
                () -> assertThat(updatedGame.round()).isEqualTo(round),
                () -> assertThat(updatedGame.gameName()).isEqualTo(name),
                () -> assertThat(updatedGame.startTime()).isEqualTo(fixedLocalDateTime),
                () -> assertThat(updatedGame.state()).isEqualTo(state),
                () -> assertThat(updatedGame.videoId()).isEqualTo(videoId)
        );
    }

    @Test
    void 경기를_삭제한다() {
        // given
        Long leagueId = 1L;
        Long gameId = 1L;

        configureMockJwtForEmail(MOCK_EMAIL);

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .cookie(COOKIE_NAME, mockToken)
                .pathParam("leagueId", leagueId)
                .pathParam("gameId", gameId)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .delete("/leagues/{leagueId}/{gameId}")
                .then().log().all()
                .extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
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
                .patch("/games/{gameId}/lineup-players/{lineupPlayerId}/captain/register", gameId,
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
                () -> assertThat(lineupPlayerResponses.get(0).gameTeamId()).isEqualTo(gameTeamId),
                () -> assertThat(actual.get(0).id()).isEqualTo(lineupPlayerId),
                () -> assertThat(actual.get(0).isCaptain()).isEqualTo(true)
        );
    }

    @Test
    void 라인업_선수를_주장에서_해제한다() throws Exception {

        //given
        Long gameId = 1L;
        Long gameTeamId = 2L;
        Long lineupPlayerId = 6L;

        // when
        RestAssured.given().log().all()
                .when()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .patch("/games/{gameId}/lineup-players/{lineupPlayerId}/captain/revoke", gameId,
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
                () -> assertThat(lineupPlayerResponses.get(0).gameTeamId()).isEqualTo(gameTeamId),
                () -> assertThat(actual.get(0).id()).isEqualTo(lineupPlayerId),
                () -> assertThat(actual.get(0).isCaptain()).isEqualTo(false)
        );
    }
}
