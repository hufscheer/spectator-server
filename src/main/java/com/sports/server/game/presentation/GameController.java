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
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/games")
public class GameController {

    private final GameService gameService;
    private final GameTeamService gameTeamService;
    private final GameTeamPlayerService gameTeamPlayerService;

    @GetMapping("/{gameId}")
    public ResponseEntity<GameDetailResponse> getGameDetail(@PathVariable final Long gameId) {
        return ResponseEntity.ok(gameService.getGameDetail(gameId));
    }

    @GetMapping
    public ResponseEntity<List<GameResponseDto>> getAllGames() {
        return ResponseEntity.ok(gameService.getAllGames());
    }

    @GetMapping("/{gameId}/cheer")
    public ResponseEntity<List<GameTeamCheerResponseDto>> getCheerCountOfGameTeams(
            @PathVariable final Long gameId
    ) {
        return ResponseEntity.ok(gameTeamService.getCheerCountOfGameTeams(gameId));
    }

    @PostMapping("/{gameId}/cheer")
    public ResponseEntity<List<GameTeamCheerResponseDto>> updateCheerCount(@PathVariable final Long gameId,
                                                                           @RequestBody @Valid GameTeamCheerRequestDto cheerRequestDto) {
        gameTeamService.updateCheerCount(gameId, cheerRequestDto);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{gameId}/lineup")
    public ResponseEntity<List<GameLineupResponse>> getGameLineup(@PathVariable final Long gameId) {
        return ResponseEntity.ok(gameTeamPlayerService.getLineup(gameId));
    }
}
