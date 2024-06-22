package com.sports.server.auth.acceptance;

import static org.assertj.core.api.Assertions.assertThat;

import com.sports.server.auth.dto.LoginRequest;
import com.sports.server.support.AcceptanceTest;
import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;

@Sql(scripts = "/member-fixture.sql")
public class AuthAcceptanceTest extends AcceptanceTest {

    @Value("${cookie.name}")
    private String COOKIE_NAME;

    @Test
    void 매니저_로그인을_한다() {
        // given
        String email = "john@example.com";
        String password = "1234";
        LoginRequest loginRequest = new LoginRequest(email, password);

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .when()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(loginRequest)
                .post("/manager/login")
                .then().log().all()
                .extract();

        //then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    void 로그인_시에_쿠키가_발행된다() {
        // given
        String email = "john@example.com";
        String password = "1234";
        LoginRequest loginRequest = new LoginRequest(email, password);

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .when()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(loginRequest)
                .post("/manager/login")
                .then().log().all()
                .extract();

        // then
        String cookieValue = response.cookie(COOKIE_NAME);
        assertThat(cookieValue).isNotNull();
    }
}

