package com.sports.server.query.acceptance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.sports.server.query.dto.response.TeamResponse;
import com.sports.server.query.dto.response.UnitResponse;
import com.sports.server.support.AcceptanceTest;
import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;

@Sql(scripts = "/team-query-fixture.sql")
public class TeamManagerQueryAcceptanceTest extends AcceptanceTest {

    @Test
    void 매니저는_자신의_조직에_속한_팀만_조회된다() {
        // given
        configureMockJwtForEmail("john.doe@example.com");

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .when()
                .cookie(COOKIE_NAME, mockToken)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .get("/manager/teams")
                .then().log().all()
                .extract();

        // then
        List<TeamResponse> actual = toResponses(response, TeamResponse.class);
        assertAll(
                () -> assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value()),
                () -> assertThat(actual).hasSize(7),
                () -> assertThat(actual)
                        .extracting(TeamResponse::name)
                        .doesNotContain("다른조직팀")
        );
    }

    @Test
    void 다른_조직의_매니저는_자신의_조직_팀만_조회된다() {
        // given
        configureMockJwtForEmail("non.manager@example.com");

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .when()
                .cookie(COOKIE_NAME, mockToken)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .get("/manager/teams")
                .then().log().all()
                .extract();

        // then
        List<TeamResponse> actual = toResponses(response, TeamResponse.class);
        assertAll(
                () -> assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value()),
                () -> assertThat(actual).hasSize(1),
                () -> assertThat(actual.get(0).name()).isEqualTo("다른조직팀")
        );
    }

    @Test
    void 매니저의_소속_기준으로_단과대별_팀_유무가_조회된다() {
        // given
        configureMockJwtForEmail("john.doe@example.com");

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .when()
                .cookie(COOKIE_NAME, mockToken)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .get("/manager/teams/units")
                .then().log().all()
                .extract();

        // then
        List<UnitResponse> actual = toResponses(response, UnitResponse.class);
        assertAll(
                () -> assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value()),
                () -> assertThat(actual).hasSize(3),
                () -> assertThat(actual)
                        .extracting(UnitResponse::unitName)
                        .containsExactlyInAnyOrder("사회과학대학", "기타", "영어대학")
        );
    }
}
