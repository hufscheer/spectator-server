package com.sports.server.command.game.presentation;

import com.sports.server.command.game.application.GameService;
import com.sports.server.command.game.application.GameTeamService;
import com.sports.server.command.game.application.LineupPlayerService;
import com.sports.server.command.game.dto.CheerCountUpdateRequest;
import com.sports.server.command.game.dto.GameRequest;
import com.sports.server.command.member.domain.Member;
import jakarta.validation.Valid;
import java.net.URI;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class GameController {

    private final GameTeamService gameTeamService;
    private final LineupPlayerService lineupPlayerService;
    private final GameService gameService;

    @PostMapping("/games/{gameId}/cheer")
    @ResponseStatus(HttpStatus.OK)
    public void updateCheerCount(@PathVariable final Long gameId,
                                 @RequestBody @Valid CheerCountUpdateRequest cheerRequestDto) {
        gameTeamService.updateCheerCount(gameId, cheerRequestDto);
    }

    @PatchMapping("/games/{gameId}/lineup-players/{lineupPlayerId}/starter")
    @ResponseStatus(HttpStatus.OK)
    public void changePlayerStateToStarter(@PathVariable final Long gameId,
                                           @PathVariable final Long lineupPlayerId) {
        lineupPlayerService.changePlayerStateToStarter(gameId, lineupPlayerId);
    }

    @PatchMapping("/games/{gameId}/lineup-players/{lineupPlayerId}/candidate")
    @ResponseStatus(HttpStatus.OK)
    public void changePlayerStateToCandidate(@PathVariable final Long gameId,
                                             @PathVariable final Long lineupPlayerId) {
        lineupPlayerService.changePlayerStateToCandidate(gameId, lineupPlayerId);
    }

    @PostMapping("/leagues/{leagueId}/games")
    public ResponseEntity<Long> registerGame(@PathVariable final Long leagueId,
                                             @RequestBody final GameRequest.Register request,
                                             final Member member) {
        Long gameId = gameService.register(leagueId, request, member);
        return ResponseEntity.created(URI.create("/games/" + gameId)).body(gameId);
    }

    @PutMapping("/leagues/{leagueId}/{gameId}")
    @ResponseStatus(HttpStatus.OK)
    public void updateGame(@PathVariable final Long leagueId, @PathVariable final Long gameId,
                           @RequestBody final GameRequest.Update request, final Member member) {
        gameService.updateGame(leagueId, gameId, request, member);
    }

    @DeleteMapping("/leagues/{leagueId}/{gameId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteGame(@PathVariable final Long leagueId,
                           @PathVariable final Long gameId, final Member member) {
        gameService.deleteGame(leagueId, gameId, member);
    }

    @DeleteMapping("/game-teams/{gameTeamId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteGameTeam(@PathVariable final Long gameTeamId, final Member member) {
        gameService.deleteGameTeam(gameTeamId, member);
    }

    @PatchMapping("/games/{gameId}/lineup-players/{lineupPlayerId}/captain/register")
    @ResponseStatus(HttpStatus.OK)
    public void changePlayerToCaptain(@PathVariable final Long gameId,
                                      @PathVariable final Long lineupPlayerId) {
        lineupPlayerService.changePlayerToCaptain(gameId, lineupPlayerId);
    }

    @PatchMapping("/games/{gameId}/lineup-players/{lineupPlayerId}/captain/revoke")
    @ResponseStatus(HttpStatus.OK)
    public void revokeCaptainFromPlayer(@PathVariable final Long gameId,
                                        @PathVariable final Long lineupPlayerId) {
        lineupPlayerService.revokeCaptainFromPlayer(gameId, lineupPlayerId);
    }

    @PostMapping("/game-teams/{gameTeamId}/lineup-players")
    @ResponseStatus(HttpStatus.CREATED)
    public Long addPlayerToLineup(@PathVariable final Long gameTeamId,
                                  @RequestBody final GameRequest.LineupPlayerRequest request) {
        return lineupPlayerService.addPlayerToLineup(gameTeamId, request);
    }

    @DeleteMapping("/game-teams/{gameTeamId}/lineup-players/{lineupPlayerId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removePlayerFromLineup(@PathVariable final Long gameTeamId,
                                       @PathVariable final Long lineupPlayerId) {
        lineupPlayerService.removePlayerFromLineup(gameTeamId, lineupPlayerId);
    }
}
