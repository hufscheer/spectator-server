package com.sports.server.command.player.presentation;

import com.sports.server.command.player.application.PlayerService;
import com.sports.server.command.player.dto.PlayerRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/players")
public class PlayerController {
    private final PlayerService playerService;

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public void register(@RequestBody PlayerRequest.Register request) {
        playerService.register(request);
    }

    @PatchMapping("/{playerId}")
    @ResponseStatus(HttpStatus.OK)
    public void update(@PathVariable Long playerId, @RequestBody PlayerRequest.Update update) {
        playerService.update(playerId, update);
    }

    @DeleteMapping("/{playerId}")
    @ResponseStatus(HttpStatus.OK)
    public void delete(@PathVariable Long playerId) {
        playerService.delete(playerId);
    }
}
