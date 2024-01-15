package com.sports.server.command.cheertalk.acceptance;

import static org.assertj.core.api.Assertions.assertThat;

import com.sports.server.command.cheertalk.dto.CheerTalkRequest;
import com.sports.server.support.AcceptanceTest;
import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

public class CheerTalkAcceptanceTest extends AcceptanceTest {

    @Test
    void 새로운_응원톡이_저장된다() {
        // given
        String content = "파이팅!";
        Long gameTeamId = 1L;
        CheerTalkRequest cheerTalkRequest = new CheerTalkRequest(content, gameTeamId);

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .when()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(cheerTalkRequest)
                .post("/cheer-talks")
                .then().log().all()
                .extract();

        //then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    }
}
