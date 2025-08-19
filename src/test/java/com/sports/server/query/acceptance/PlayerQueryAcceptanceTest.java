package com.sports.server.query.acceptance;

import com.sports.server.command.player.domain.Player;
import com.sports.server.command.player.domain.PlayerRepository;
import com.sports.server.query.dto.response.PlayerResponse;
import com.sports.server.support.AcceptanceTest;
import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

public class PlayerQueryAcceptanceTest extends AcceptanceTest {

    @Autowired
    private PlayerRepository playerRepository;

    private Player player1;
    private Player player2;

    @Override
    @BeforeEach
    protected void setUp() {
        super.setUp();
        player1 = new Player("손흥민", "202500111");
        player2 = new Player("박지성", "202500112");
        playerRepository.save(player1);
        playerRepository.save(player2);
    }

    @Test
    void 모든_선수를_조회한다() {
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .when()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .get("/players")
                .then().log().all()
                .extract();

        // then
        List<PlayerResponse> actual = toResponses(response, PlayerResponse.class);
        List<PlayerResponse> expected = List.of(new PlayerResponse(player1), new PlayerResponse(player2));

        assertAll(
                () -> assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value()),
                () -> assertThat(actual).usingRecursiveComparison().isEqualTo(expected)
        );
    }

    @Test
    void 선수를_상세_조회한다(){
        // given
        Long playerId = player1.getId();
        PlayerResponse expected = new PlayerResponse(player1);

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .when()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .get("/players/{playerId}", playerId)
                .then().log().all()
                .extract();

        // then
        PlayerResponse actual = toResponse(response, PlayerResponse.class);

        assertAll(
                () -> assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value()),
                () -> assertThat(actual).isEqualTo(expected)
        );

    }

}
