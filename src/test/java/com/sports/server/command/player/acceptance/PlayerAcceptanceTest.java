package com.sports.server.command.player.acceptance;

import com.sports.server.command.player.domain.Player;
import com.sports.server.command.player.domain.PlayerRepository;
import com.sports.server.command.player.dto.PlayerRequest;
import com.sports.server.support.AcceptanceTest;
import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;

import static org.assertj.core.api.Assertions.assertThat;

@Sql("/member-fixture.sql")
public class PlayerAcceptanceTest extends AcceptanceTest {

    @Autowired
    private PlayerRepository playerRepository;

    @Test
    void 선수를_등록한다() {
        // given
        PlayerRequest.Register request = new PlayerRequest.Register("손흥민", "202500002");

        configureMockJwtForEmail("admin@example.com");

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .cookie(COOKIE_NAME, mockToken)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request)
                .post("/players")
                .then().log().all()
                .extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value());
    }

    @Test
    void 선수_정보를_수정한다() {
        // given
        Player savedPlayer = new Player("박지성", "202500001");
        playerRepository.save(savedPlayer);

        PlayerRequest.Update request = new PlayerRequest.Update("손흥민", "202500002");
        configureMockJwtForEmail("admin@example.com");

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .cookie(COOKIE_NAME, mockToken)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request)
                .patch("/players/{playerId}", savedPlayer.getId())
                .then().log().all()
                .extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    void 선수를_삭제한다() {
        // given
        Player player = new Player("손흥민", "202500001");
        playerRepository.save(player);

        configureMockJwtForEmail("admin@example.com");

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .cookie(COOKIE_NAME, mockToken)
                .pathParam("playerId", player.getId())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .delete("/players/{playerId}")
                .then().log().all()
                .extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    }

}
