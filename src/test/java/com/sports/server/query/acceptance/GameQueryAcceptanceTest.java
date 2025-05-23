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
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;

@Sql(scripts = "/game-fixture.sql")
public class GameQueryAcceptanceTest extends AcceptanceTest {

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
                () -> assertThat(game.round()).isEqualTo(4),
                () -> assertThat(teams).hasSize(2),
                () -> assertThat(teams)
                        .filteredOn(team -> team.gameTeamId().equals(1L))
                        .containsExactly(new GameDetailResponse.TeamResponse(
                                1L, "팀 A",
                                "http://example.com/logo_a.png", 1, 0)
                        ),
                () -> assertThat(teams)
                        .filteredOn(team -> team.gameTeamId().equals(2L))
                        .containsExactly(new GameDetailResponse.TeamResponse(
                                2L, "팀 B",
                                "http://example.com/logo_b.png", 2, 0)
                        ),
                () -> assertThat(game.state()).isEqualTo("SCHEDULED")
        );
    }

    @Test
    void 리그의_경기를_전체_조회한다() {

        // when
        ExtractableResponse<Response> response = RestAssured.given()
                .queryParam("state", "SCHEDULED")
                .queryParam("league_id", 1L)
                .queryParam("round", 4)
                .queryParam("league_team_id", 1L)
                .queryParam("league_team_id", 2L)
                .log().all()
                .when()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .get("/games")
                .then().log().all()
                .extract();

        //then
        List<GameResponseDto> games = toResponses(response, GameResponseDto.class);
        assertAll(
                () -> assertThat(games).hasSize(9),
                () -> assertThat(games)
                        .filteredOn(game -> game.id().equals(1L))
                        .containsExactly(
                                new GameResponseDto(
                                        1L, LocalDateTime.of(2023, 11, 12, 10, 0, 0),
                                        "1st Quarter", "농구 대전", 4, "abc123",
                                        List.of(new GameResponseDto.TeamResponse(
                                                        1L, "팀 A", "http://example.com/logo_a.png", 1, 0
                                                ),
                                                new GameResponseDto.TeamResponse(
                                                        2L, "팀 B", "http://example.com/logo_b.png", 2, 0)),
                                        "농구", false
                                )
                        ),
                () -> assertThat(games)
                        .filteredOn(game -> game.id().equals(2L))
                        .containsExactly(
                                new GameResponseDto(
                                        2L, LocalDateTime.of(2023, 11, 12, 10, 10, 0),
                                        "1st Quarter", "두번째로 빠른 경기", 4, "abc123",
                                        List.of(new GameResponseDto.TeamResponse(
                                                        3L, "팀 B", "http://example.com/logo_b.png", 0, 0),
                                                new GameResponseDto.TeamResponse(
                                                        4L, "팀 C", "http://example.com/logo_c.png", 0, 0)
                                        ),
                                        "농구", false
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
        List<LineupPlayerResponse.All> actual = toResponses(response, LineupPlayerResponse.All.class);
        LineupPlayerResponse.All teamA = actual.stream()
                .filter(lineup -> lineup.gameTeamId().equals(gameTeamAId))
                .toList()
                .get(0);
        LineupPlayerResponse.All teamB = actual.stream()
                .filter(lineup -> lineup.gameTeamId().equals(gameTeamBId))
                .toList()
                .get(0);
        List<LineupPlayerResponse.PlayerResponse> teamAPlayers = Stream.concat(
                teamA.candidatePlayers().stream(),
                teamA.starterPlayers().stream()
        ).toList();
        List<LineupPlayerResponse.PlayerResponse> teamBPlayers = Stream.concat(
                teamB.starterPlayers().stream(),
                teamB.candidatePlayers().stream()
        ).toList();
        assertAll(
                () -> assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value()),
                () -> assertThat(actual).hasSize(2),
                () -> assertThat(actual)
                        .map(LineupPlayerResponse.All::gameTeamId)
                        .containsExactly(gameTeamAId, gameTeamBId),

                () -> assertThat(teamA.teamName()).isEqualTo("팀 A"),
                () -> assertThat(teamAPlayers)
                        .map(LineupPlayerResponse.PlayerResponse::playerName)
                        .containsExactly("선수1", "선수2", "선수3", "선수4", "선수5"),
                () -> assertThat(teamAPlayers)
                        .map(LineupPlayerResponse.PlayerResponse::description)
                        .containsExactly("센터", "파워 포워드", "슈팅 가드", "포인트 가드", "스몰 포워드"),
                () -> assertThat(teamAPlayers)
                        .map(LineupPlayerResponse.PlayerResponse::number)
                        .containsExactly(1, 2, 3, 4, 5),
                () -> assertThat(teamAPlayers)
                        .map(LineupPlayerResponse.PlayerResponse::isCaptain)
                        .containsOnly(false),
                () -> assertThat(teamB.teamName()).isEqualTo("팀 B"),
                () -> assertThat(teamBPlayers)
                        .map(LineupPlayerResponse.PlayerResponse::playerName)
                        .containsExactly("선수6", "선수7", "선수8", "선수9", "선수10"),
                () -> assertThat(teamBPlayers)
                        .map(LineupPlayerResponse.PlayerResponse::description)
                        .containsExactly("센터", "파워 포워드", "슈팅 가드", "포인트 가드", "스몰 포워드"),
                () -> assertThat(teamBPlayers)
                        .map(LineupPlayerResponse.PlayerResponse::number)
                        .containsExactly(1, 2, 3, 4, 5),
                () -> assertThat(teamBPlayers)
                        .map(LineupPlayerResponse.PlayerResponse::isCaptain)
                        .containsExactly(true, false, false, false, false)
        );
    }

    @Test
    void 출전_선수를_조회한다() {
        // given
        Long basketBallGameId = 1L;
        Long gameTeamAId = 1L;
        Long gameTeamBId = 2L;

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .when()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .get("/games/{gameId}/lineup/playing", basketBallGameId)
                .then().log().all()
                .extract();

        // then
        List<LineupPlayerResponse.Playing> actual = toResponses(response, LineupPlayerResponse.Playing.class);
        LineupPlayerResponse.Playing teamA = actual.stream()
                .filter(lineup -> lineup.gameTeamId().equals(gameTeamAId))
                .toList()
                .get(0);
        LineupPlayerResponse.Playing teamB = actual.stream()
                .filter(lineup -> lineup.gameTeamId().equals(gameTeamBId))
                .toList()
                .get(0);
        assertAll(
                () -> assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value()),
                () -> assertThat(actual).hasSize(2),
                () -> assertThat(actual)
                        .map(LineupPlayerResponse.Playing::gameTeamId)
                        .containsExactly(gameTeamAId, gameTeamBId),

                () -> assertThat(teamA.teamName()).isEqualTo("팀 A"),
                () -> assertThat(teamA.gameTeamPlayers())
                        .map(LineupPlayerResponse.PlayerResponse::playerName)
                        .containsExactly("선수5"),
                () -> assertThat(teamA.gameTeamPlayers())
                        .map(LineupPlayerResponse.PlayerResponse::description)
                        .containsExactly("스몰 포워드"),
                () -> assertThat(teamA.gameTeamPlayers())
                        .map(LineupPlayerResponse.PlayerResponse::number)
                        .containsExactly(5),
                () -> assertThat(teamB.teamName()).isEqualTo("팀 B"),
                () -> assertThat(teamB.gameTeamPlayers())
                        .map(LineupPlayerResponse.PlayerResponse::playerName)
                        .containsExactly("선수10"),
                () -> assertThat(teamB.gameTeamPlayers())
                        .map(LineupPlayerResponse.PlayerResponse::description)
                        .containsExactly("스몰 포워드"),
                () -> assertThat(teamB.gameTeamPlayers())
                        .map(LineupPlayerResponse.PlayerResponse::number)
                        .containsExactly(5)
        );
    }

}
