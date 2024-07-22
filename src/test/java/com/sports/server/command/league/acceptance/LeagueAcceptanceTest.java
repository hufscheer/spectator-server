package com.sports.server.command.league.acceptance;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;

import com.sports.server.command.league.dto.LeagueRequestDto;
import com.sports.server.support.AcceptanceTest;

import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;

@Sql("/league-fixture.sql")
public class LeagueAcceptanceTest extends AcceptanceTest {
	@Test
	void 대회를_저장한다() throws Exception {
		// given
		LeagueRequestDto.Register request = new LeagueRequestDto.Register(1L, "우물정 제기차기 대회", "4강", LocalDateTime.now(),
			LocalDateTime.now());

		configureMockJwtForEmail("john.doe@example.com");

		// when
		ExtractableResponse<Response> response = RestAssured.given().log().all()
			.cookie(COOKIE_NAME, mockToken)
			.contentType(MediaType.APPLICATION_JSON_VALUE)
			.body(request)
			.post("/leagues")
			.then().log().all()
			.extract();

		// then
		assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
	}
}
