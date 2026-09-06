package com.sports.server.query.application;

import com.sports.server.command.bracket.domain.Bracket;
import com.sports.server.command.bracket.domain.BracketMatch;
import com.sports.server.command.bracket.exception.BracketErrorMessages;
import com.sports.server.command.league.domain.League;
import com.sports.server.common.application.EntityUtils;
import com.sports.server.common.exception.NotFoundException;
import com.sports.server.query.dto.response.BracketResponse;
import com.sports.server.query.repository.BracketMatchQueryRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BracketQueryService {

    private final EntityUtils entityUtils;
    private final BracketMatchQueryRepository bracketMatchQueryRepository;

    public BracketResponse findBracketByLeagueId(final Long leagueId) {
        entityUtils.getEntity(leagueId, League.class);
        List<BracketMatch> matches = bracketMatchQueryRepository.findAllByLeagueIdWithTeamsAndGames(leagueId);
        if (matches.isEmpty()) {
            throw new NotFoundException(BracketErrorMessages.BRACKET_NOT_FOUND);
        }
        return BracketResponse.of(Bracket.from(matches));
    }
}
