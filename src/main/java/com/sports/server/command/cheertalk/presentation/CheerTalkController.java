package com.sports.server.command.cheertalk.presentation;

import com.sports.server.command.cheertalk.application.CheerTalkService;
import com.sports.server.command.cheertalk.dto.CheerTalkRequest;
import com.sports.server.command.member.domain.Member;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/cheer-talks")
public class CheerTalkController {

    private final CheerTalkService cheerTalkService;

    @PostMapping
    public ResponseEntity<Void> register(@RequestBody @Valid final CheerTalkRequest cheerTalkRequest) {
        cheerTalkService.register(cheerTalkRequest);
        return ResponseEntity.ok(null);
    }

    @PatchMapping("/{leagueId}/{cheerTalkId}/block")
    public ResponseEntity<Void> block(@PathVariable Long leagueId,
                                      @PathVariable Long cheerTalkId,
                                      final Member manager) {
        cheerTalkService.block(leagueId, cheerTalkId, manager);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("{leagueId}/{cheerTalkId}/unblock")
    public ResponseEntity<Void> unblock(@PathVariable Long leagueId,
                                        @PathVariable Long cheerTalkId,
                                        final Member manager) {
        cheerTalkService.unblock(leagueId, cheerTalkId, manager);
        return ResponseEntity.ok().build();
    }
}
