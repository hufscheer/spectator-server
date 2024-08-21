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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

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
}
