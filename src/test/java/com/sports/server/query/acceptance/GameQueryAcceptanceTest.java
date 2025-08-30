package com.sports.server.query.acceptance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.sports.server.query.dto.response.GameDetailResponse;
import com.sports.server.query.dto.response.GameResponseDto;
import com.sports.server.query.dto.response.LeagueWithGamesResponse;
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
        Long soccerGameId = 1L;

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .when()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .get("/games/{gameId}", soccerGameId)
                .then().log().all()
                .extract();

        // then
        GameDetailResponse game = toResponse(response, GameDetailResponse.class);
        List<GameDetailResponse.TeamResponse> teams = game.gameTeams();
        assertAll(
                () -> assertThat(game.gameName()).isEqualTo("축구 대전"),
                () -> assertThat(game.gameQuarter()).isEqualTo("1st Quarter"),
                () -> assertThat(game.videoId()).isEqualTo("abc123"),
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
                .queryParam("leagueTeamIds", 1L)
                .queryParam("leagueTeamIds", 2L)
                .log().all()
                .when()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .get("/games")
                .then().log().all()
                .extract();

        //then
        List<LeagueWithGamesResponse> gamesWithLeague = toResponses(response, LeagueWithGamesResponse.class);
        LeagueWithGamesResponse gameWithLeague = gamesWithLeague.get(0);
        List<GameResponseDto> games = gameWithLeague.games();

        assertAll(
                () -> assertThat(games).hasSize(9),
                () -> assertThat(games)
                        .map(GameResponseDto::id)
                        .containsExactly(1L, 2L, 3L, 4L, 6L, 7L, 5L, 8L, 9L),

                () -> assertThat(games)
                        .filteredOn(game -> game.id().equals(1L))
                        .usingRecursiveFieldByFieldElementComparator() // 중첩 객체 비교를 위해 추가
                        .containsExactly(
                                new GameResponseDto(
                                        1L, LocalDateTime.of(2023, 11, 12, 10, 0, 0),
                                        "1st Quarter", "축구 대전", 4, "abc123",
                                        List.of(new GameResponseDto.TeamResponse(
                                                        1L, "팀 A", "http://example.com/logo_a.png", 1, 0
                                                ),
                                                new GameResponseDto.TeamResponse(
                                                        2L, "팀 B", "http://example.com/logo_b.png", 2, 0)), false
                                )
                        ),
                () -> assertThat(games)
                        .filteredOn(game -> game.id().equals(2L))
                        .usingRecursiveFieldByFieldElementComparator() // 중첩 객체 비교를 위해 추가
                        .containsExactly(
                                new GameResponseDto(
                                        2L, LocalDateTime.of(2023, 11, 12, 10, 10, 0),
                                        "1st Quarter", "두번째로 빠른 경기", 4, "abc123",
                                        List.of(new GameResponseDto.TeamResponse(
                                                        3L, "팀 B", "http://example.com/logo_b.png", 0, 0),
                                                new GameResponseDto.TeamResponse(
                                                        4L, "팀 C", "http://example.com/logo_c.png", 0, 0)
                                        ), false
                                )
                        )
        );
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
                        .containsExactly("선수11", "선수12", "선수13", "선수14", "선수15"),
                () -> assertThat(teamAPlayers)
                        .map(LineupPlayerResponse.PlayerResponse::jerseyNumber)
                        .containsExactly(1, 2, 3, 4, 5),
                () -> assertThat(teamAPlayers)
                        .map(LineupPlayerResponse.PlayerResponse::isCaptain)
                        .containsOnly(false),
                () -> assertThat(teamB.teamName()).isEqualTo("팀 B"),
                () -> assertThat(teamBPlayers)
                        .map(LineupPlayerResponse.PlayerResponse::playerName)
                        .containsExactly("선수16", "선수17", "선수18", "선수19", "선수20"),
                () -> assertThat(teamBPlayers)
                        .map(LineupPlayerResponse.PlayerResponse::jerseyNumber)
                        .containsExactly(1, 2, 3, 4, 5),
                () -> assertThat(teamBPlayers)
                        .map(LineupPlayerResponse.PlayerResponse::isCaptain)
                        .containsExactly(true, false, false, false, false)
        );
    }

    @Test
    void 출전_선수를_조회한다() {
        // given
        Long soccerGameId = 1L;
        Long gameTeamAId = 1L;
        Long gameTeamBId = 2L;

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .when()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .get("/games/{gameId}/lineup/playing", soccerGameId)
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
                        .containsExactly("선수15"),
                () -> assertThat(teamA.gameTeamPlayers())
                        .map(LineupPlayerResponse.PlayerResponse::jerseyNumber)
                        .containsExactly(5),
                () -> assertThat(teamB.teamName()).isEqualTo("팀 B"),
                () -> assertThat(teamB.gameTeamPlayers())
                        .map(LineupPlayerResponse.PlayerResponse::playerName)
                        .containsExactly("선수20"),
                () -> assertThat(teamB.gameTeamPlayers())
                        .map(LineupPlayerResponse.PlayerResponse::jerseyNumber)
                        .containsExactly(5)
        );
    }

}
