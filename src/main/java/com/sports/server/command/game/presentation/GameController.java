package com.sports.server.command.game.presentation;

import com.sports.server.command.game.application.GameService;
import com.sports.server.command.game.application.GameTeamService;
import com.sports.server.command.game.application.LineupPlayerService;
import com.sports.server.command.game.dto.CheerCountUpdateRequest;
import com.sports.server.command.game.dto.GameRequestDto;
import com.sports.server.command.member.domain.Member;
import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class GameController {

    private final GameTeamService gameTeamService;
    private final LineupPlayerService lineupPlayerService;
    private final GameService gameService;

    @PostMapping("/games/{gameId}/cheer")
    public ResponseEntity<Void> updateCheerCount(@PathVariable final Long gameId,
                                                 @RequestBody @Valid CheerCountUpdateRequest cheerRequestDto) {
        gameTeamService.updateCheerCount(gameId, cheerRequestDto);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/games/{gameId}/lineup-players/{lineupPlayerId}/starter")
    public ResponseEntity<Void> changePlayerStateToStarter(@PathVariable final Long gameId,
                                                           @PathVariable final Long lineupPlayerId) {
        lineupPlayerService.changePlayerStateToStarter(gameId, lineupPlayerId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/games/{gameId}/lineup-players/{lineupPlayerId}/candidate")
    public ResponseEntity<Void> changePlayerStateToCandidate(@PathVariable final Long gameId,
                                                             @PathVariable final Long lineupPlayerId) {
        lineupPlayerService.changePlayerStateToCandidate(gameId, lineupPlayerId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/leagues/{leagueId}/games")
    public ResponseEntity<Void> registerGame(@PathVariable final Long leagueId,
                                             @RequestBody final GameRequestDto.Register requestDto,
                                             final Member member) {
        gameService.register(leagueId, requestDto, member);
        return ResponseEntity.created(URI.create("")).build();
    }

    @PutMapping("/leagues/{leagueId}/{gameId}")
    public ResponseEntity<Void> updateGame(@PathVariable final Long leagueId,
                                           @PathVariable final Long gameId,
                                           @RequestBody final GameRequestDto.Update requestDto,
                                           final Member member) {
        gameService.updateGame(leagueId, gameId, requestDto, member);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/games/{gameId}/{gameTeamId}/lineup-players/{lineupPlayerId}/captain/register")
    public ResponseEntity<Void> changePlayerToCaptain(@PathVariable final Long gameId,
                                                      @PathVariable final Long lineupPlayerId,
                                                      @PathVariable final Long gameTeamId) {
        lineupPlayerService.changePlayerToCaptain(gameId, gameTeamId, lineupPlayerId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/games/{gameId}/{gameTeamId}/lineup-players/{lineupPlayerId}/captain/revoke")
    public ResponseEntity<Void> revokeCaptainFromPlayer(@PathVariable final Long gameId,
                                                        @PathVariable final Long lineupPlayerId,
                                                        @PathVariable final Long gameTeamId) {
        lineupPlayerService.revokeCaptainFromPlayer(gameId, gameTeamId, lineupPlayerId);
        return ResponseEntity.ok().build();
    }
}
