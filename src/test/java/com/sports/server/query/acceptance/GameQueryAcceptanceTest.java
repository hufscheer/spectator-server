package com.sports.server.query.acceptance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.sports.server.query.dto.response.GameDetailResponse;
import com.sports.server.query.dto.response.GameResponseDto;
import com.sports.server.query.dto.response.LineupPlayerResponse;
import com.sports.server.support.AcceptanceTest;
import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;

@Sql(scripts = "/game-fixture.sql")
public class GameQueryAcceptanceTest extends AcceptanceTest {

    private final int defaultSizeOfData = 10;

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

    @Test
    void 리그의_경기를_전체_조회한다() {

        // when
        ExtractableResponse<Response> response = RestAssured.given()
                .queryParam("state", "SCHEDULED")
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
        int lastPkOfFixtureFromFirstLeague = 13;
        ExtractableResponse<Response> response = RestAssured.given()
                .queryParam("state", "SCHEDULED")
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

    @Test
    void 경기_라인업을_조회한다() {
        // given
        Long basketBallGameId = 1L;
        Long gameTeamAId = 1L;
        Long gameTeamBId = 2L;

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .when()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .get("/games/{gameId}/lineup", basketBallGameId)
                .then().log().all()
                .extract();

        // then
        List<LineupPlayerResponse> actual = toResponses(response, LineupPlayerResponse.class);
        LineupPlayerResponse teamA = actual.stream()
                .filter(lineup -> lineup.gameTeamId().equals(gameTeamAId))
                .toList()
                .get(0);
        LineupPlayerResponse teamB = actual.stream()
                .filter(lineup -> lineup.gameTeamId().equals(gameTeamBId))
                .toList()
                .get(0);
        assertAll(
                () -> assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value()),
                () -> assertThat(actual).hasSize(2),
                () -> assertThat(actual)
                        .map(LineupPlayerResponse::gameTeamId)
                        .containsExactly(gameTeamAId, gameTeamBId),

                () -> assertThat(teamA.teamName()).isEqualTo("팀 A"),
                () -> assertThat(teamA.gameTeamPlayers())
                        .map(LineupPlayerResponse.PlayerResponse::playerName)
                        .containsExactly("선수1", "선수2", "선수3", "선수4", "선수5"),
                () -> assertThat(teamA.gameTeamPlayers())
                        .map(LineupPlayerResponse.PlayerResponse::description)
                        .containsExactly("센터", "파워 포워드", "슈팅 가드", "포인트 가드", "스몰 포워드"),
                () -> assertThat(teamA.gameTeamPlayers())
                        .map(LineupPlayerResponse.PlayerResponse::number)
                        .containsExactly(1, 2, 3, 4, 5),
                () -> assertThat(teamA.gameTeamPlayers())
                        .map(LineupPlayerResponse.PlayerResponse::isCaptain)
                        .containsOnly(false),
                () -> assertThat(teamA.order())
                        .isEqualTo(1),
                () -> assertThat(teamB.teamName()).isEqualTo("팀 B"),
                () -> assertThat(teamB.gameTeamPlayers())
                        .map(LineupPlayerResponse.PlayerResponse::playerName)
                        .containsExactly("선수6", "선수7", "선수8", "선수9", "선수10"),
                () -> assertThat(teamB.gameTeamPlayers())
                        .map(LineupPlayerResponse.PlayerResponse::description)
                        .containsExactly("센터", "파워 포워드", "슈팅 가드", "포인트 가드", "스몰 포워드"),
                () -> assertThat(teamB.gameTeamPlayers())
                        .map(LineupPlayerResponse.PlayerResponse::number)
                        .containsExactly(1, 2, 3, 4, 5),
                () -> assertThat(teamB.gameTeamPlayers())
                        .map(LineupPlayerResponse.PlayerResponse::isCaptain)
                        .containsOnly(false),
                () -> assertThat(teamB.order())
                        .isEqualTo(2)
        );
    }
}
