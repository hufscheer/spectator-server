package com.sports.server.game.presentation;

import com.sports.server.comment.application.CommentService;
import com.sports.server.comment.dto.response.CommentResponseDto;
import com.sports.server.game.application.GameService;
import com.sports.server.game.dto.request.GameRegisterRequestDto;
import com.sports.server.game.dto.response.GameDetailResponseDto;
import com.sports.server.game.dto.response.GameResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/games")
public class GameController {

    private final GameService gameService;

    private final CommentService commentService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody @Valid final GameRegisterRequestDto requestDto) {
        Long gameId = gameService.register(requestDto);
        return ResponseEntity.created(URI.create("/games/" + gameId)).build();
    }

    @GetMapping("/{gameId}")
    public ResponseEntity<GameDetailResponseDto> getOneGame(@PathVariable final Long gameId) {
        return ResponseEntity.ok(gameService.getOneGame(gameId));
    }

    @GetMapping
    public ResponseEntity<List<GameResponseDto>> getOneGame() {
        return ResponseEntity.ok(gameService.getAllGames());
    }

    @GetMapping("/{gameId}/comments")
    public ResponseEntity<List<CommentResponseDto>> getAllComments(@PathVariable final Long gameId) {
        return ResponseEntity.ok(commentService.getAllCommentsWithGameId(gameId));
    }
}
