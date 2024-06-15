package com.sports.server.command.game.acceptance;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Objects;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;

import com.sports.server.command.game.domain.LineupPlayerState;
import com.sports.server.command.game.dto.LineupPlayerStateUpdateRequest;
import com.sports.server.query.dto.response.LineupPlayerResponse;
import com.sports.server.support.AcceptanceTest;

import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;

@Sql(scripts = "/game-fixture.sql")
public class LineupPlayerAcceptanceTest extends AcceptanceTest {

	@Test
	void 라인업_선수의_상태를_변경한다() {
		//given
		Long gameId = 1L;
		Long gameTeamId = 1L;
		String state = "CANDIDATE";
		List<Long> lineupPlayerIds = List.of(1L, 2L, 3L, 4L, 5L);
		LineupPlayerStateUpdateRequest request = new LineupPlayerStateUpdateRequest(state, lineupPlayerIds);

		//when
		RestAssured.given().log().all()
			.when()
			.contentType(MediaType.APPLICATION_JSON_VALUE)
			.body(request)
			.put("/lineup-players/state")
			.then().log().all()
			.extract();

		ExtractableResponse<Response> response = RestAssured.given().log().all()
			.when()
			.contentType(MediaType.APPLICATION_JSON_VALUE)
			.get("/games/{gameId}/lineup", gameId)
			.then().log().all()
			.extract();

		//then
		List<LineupPlayerResponse> actual = toResponses(response, LineupPlayerResponse.class).stream()
			.filter(team -> Objects.equals(team.gameTeamId(), gameTeamId))
			.toList();
		assertAll(
			() -> assertThat(actual.get(0).gameTeamPlayers().get(0).state()).isEqualTo(LineupPlayerState.CANDIDATE),
			() -> assertThat(actual.get(0).gameTeamPlayers().get(1).state()).isEqualTo(LineupPlayerState.CANDIDATE),
			() -> assertThat(actual.get(0).gameTeamPlayers().get(2).state()).isEqualTo(LineupPlayerState.CANDIDATE),
			() -> assertThat(actual.get(0).gameTeamPlayers().get(3).state()).isEqualTo(LineupPlayerState.CANDIDATE),
			() -> assertThat(actual.get(0).gameTeamPlayers().get(4).state()).isEqualTo(LineupPlayerState.CANDIDATE)
		);
	}
}
