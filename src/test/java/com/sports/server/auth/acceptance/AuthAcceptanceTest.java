package com.sports.server.auth.acceptance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.doThrow;

import com.sports.server.auth.dto.LoginRequest;
import com.sports.server.auth.exception.AuthorizationErrorMessages;
import com.sports.server.common.exception.UnauthorizedException;
import com.sports.server.support.AcceptanceTest;
import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;

@Sql(scripts = "/member-fixture.sql")
public class AuthAcceptanceTest extends AcceptanceTest {

    @Value("${cookie.name}")
    private String COOKIE_NAME;

    @Nested
    @DisplayName("로그인을 할 때 성공하는 경우")
    class LoginSuccessTest {

        String email = "john@example.com";
        String password = "1234";
        LoginRequest loginRequest = new LoginRequest(email, password);

        @Test
        void 예외를_던지지_않는다() {
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
        void 쿠키를_등록한다() {
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

        @Test
        void 만료된_토큰이_교체된다() {
            // given
            String expiredToken = "expired-token";

            doThrow(new UnauthorizedException("만료된 토큰입니다."))
                    .when(jwtUtil).validateToken(expiredToken);

            // when
            ExtractableResponse<Response> response = RestAssured.given().log().all()
                    .when()
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .cookie(COOKIE_NAME, "expired-token")
                    .body(loginRequest)
                    .post("/manager/login")
                    .then().log().all()
                    .extract();

            // then
            String cookieValue = response.cookie(COOKIE_NAME);

            assertDoesNotThrow(() -> {
                jwtUtil.validateToken(cookieValue);
            });
            assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        }
    }

    @Nested
    @DisplayName("로그인을 할 때")
    class LoginFailureTest {
        @Test
        void 존재하지_않는_이메일로_로그인을_시도하는_경우_예외를_던진다() {
            // given
            String invalidEmail = "not-exist@example.com";
            String password = "1234";
            LoginRequest loginRequest = new LoginRequest(invalidEmail, password);

            // when
            ExtractableResponse<Response> response = RestAssured.given().log().all()
                    .when()
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .body(loginRequest)
                    .post("/manager/login")
                    .then().log().all()
                    .extract();

            // then
            assertThat(response.statusCode()).isEqualTo(HttpStatus.NOT_FOUND.value());
            String errorMessage = response.jsonPath().getString("message");
            assertThat(errorMessage).isEqualTo(AuthorizationErrorMessages.MEMBER_NOT_FOUND_EXCEPTION);
        }

        @Test
        void 잘못된_비밀번호로_로그인을_시도하는_경우_예외를_던진다() {
            // given
            String email = "john@example.com";
            String invalidPassword = "invalid-password";
            LoginRequest loginRequest = new LoginRequest(email, invalidPassword);

            // when
            ExtractableResponse<Response> response = RestAssured.given().log().all()
                    .when()
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .body(loginRequest)
                    .post("/manager/login")
                    .then().log().all()
                    .extract();

            // then
            assertThat(response.statusCode()).isEqualTo(HttpStatus.NOT_FOUND.value());
            String errorMessage = response.jsonPath().getString("message");
            assertThat(errorMessage).isEqualTo(AuthorizationErrorMessages.MEMBER_NOT_FOUND_EXCEPTION);
        }

    }
}

