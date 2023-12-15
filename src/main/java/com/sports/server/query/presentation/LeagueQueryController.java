package com.sports.server.query.presentation;

import com.sports.server.query.application.LeagueQueryService;
import com.sports.server.query.dto.response.LeagueResponse;
import com.sports.server.query.dto.response.LeagueSportResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/leagues")
@RequiredArgsConstructor
public class LeagueQueryController {

    private final LeagueQueryService leagueQueryService;

    @GetMapping
    public ResponseEntity<List<LeagueResponse>> findAll() {
        return ResponseEntity.ok(leagueQueryService.findAll());
    }

    @GetMapping("/{leagueId}/sports")
    public ResponseEntity<List<LeagueSportResponse>> findSportsByLeague(@PathVariable Long leagueId) {
        return ResponseEntity.ok(leagueQueryService.findSportsByLeague(leagueId));
    }
}
