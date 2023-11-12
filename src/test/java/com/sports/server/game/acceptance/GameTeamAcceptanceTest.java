package com.sports.server.game.acceptance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.sports.server.game.dto.request.GameTeamCheerRequestDto;
import com.sports.server.game.dto.response.GameTeamCheerResponseDto;
import com.sports.server.support.AcceptanceTest;
import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;

@Sql(scripts = "/game-fixture.sql")
public class GameTeamAcceptanceTest extends AcceptanceTest {

    @Test
    void 경기에_참여하는_팀을_응원한다() {

        //given
        GameTeamCheerRequestDto cheerRequestDto = new GameTeamCheerRequestDto();
        Long gameId = 1L;
        int cheerCountBeforePost = 1;

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
        List<GameTeamCheerResponseDto> actual = toResponses(response, GameTeamCheerResponseDto.class);
        assertAll(
                () -> assertThat(actual)
                        .map(GameTeamCheerResponseDto::cheerCount)
                        .isEqualTo(cheerCountBeforePost)
        );
    }
}
