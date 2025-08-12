package com.sports.server.query.acceptance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.sports.server.query.dto.response.MemberResponse;
import com.sports.server.support.AcceptanceTest;
import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

@ActiveProfiles("test")
@Sql("/member-fixture.sql")
public class MemberQueryAcceptanceTest extends AcceptanceTest {

    @Test
    void 멤버의_정보를_조회한다() {

        // given
        String email = "john@example.com";
        configureMockJwtForEmail(email);

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .when()
                .cookie(COOKIE_NAME, mockToken)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .get("/members/info")
                .then().log().all()
                .extract();

        // then
        MemberResponse actual = toResponse(response, MemberResponse.class);
        assertAll(
                () -> assertThat(actual.email()).isEqualTo(email),
                () -> assertThat(actual.nameOfOrganization()).isEqualTo("축구 협회")
        );
    }

}
