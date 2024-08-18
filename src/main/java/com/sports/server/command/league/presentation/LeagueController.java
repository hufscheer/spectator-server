package com.sports.server.command.league.presentation;

import com.sports.server.command.league.application.LeagueService;
import com.sports.server.command.league.dto.LeagueRequestDto;
import com.sports.server.command.member.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/leagues")
public class LeagueController {
    private final LeagueService leagueService;

    @PostMapping
    public ResponseEntity<Void> register(@RequestBody final LeagueRequestDto.Register register, Member member) {
        leagueService.register(member, register);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{leagueId}")
    public ResponseEntity<Void> delete(final Member member, @PathVariable final Long leagueId) {
        leagueService.delete(member, leagueId);
        return ResponseEntity.ok().build();
    }
}
