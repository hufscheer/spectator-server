package com.sports.server.query.presentation;

import com.sports.server.command.league.domain.SportType;
import com.sports.server.command.member.domain.Member;
import com.sports.server.query.application.TeamQueryService;
import com.sports.server.query.dto.response.TeamResponse;
import com.sports.server.query.dto.response.UnitResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/manager/teams")
public class TeamManagerQueryController {

    private final TeamQueryService teamQueryService;

    @GetMapping("/units")
    public ResponseEntity<List<UnitResponse>> getUnitsWithTeams(
            @RequestParam(required = false) final SportType sportType,
            final Member member) {
        return ResponseEntity.ok(teamQueryService.getUnitsWithTeams(sportType, member));
    }

    @GetMapping
    public ResponseEntity<List<TeamResponse>> getAllTeams(
            @RequestParam(required = false) final List<String> units,
            @RequestParam(required = false) final SportType sportType,
            final Member member) {
        return ResponseEntity.ok(teamQueryService.getAllTeamsByUnits(units, sportType, member));
    }
}
