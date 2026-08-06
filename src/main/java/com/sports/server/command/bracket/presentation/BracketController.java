package com.sports.server.command.bracket.presentation;

import com.sports.server.command.bracket.application.BracketService;
import com.sports.server.command.bracket.dto.BracketRequest;
import com.sports.server.command.member.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/leagues")
public class BracketController {

    private final BracketService bracketService;

    @PutMapping("/{leagueId}/bracket")
    @ResponseStatus(HttpStatus.OK)
    public void save(@PathVariable("leagueId") final Long leagueId,
                     @RequestBody final BracketRequest.Save request, final Member member) {
        bracketService.replace(member, leagueId, request);
    }
}
