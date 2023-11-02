package com.sports.server.game.presentation;

import com.sports.server.game.application.GameService;
import com.sports.server.game.dto.response.GameDetailResponseDto;
import com.sports.server.game.dto.response.GameResponseDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/games")
public class GameController {

    private final GameService gameService;

    @GetMapping("/{gameId}")
    public ResponseEntity<GameDetailResponseDto> getOneGame(@PathVariable final Long gameId) {
        return ResponseEntity.ok(gameService.getOneGame(gameId));
    }

    @GetMapping
    public ResponseEntity<List<GameResponseDto>> getAllGames() {
        return ResponseEntity.ok(gameService.getAllGames());
    }
}
