package com.sports.server.command.game.presentation;

import com.sports.server.command.game.application.GameService;
import com.sports.server.command.game.application.GameTeamService;
import com.sports.server.command.game.application.LineupPlayerService;
import com.sports.server.command.game.dto.CheerCountUpdateRequest;
import com.sports.server.command.game.dto.GameRequest;
import com.sports.server.command.league.dto.LeagueTeamRequest;
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

    // 선수 등번호 변경 - 등번호 변경 위치 아직 매니저 서버 ui 에서 안 나옴, 임시 추가
    @PatchMapping("/lineup-players/{lineupPlayerId}/jersey-number")
    @ResponseStatus(HttpStatus.OK)
    public void changePlayerJerseyNumber(@PathVariable final Long lineupPlayerId,
                                         @RequestBody final LeagueTeamRequest.UpdateJerseyNumber request) {
        lineupPlayerService.changePlayerJerseyNumber(lineupPlayerId, request);
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

    // restdocs 에서 API 에러(테스트 코드에 오류있는 듯)
    @DeleteMapping("/leagues/{leagueId}/{gameId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteGame(@PathVariable final Long leagueId,
                           @PathVariable final Long gameId, final Member manager) {
        gameService.deleteGame(leagueId, gameId, manager);
    }

    // 게임팀 삭제 로직 - 매니저 서버 ui 확인 필요
    @DeleteMapping("/game-teams/{gameTeamId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteGameTeam(@PathVariable final Long gameTeamId, final Member manager) {
        gameService.deleteGameTeam(gameTeamId, manager);
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
}
