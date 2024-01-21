package com.sports.server.command.game.presentation;

import com.sports.server.command.game.application.GameTeamService;
import com.sports.server.command.game.dto.CheerCountUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/games")
public class GameController {

    private final GameTeamService gameTeamService;

    @PostMapping("/{gameId}/cheer")
    public ResponseEntity<Void> updateCheerCount(@PathVariable final Long gameId,
                                                 @RequestBody @Valid CheerCountUpdateRequest cheerRequestDto) {
        gameTeamService.updateCheerCount(gameId, cheerRequestDto);
        return ResponseEntity.ok().build();
    }
}
