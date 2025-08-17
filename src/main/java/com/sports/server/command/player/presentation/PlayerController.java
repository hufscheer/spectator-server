package com.sports.server.command.player.presentation;

import com.sports.server.command.player.application.PlayerService;
import com.sports.server.command.player.dto.PlayerRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@RequestMapping("/players")
public class PlayerController {
    private final PlayerService playerService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Long> register(@RequestBody PlayerRequest.Register request) {
        Long playerId = playerService.register(request);
        return ResponseEntity.created(URI.create("/players/" + playerId)).body(playerId);
    }

    @PatchMapping("/{playerId}")
    @ResponseStatus(HttpStatus.OK)
    public void update(@PathVariable Long playerId, @RequestBody PlayerRequest.Update update) {
        playerService.update(playerId, update);
    }

    @DeleteMapping("/{playerId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long playerId) {
        playerService.delete(playerId);
    }
}
