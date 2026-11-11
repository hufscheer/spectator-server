package com.sports.server.query.application;

import com.sports.server.command.bracket.domain.Bracket;
import com.sports.server.command.bracket.domain.BracketMatch;
import com.sports.server.command.bracket.exception.BracketErrorMessages;
import com.sports.server.command.league.domain.League;
import com.sports.server.common.application.EntityUtils;
import com.sports.server.common.exception.NotFoundException;
import com.sports.server.query.dto.response.BracketResponse;
import com.sports.server.query.repository.BracketMatchQueryRepository;
import com.sports.server.query.repository.TeamQueryRepository;
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
    private final TeamQueryRepository teamQueryRepository;

    public BracketResponse findBracketByLeagueId(final Long leagueId) {
        entityUtils.getEntity(leagueId, League.class);
        // 지워진 팀도 영속성 컨텍스트에 먼저 올려, 아래 조회의 팀 참조가 이 인스턴스로 풀리게 한다
        teamQueryRepository.findAllInBracketIncludingDeleted(leagueId);
        List<BracketMatch> matches = bracketMatchQueryRepository.findAllByLeagueIdWithTeamsAndGames(leagueId);
        if (matches.isEmpty()) {
            throw new NotFoundException(BracketErrorMessages.BRACKET_NOT_FOUND);
        }
        return BracketResponse.of(Bracket.from(matches));
    }
}
