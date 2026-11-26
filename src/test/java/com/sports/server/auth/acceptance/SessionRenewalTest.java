package com.sports.server.auth.acceptance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.sports.server.auth.dto.LoginRequest;
import com.sports.server.auth.utils.JwtUtil;
import com.sports.server.support.config.AsyncTestConfig;
import com.sports.server.support.isolation.DatabaseIsolation;
import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import java.time.Duration;
import java.util.Date;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;

/**
 * 토큰 재발급은 실제 JwtUtil 이 필요해서 JwtUtil 을 mock 으로 두는 AcceptanceTest 를 상속하지 않는다.
 * 수명 값은 운영(be-config)과 같게 둔다.
 */
@DatabaseIsolation
@Import(AsyncTestConfig.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
        "jwt.valid-time=86400000",
        "jwt.renew-before=12h",
        "jwt.max-session-age=7d",
})
@Sql(scripts = "/member-fixture.sql")
class SessionRenewalTest {

    private static final Duration VALID_TIME = Duration.ofHours(24);
    private static final Duration MAX_SESSION_AGE = Duration.ofDays(7);

    private static final long ADMIN_ID = 1L;
    private static final String ADMIN_EMAIL = "john@example.com";
    private static final long NON_ADMIN_ID = 4L;
    private static final String NON_ADMIN_EMAIL = "user4@example.com";

    @LocalServerPort
    private int port;

    @Autowired
    private JwtUtil jwtUtil;

    @Value("${jwt.secret-key}")
    private String secretKey;

    @Value("${cookie.name}")
    private String cookieName;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Nested
    @DisplayName("재발급 판단")
    class PlanRenewal {

        @Test
        void 남은_수명이_길면_재발급하지_않는다() {
            String token = token(ADMIN_ID, ADMIN_EMAIL, ago(Duration.ofHours(1)), later(Duration.ofHours(23)));

            assertThat(jwtUtil.planRenewal(token)).isEmpty();
        }

        @Test
        void 남은_수명이_짧으면_지금부터_수명만큼_연장하고_로그인_시각은_그대로_둔다() {
            Date authTime = ago(Duration.ofHours(23));
            String token = token(ADMIN_ID, ADMIN_EMAIL, authTime, later(Duration.ofHours(1)));

            Optional<JwtUtil.Renewal> renewal = jwtUtil.planRenewal(token);

            assertThat(renewal).isPresent();
            assertThat(renewal.get().expiresAt().getTime())
                    .isCloseTo(System.currentTimeMillis() + VALID_TIME.toMillis(), within(5_000L));
            assertThat(renewal.get().authTime()).isEqualTo(seconds(authTime));
        }

        @Test
        void 새_만료_시각은_처음_로그인부터_상한을_넘지_않는다() {
            Date authTime = ago(MAX_SESSION_AGE.minusHours(3));
            String token = token(ADMIN_ID, ADMIN_EMAIL, authTime, later(Duration.ofHours(1)));

            Optional<JwtUtil.Renewal> renewal = jwtUtil.planRenewal(token);

            assertThat(renewal).isPresent();
            assertThat(renewal.get().expiresAt().getTime())
                    .isEqualTo(seconds(authTime).getTime() + MAX_SESSION_AGE.toMillis());
        }

        @Test
        void 처음_로그인부터_상한을_넘기면_재발급하지_않는다() {
            String token = token(ADMIN_ID, ADMIN_EMAIL, ago(MAX_SESSION_AGE.plusMinutes(1)),
                    later(Duration.ofHours(1)));

            assertThat(jwtUtil.planRenewal(token)).isEmpty();
        }

        @Test
        void 로그인_시각_클레임이_없는_기존_토큰은_만료_시각에서_수명을_빼_로그인_시각으로_삼는다() {
            Date expiresAt = later(Duration.ofHours(1));
            String legacyToken = JWT.create()
                    .withSubject(ADMIN_EMAIL)
                    .withExpiresAt(expiresAt)
                    .withClaim("id", ADMIN_ID)
                    .withClaim("email", ADMIN_EMAIL)
                    .sign(Algorithm.HMAC512(secretKey));

            Optional<JwtUtil.Renewal> renewal = jwtUtil.planRenewal(legacyToken);

            assertThat(renewal).isPresent();
            assertThat(renewal.get().authTime().getTime())
                    .isEqualTo(seconds(expiresAt).getTime() - VALID_TIME.toMillis());
        }
    }

    @Nested
    @DisplayName("인증된 요청의 응답")
    class RenewOnRequest {

        @Test
        void 남은_수명이_길면_쿠키를_다시_내려주지_않는다() {
            ExtractableResponse<Response> response = getMemberInfo(
                    token(ADMIN_ID, ADMIN_EMAIL, ago(Duration.ofHours(1)), later(Duration.ofHours(23))));

            assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
            assertThat(response.headers().getValues(HttpHeaders.SET_COOKIE)).isEmpty();
        }

        @Test
        void 남은_수명이_짧으면_로그인과_같은_속성의_새_쿠키를_내려주고_그_쿠키로_계속_요청할_수_있다() {
            Date authTime = ago(Duration.ofHours(23));
            Date oldExpiresAt = later(Duration.ofHours(1));
            ExtractableResponse<Response> response = getMemberInfo(
                    token(ADMIN_ID, ADMIN_EMAIL, authTime, oldExpiresAt));

            assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
            String setCookie = response.header(HttpHeaders.SET_COOKIE);
            assertThat(setCookie).isNotNull();
            assertThat(attributesOf(setCookie)).isEqualTo(attributesOf(loginSetCookie()));

            String renewedToken = response.cookie(cookieName);
            DecodedJWT decoded = JWT.require(Algorithm.HMAC512(secretKey)).build().verify(renewedToken);
            assertThat(decoded.getExpiresAt()).isAfter(oldExpiresAt);
            assertThat(decoded.getClaim("auth_time").asDate()).isEqualTo(seconds(authTime));
            assertThat(decoded.getClaim("email").asString()).isEqualTo(ADMIN_EMAIL);

            ExtractableResponse<Response> next = getMemberInfo(renewedToken);
            assertThat(next.statusCode()).isEqualTo(HttpStatus.OK.value());
            assertThat(next.headers().getValues(HttpHeaders.SET_COOKIE)).isEmpty();
        }

        @Test
        void 공개_조회_요청에서도_만료가_가까우면_쿠키를_다시_내려준다() {
            ExtractableResponse<Response> response = RestAssured.given()
                    .cookie(cookieName, token(ADMIN_ID, ADMIN_EMAIL, ago(Duration.ofHours(23)),
                            later(Duration.ofHours(1))))
                    .when().get("/organizations")
                    .then().extract();

            assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
            assertThat(response.cookie(cookieName)).isNotNull();
        }

        @Test
        void 처음_로그인부터_상한을_넘기면_쿠키를_다시_내려주지_않는다() {
            ExtractableResponse<Response> response = getMemberInfo(token(ADMIN_ID, ADMIN_EMAIL,
                    ago(MAX_SESSION_AGE.plusMinutes(1)), later(Duration.ofHours(1))));

            assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
            assertThat(response.headers().getValues(HttpHeaders.SET_COOKIE)).isEmpty();
        }

        @Test
        void 만료된_토큰은_지금처럼_401이고_쿠키를_내려주지_않는다() {
            ExtractableResponse<Response> response = getMemberInfo(token(ADMIN_ID, ADMIN_EMAIL,
                    ago(Duration.ofHours(25)), ago(Duration.ofMinutes(1))));

            assertThat(response.statusCode()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
            assertThat(response.headers().getValues(HttpHeaders.SET_COOKIE)).isEmpty();
        }

        @Test
        void 다른_키로_서명한_토큰은_401이고_쿠키를_내려주지_않는다() {
            String forged = JWT.create()
                    .withSubject(ADMIN_EMAIL)
                    .withExpiresAt(later(Duration.ofHours(1)))
                    .withClaim("id", ADMIN_ID)
                    .withClaim("email", ADMIN_EMAIL)
                    .withClaim("auth_time", ago(Duration.ofHours(23)))
                    .sign(Algorithm.HMAC512("other-secret"));

            ExtractableResponse<Response> response = getMemberInfo(forged);

            assertThat(response.statusCode()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
            assertThat(response.headers().getValues(HttpHeaders.SET_COOKIE)).isEmpty();
        }

        @Test
        void 로그아웃으로_쿠키가_지워진_뒤에는_401이고_쿠키를_내려주지_않는다() {
            ExtractableResponse<Response> response = RestAssured.given()
                    .when().get("/members/info")
                    .then().extract();

            assertThat(response.statusCode()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
            assertThat(response.headers().getValues(HttpHeaders.SET_COOKIE)).isEmpty();
        }

        @Test
        void 관리자가_아닌_계정은_재발급하지_않는다() {
            ExtractableResponse<Response> response = getMemberInfo(token(NON_ADMIN_ID, NON_ADMIN_EMAIL,
                    ago(Duration.ofHours(23)), later(Duration.ofHours(1))));

            assertThat(response.headers().getValues(HttpHeaders.SET_COOKIE)).isEmpty();
        }
    }

    private ExtractableResponse<Response> getMemberInfo(String token) {
        return RestAssured.given()
                .cookie(cookieName, token)
                .when().get("/members/info")
                .then().extract();
    }

    private String loginSetCookie() {
        return RestAssured.given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(new LoginRequest(ADMIN_EMAIL, "1234"))
                .when().post("/manager/login")
                .then().statusCode(HttpStatus.OK.value())
                .extract().header(HttpHeaders.SET_COOKIE);
    }

    /** 값과 Expires(발급 시각에 따라 달라짐)를 뺀 쿠키 속성 */
    private static String attributesOf(String setCookie) {
        StringBuilder attributes = new StringBuilder();
        String[] parts = setCookie.split(";\\s*");
        attributes.append(parts[0], 0, parts[0].indexOf('=') + 1);
        for (int i = 1; i < parts.length; i++) {
            if (!parts[i].startsWith("Expires=")) {
                attributes.append("; ").append(parts[i]);
            }
        }
        return attributes.toString();
    }

    private String token(long id, String email, Date authTime, Date expiresAt) {
        return JWT.create()
                .withSubject(email)
                .withExpiresAt(expiresAt)
                .withClaim("id", id)
                .withClaim("email", email)
                .withClaim("auth_time", authTime)
                .sign(Algorithm.HMAC512(secretKey));
    }

    private static Date ago(Duration duration) {
        return new Date(System.currentTimeMillis() - duration.toMillis());
    }

    private static Date later(Duration duration) {
        return new Date(System.currentTimeMillis() + duration.toMillis());
    }

    /** JWT 의 날짜 클레임은 초 단위로 저장된다 */
    private static Date seconds(Date date) {
        return new Date(date.getTime() / 1000 * 1000);
    }
}
