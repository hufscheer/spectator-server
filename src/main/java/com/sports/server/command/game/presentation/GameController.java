package com.sports.server.command.game.presentation;

import com.sports.server.command.game.application.GameTeamService;
import com.sports.server.command.game.dto.request.GameTeamCheerRequestDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/games")
public class GameController {

    private final GameTeamService gameTeamService;

    @PostMapping("/{gameId}/cheer")
    public ResponseEntity<Void> updateCheerCount(@PathVariable final Long gameId,
                                                                           @RequestBody @Valid GameTeamCheerRequestDto cheerRequestDto) {
        gameTeamService.updateCheerCount(gameId, cheerRequestDto);
        return ResponseEntity.ok().build();
    }
}
