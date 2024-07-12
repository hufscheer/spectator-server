package com.sports.server.command.league.acceptance;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import com.sports.server.command.league.dto.LeagueRequest;
import com.sports.server.support.AcceptanceTest;

import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;

class LeagueAcceptanceTest extends AcceptanceTest {
	@Test
	void 대회를_저장한다() throws Exception {
		// given
		LeagueRequest request = new LeagueRequest(1L, "우물정 제기차기 대회", 4, LocalDateTime.now(), LocalDateTime.now());

		// when
		ExtractableResponse<Response> response = RestAssured.given().log().all()
			.when()
			.contentType(MediaType.APPLICATION_JSON_VALUE)
			.body(request)
			.post("/manager/leagues")
			.then().log().all()
			.extract();

		// then
		assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
	}
}
