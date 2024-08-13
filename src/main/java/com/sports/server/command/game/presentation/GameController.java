package com.sports.server.command.game.presentation;

import com.sports.server.command.game.application.GameTeamService;
import com.sports.server.command.game.application.LineupPlayerService;
import com.sports.server.command.game.dto.CheerCountUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
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
    private final LineupPlayerService lineupPlayerService;

    @PostMapping("/{gameId}/cheer")
    public ResponseEntity<Void> updateCheerCount(@PathVariable final Long gameId,
                                                 @RequestBody @Valid CheerCountUpdateRequest cheerRequestDto) {
        gameTeamService.updateCheerCount(gameId, cheerRequestDto);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{gameId}/lineup-players/{lineupPlayerId}/starter")
    public ResponseEntity<Void> changePlayerStateToStarter(@PathVariable final Long gameId,
                                                           @PathVariable final Long lineupPlayerId) {
        lineupPlayerService.changePlayerStateToStarter(gameId, lineupPlayerId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{gameId}/lineup-players/{lineupPlayerId}/candidate")
    public ResponseEntity<Void> changePlayerStateToCandidate(@PathVariable final Long gameId,
                                                             @PathVariable final Long lineupPlayerId) {
        lineupPlayerService.changePlayerStateToCandidate(gameId, lineupPlayerId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{gameId}/lineup-players/{lineupPlayerId}/captain")
    public ResponseEntity<Void> changePlayerCaptainStatus(@PathVariable final Long gameId,
                                                          @PathVariable final Long lineupPlayerId) {
        lineupPlayerService.changePlayerCaptainStatus(gameId, lineupPlayerId);
        return ResponseEntity.ok().build();
    }

}
