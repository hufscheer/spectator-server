package com.sports.server.game.presentation;

import com.sports.server.game.application.GameService;
import com.sports.server.game.dto.request.GameRegisterRequestDto;
import com.sports.server.game.dto.response.GameResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/games")
public class GameController {

    private final GameService gameService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody @Valid GameRegisterRequestDto requestDto) {
        gameService.register(requestDto);
        // TODO: game 전체 조회 기능 구현 이후에 ResponseEntity.created 로 변경하기
        return ResponseEntity.ok("");
    }

    @GetMapping("/{gameId}")
    public ResponseEntity<GameResponseDto> getOneGame(@PathVariable Long gameId) {
        return ResponseEntity.ok(gameService.getOneGame(gameId));
    }
}
