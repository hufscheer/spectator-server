package com.sports.server.query.presentation;

import com.sports.server.common.dto.PageRequestDto;
import com.sports.server.query.application.GameQueryService;
import com.sports.server.query.application.GameTeamQueryService;
import com.sports.server.query.application.LineupPlayerQueryService;
import com.sports.server.query.dto.request.GamesQueryRequestDto;
import com.sports.server.query.dto.response.*;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/games")
public class GameQueryController {

    private final GameQueryService gameQueryService;
    private final GameTeamQueryService gameTeamQueryService;
    private final LineupPlayerQueryService lineupPlayerQueryService;

    @GetMapping("/{gameId}")
    public ResponseEntity<GameDetailResponse> getGameDetail(@PathVariable final Long gameId) {
        return ResponseEntity.ok(gameQueryService.getGameDetail(gameId));
    }

    @GetMapping("{gameId}/video")
    public ResponseEntity<VideoResponse> getVideo(@PathVariable final Long gameId) {
        return ResponseEntity.ok(gameQueryService.getVideo(gameId));
    }

    @GetMapping
    public ResponseEntity<List<LeagueWithGamesResponse>> getAllGames(
            @ModelAttribute GamesQueryRequestDto queryRequestDto,
            @ModelAttribute PageRequestDto pageRequest) {
        return ResponseEntity.ok(gameQueryService.getAllGames(queryRequestDto, pageRequest));
    }

    @GetMapping("/{gameId}/cheer")
    public ResponseEntity<List<GameTeamCheerResponseDto>> getCheerCountOfGameTeams(
            @PathVariable final Long gameId
    ) {
        return ResponseEntity.ok(gameTeamQueryService.getCheerCountOfGameTeams(gameId));
    }

    @GetMapping("/{gameId}/lineup")
    public ResponseEntity<List<LineupPlayerResponse.All>> getGameLineup(@PathVariable final Long gameId) {
        return ResponseEntity.ok(lineupPlayerQueryService.getLineup(gameId));
    }

    @GetMapping("/{gameId}/lineup/playing")
    public ResponseEntity<List<LineupPlayerResponse.Playing>> getGamePlayingLineup(@PathVariable final Long gameId) {
        return ResponseEntity.ok(lineupPlayerQueryService.getPlayingLineup(gameId));
    }

    @GetMapping("/search")
    public ResponseEntity<List<GameDetailResponse>> getGamesByYearAndMonth(
            @RequestParam Integer year,
            @RequestParam Integer month) {
        return ResponseEntity.ok(gameQueryService.getGamesByYearAndMonth(year, month));
    }
}
