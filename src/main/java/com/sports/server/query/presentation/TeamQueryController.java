package com.sports.server.query.presentation;

import com.sports.server.query.application.GameQueryService;
import com.sports.server.query.application.TeamQueryService;
import com.sports.server.query.dto.response.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/teams")
public class TeamQueryController {

    private final TeamQueryService teamQueryService;
    private final GameQueryService gameQueryService;

    @GetMapping
    public ResponseEntity<List<TeamResponse>> getAllTeams(@RequestParam(required = false) final List<String> units) {
        return ResponseEntity.ok(teamQueryService.getAllTeamsByUnits(units));
    }

    @GetMapping("/{teamId}")
    public ResponseEntity<TeamDetailResponse> getTeamDetail(@PathVariable final Long teamId) {
        return ResponseEntity.ok(teamQueryService.getTeamDetail(teamId));
    }

    @GetMapping("/{teamId}/players")
    public ResponseEntity<List<PlayerResponse>> getTeamPlayers(@PathVariable final Long teamId) {
        return ResponseEntity.ok(teamQueryService.getAllTeamPlayers(teamId));
    }

    @GetMapping("/{teamId}/games")
    public ResponseEntity<List<GameDetailResponse>> getAllGamesByTeam(@PathVariable final Long teamId) {
        return ResponseEntity.ok(gameQueryService.getAllGamesDetailByTeam(teamId));
    }

    @GetMapping("/summary")
    public ResponseEntity<List<TeamSummaryResponse>> getAllTeamsSummary(@RequestParam(required = false) final List<String> units){
        return ResponseEntity.ok(teamQueryService.getAllTeamsSummary(units));
    }
}
