package com.sports.server.game.acceptance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.sports.server.game.dto.response.GameDetailResponse;
import com.sports.server.game.dto.response.GameResponseDto;
import com.sports.server.support.AcceptanceTest;
import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;

@Sql(scripts = "/game-fixture.sql")
class GameAcceptanceTest extends AcceptanceTest {

    final int lastPkOfFixtureFromFirstLeague = 13;
    final int defaultSizeOfData = 10;

    @Test
    void 게임을_상세_조회한다() {
        // given
        Long basketBallGameId = 1L;

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .when()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .get("/games/{gameId}", basketBallGameId)
                .then().log().all()
                .extract();

        // then
        GameDetailResponse game = toResponse(response, GameDetailResponse.class);
        List<GameDetailResponse.TeamResponse> teams = game.gameTeams();
        assertAll(
                () -> assertThat(game.gameName()).isEqualTo("농구 대전"),
                () -> assertThat(game.gameQuarter()).isEqualTo("1st Quarter"),
                () -> assertThat(game.videoId()).isEqualTo("abc123"),
                () -> assertThat(game.sportName()).isEqualTo("농구"),
                () -> assertThat(game.startTime()).isEqualTo(
                        LocalDateTime.of(2023, 11, 12, 10, 0, 0)
                ),

                () -> assertThat(teams).hasSize(2),
                () -> assertThat(teams)
                        .filteredOn(team -> team.gameTeamId().equals(1L))
                        .containsExactly(new GameDetailResponse.TeamResponse(
                                1L, "팀 A",
                                "http://example.com/logo_a.png", 1, 1)
                        ),
                () -> assertThat(teams)
                        .filteredOn(team -> team.gameTeamId().equals(2L))
                        .containsExactly(new GameDetailResponse.TeamResponse(
                                2L, "팀 B",
                                "http://example.com/logo_b.png", 2, 2)
                        )
        );
    }

    @Disabled
    @Test
    void 리그의_경기를_전체_조회한다() {

        // when
        ExtractableResponse<Response> response = RestAssured.given()
                .queryParam("status", "SCHEDULED")
                .queryParam("league_id", 1L)
                .log().all()
                .when()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .get("/games")
                .then().log().all()
                .extract();

        //then
        List<GameResponseDto> games = toResponses(response, GameResponseDto.class);
        assertAll(
                () -> assertThat(games).hasSize(defaultSizeOfData),
                () -> assertThat(games)
                        .filteredOn(game -> game.id().equals(1L))
                        .containsExactly(
                                new GameResponseDto(
                                        1L, LocalDateTime.of(2023, 11, 12, 10, 0, 0),
                                        "1st Quarter", "농구 대전",
                                        List.of(new GameResponseDto.TeamResponse(
                                                        1L, "팀 A", "http://example.com/logo_a.png", 1, 1
                                                ),
                                                new GameResponseDto.TeamResponse(
                                                        2L, "팀 B", "http://example.com/logo_b.png", 2, 2)),
                                        "농구"
                                )
                        ),
                () -> assertThat(games)
                        .filteredOn(game -> game.id().equals(2L))
                        .containsExactly(
                                new GameResponseDto(
                                        2L, LocalDateTime.of(2023, 11, 12, 10, 10, 0),
                                        "1st Quarter", "두번째로 빠른 경기",
                                        List.of(new GameResponseDto.TeamResponse(
                                                        3L, "팀 B", "http://example.com/logo_b.png", 0, 1),
                                                new GameResponseDto.TeamResponse(
                                                        4L, "팀 C", "http://example.com/logo_c.png", 0, 2)

                                        ),

                                        "농구"
                                )

                        )
        );

    }

    @Test
    void 스포츠_아이디가_여러개일_경우_해당하는_모든_경기를_반환한다() {

        // when
        ExtractableResponse<Response> response = RestAssured.given()
                .queryParam("status", "SCHEDULED")
                .queryParam("league_id", 1L)
                .queryParam("size", lastPkOfFixtureFromFirstLeague)
                .queryParam("sport_id", 1L)
                .queryParam("sport_id", 2L)
                .log().all()
                .when()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .get("/games")
                .then().log().all()
                .extract();

        // then
        List<GameResponseDto> games = toResponses(response, GameResponseDto.class);
        assertThat(games).hasSize(lastPkOfFixtureFromFirstLeague);

    }
}
