package com.sports.server.query.presentation;

import com.sports.server.query.application.PlayerQueryService;
import com.sports.server.query.dto.response.PlayerResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/players")
public class PlayerQueryController {

    private final PlayerQueryService playerQueryService;

    @GetMapping
    public ResponseEntity<List<PlayerResponse>> getAllPlayers(){
        return ResponseEntity.ok(playerQueryService.getAllPlayers());
    }

    @GetMapping("/{playerId}")
    public ResponseEntity<PlayerResponse> getPlayerById(@PathVariable Long playerId){
        return ResponseEntity.ok(playerQueryService.findPlayer(playerId));
    }
}
