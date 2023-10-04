package com.sports.server.game.presentation;

import com.sports.server.game.application.GameService;
import com.sports.server.game.dto.request.GameRegisterRequestDto;
import com.sports.server.game.dto.response.GameResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@RequestMapping("/games")
public class GameController {

    private final GameService gameService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody @Valid final GameRegisterRequestDto requestDto) {
        Long gameId = gameService.register(requestDto);
        return ResponseEntity.created(URI.create("/games/" + gameId)).build();
    }

    @GetMapping("/{gameId}")
    public ResponseEntity<GameResponseDto> getOneGame(@PathVariable final Long gameId) {
        return ResponseEntity.ok(gameService.getOneGame(gameId));
    }
}
