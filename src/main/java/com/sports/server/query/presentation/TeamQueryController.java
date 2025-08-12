package com.sports.server.query.presentation;

import com.sports.server.query.application.GameQueryService;
import com.sports.server.query.application.TeamQueryService;
import com.sports.server.query.dto.response.GameDetailResponse;
import com.sports.server.query.dto.response.TeamDetailResponse;
import com.sports.server.query.dto.response.TeamResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/teams")
public class TeamQueryController {

    private final TeamQueryService teamQueryService;
    private final GameQueryService gameQueryService;

    @GetMapping
    public ResponseEntity<List<TeamResponse>> getAllTeams() {
        return ResponseEntity.ok(teamQueryService.getAllTeams());
    }

    @GetMapping("/{teamId}")
    public ResponseEntity<TeamDetailResponse> getTeamDetail(@PathVariable final Long teamId) {
        return ResponseEntity.ok(teamQueryService.getTeamDetail(teamId));
    }

    @GetMapping("/{teamId}/games")
    public ResponseEntity<List<GameDetailResponse>> getAllGamesByTeam(@PathVariable final Long teamId) {
        return ResponseEntity.ok(gameQueryService.getAllGamesDetailByTeam(teamId));
    }

}
