package com.sports.server.query.presentation;

import com.sports.server.command.league.domain.SportType;
import com.sports.server.command.member.domain.Member;
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

    @GetMapping("/units")
    public ResponseEntity<List<UnitResponse>> getUnitsWithTeams(
            @RequestParam(required = false) final SportType sportType,
            final Member member) {
        Long organizationId = member.getOrganization().getId();
        return ResponseEntity.ok(teamQueryService.getUnitsWithTeams(sportType, organizationId));
    }

    @GetMapping
    public ResponseEntity<List<TeamResponse>> getAllTeams(
            @RequestParam(required = false) final List<String> units,
            @RequestParam(required = false) final SportType sportType,
            final Member member) {
        return ResponseEntity.ok(teamQueryService.getAllTeamsByUnits(units, sportType, member));
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
    public ResponseEntity<List<TeamSummaryResponse>> getAllTeamsSummary(
            @RequestParam(required = false) final List<String> units,
            @RequestParam(required = false) final SportType sportType) {
        return ResponseEntity.ok(teamQueryService.getAllTeamsSummary(units, sportType));
    }
}
