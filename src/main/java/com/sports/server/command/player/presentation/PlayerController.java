package com.sports.server.command.player.presentation;

import com.sports.server.command.member.domain.Member;
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
    @ResponseStatus(HttpStatus.CREATED)
    public void register(@RequestBody PlayerRequest.Register request, Member member) {
        playerService.register(member, request);
    }

    @PatchMapping("/{playerId}")
    @ResponseStatus(HttpStatus.OK)
    public void update(@PathVariable Long playerId, @RequestBody PlayerRequest.Update update, Member member) {
        playerService.update(member, playerId, update);
    }

    @DeleteMapping("/{playerId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long playerId, Member member) {
        playerService.delete(member, playerId);
    }
}
