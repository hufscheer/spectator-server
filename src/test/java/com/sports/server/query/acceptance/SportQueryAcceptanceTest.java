package com.sports.server.query.acceptance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.sports.server.query.dto.response.SportResponse;
import com.sports.server.support.AcceptanceTest;
import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;

@Sql("/game-fixture.sql")
public class SportQueryAcceptanceTest extends AcceptanceTest {

    @Test
    void 모든_스포츠를_조회한다() {

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .when()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .get("/sports")
                .then().log().all()
                .extract();

        // then
        List<SportResponse> actual = toResponses(response, SportResponse.class);
        assertAll(
                () -> assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value()),
                () -> assertThat(actual)
                        .map(SportResponse::id)
                        .containsExactly(1L, 2L, 3L),
                () -> assertThat(actual)
                        .map(SportResponse::name)
                        .containsExactly("농구", "루미큐브", "축구"));
    }

}
