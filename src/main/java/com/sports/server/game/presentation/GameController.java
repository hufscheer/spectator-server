package com.sports.server.game.presentation;

import com.sports.server.game.application.GameService;
import com.sports.server.game.application.GameTeamPlayerService;
import com.sports.server.game.application.GameTeamService;
import com.sports.server.game.dto.request.GameTeamCheerRequestDto;
import com.sports.server.game.dto.response.GameDetailResponse;
import com.sports.server.game.dto.response.GameLineupResponse;
import com.sports.server.game.dto.response.GameResponseDto;
import com.sports.server.game.dto.response.GameTeamCheerResponseDto;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;
    private final GameTeamService gameTeamService;
    private final GameTeamPlayerService gameTeamPlayerService;

    @GetMapping("/games/{gameId}")
    public ResponseEntity<GameDetailResponse> getGameDetail(@PathVariable final Long gameId) {
        return ResponseEntity.ok(gameService.getGameDetail(gameId));
    }

    @GetMapping("/leagues/{leagueId}/games")
    public ResponseEntity<List<GameResponseDto>> getAllGamesWithLeague(@PathVariable final Long leagueId) {
        return ResponseEntity.ok(gameService.getAllGamesWithLeague(leagueId));
    }

    @GetMapping("/games/{gameId}/cheer")
    public ResponseEntity<List<GameTeamCheerResponseDto>> getCheerCountOfGameTeams(
            @PathVariable final Long gameId
    ) {
        return ResponseEntity.ok(gameTeamService.getCheerCountOfGameTeams(gameId));
    }

    @PostMapping("/games/{gameId}/cheer")
    public ResponseEntity<List<GameTeamCheerResponseDto>> updateCheerCount(@PathVariable final Long gameId,
                                                                           @RequestBody @Valid GameTeamCheerRequestDto cheerRequestDto) {
        gameTeamService.updateCheerCount(gameId, cheerRequestDto);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/games/{gameId}/lineup")
    public ResponseEntity<List<GameLineupResponse>> getGameLineup(@PathVariable final Long gameId) {
        return ResponseEntity.ok(gameTeamPlayerService.getLineup(gameId));
    }
}
