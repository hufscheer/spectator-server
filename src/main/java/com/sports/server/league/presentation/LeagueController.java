package com.sports.server.league.presentation;

import com.sports.server.league.application.LeagueService;
import com.sports.server.league.dto.response.LeagueResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/leagues")
@RequiredArgsConstructor
public class LeagueController {

    private final LeagueService leagueService;

    @GetMapping
    public ResponseEntity<List<LeagueResponse>> findAll() {
        return ResponseEntity.ok(leagueService.findAll());
    }
}
