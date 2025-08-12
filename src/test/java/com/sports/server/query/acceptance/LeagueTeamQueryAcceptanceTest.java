//package com.sports.server.query.acceptance;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.junit.jupiter.api.Assertions.assertAll;
//
//import com.sports.server.query.dto.response.LeagueTeamResponse;
//import com.sports.server.support.AcceptanceTest;
//import io.restassured.RestAssured;
//import io.restassured.response.ExtractableResponse;
//import io.restassured.response.Response;
//import java.util.List;
//import org.junit.jupiter.api.Test;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.MediaType;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.test.context.jdbc.Sql;
//
//@ActiveProfiles("dev")
//@Sql(scripts = "/game-fixture.sql")
//public class LeagueTeamQueryAcceptanceTest extends AcceptanceTest {
//
//    @Test
//    void 리그의_모든_리그팀을_조회한다() {
//        // given
//        Long threeBuildingCup = 1L;
//
//        // when
//        ExtractableResponse<Response> response = RestAssured.given().log().all()
//                .when()
//                .contentType(MediaType.APPLICATION_JSON_VALUE)
//                .get("/leagues/{leagueId}/teams", threeBuildingCup)
//                .then().log().all()
//                .extract();
//
//        // then
//        List<LeagueTeamResponse> actual = toResponses(response, LeagueTeamResponse.class);
//        assertAll(
//                () -> assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value()),
//                () -> assertThat(actual)
//                        .map(LeagueTeamResponse::teamName)
//                        .containsExactly("팀 A", "팀 B", "팀 C", "팀 D"),
//                () -> assertThat(actual)
//                        .map(LeagueTeamResponse::teamId)
//                        .containsExactly(1L, 2L, 3L, 4L)
//
//        );
//    }
//
//    @Test
//    void 라운드에_진출한_리그팀만_조회한다() {
//        // given
//        Long threeBuildingCup = 1L;
//        int finalRound = 2;
//
//        // when
//        ExtractableResponse<Response> response = RestAssured.given().log().all()
//                .when()
//                .contentType(MediaType.APPLICATION_JSON_VALUE)
//                .param("round", finalRound)
//                .get("/leagues/{leagueId}/teams", threeBuildingCup)
//                .then().log().all()
//                .extract();
//
//        // then
//        List<LeagueTeamResponse> actual = toResponses(response, LeagueTeamResponse.class);
//        assertAll(
//                () -> assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value()),
//                () -> assertThat(actual)
//                        .map(LeagueTeamResponse::teamName)
//                        .containsExactly("팀 B", "팀 D"),
//                () -> assertThat(actual)
//                        .map(LeagueTeamResponse::teamId)
//                        .containsExactly(2L, 4L)
//        );
//    }
//}
