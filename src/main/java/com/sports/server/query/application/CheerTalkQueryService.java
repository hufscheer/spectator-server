package com.sports.server.query.application;

import com.sports.server.auth.exception.AuthorizationErrorMessages;
import com.sports.server.command.cheertalk.domain.CheerTalk;
import com.sports.server.command.league.domain.League;
import com.sports.server.command.member.domain.Member;
import com.sports.server.common.application.EntityUtils;
import com.sports.server.common.dto.PageRequestDto;
import com.sports.server.common.exception.UnauthorizedException;
import com.sports.server.query.dto.response.CheerTalkResponse;
import com.sports.server.query.dto.response.ReportedCheerTalkResponse;
import com.sports.server.query.repository.CheerTalkDynamicRepository;
import com.sports.server.query.repository.GameQueryRepository;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CheerTalkQueryService {

    private final CheerTalkDynamicRepository cheerTalkDynamicRepository;

    private final GameQueryRepository gameQueryRepository;

    private final EntityUtils entityUtils;

    public List<CheerTalkResponse> getCheerTalksByGameId(final Long gameId, final PageRequestDto pageRequest) {
        List<CheerTalk> cheerTalks = cheerTalkDynamicRepository.findByGameIdOrderByStartTime(
                gameId, pageRequest.cursor(), pageRequest.size()
        );

        List<CheerTalkResponse> responses = cheerTalks.stream()
                .map(CheerTalkResponse::new)
                .collect(Collectors.toList());

        Collections.reverse(responses);
        return responses;
    }

    public List<ReportedCheerTalkResponse> getReportedCheerTalksByLeagueId(final Long leagueId,
                                                                           final PageRequestDto pageRequest,
                                                                           final Member manager) {
        League league = entityUtils.getEntity(leagueId, League.class);

        if (!league.isManagedBy(manager)) {
            throw new UnauthorizedException(AuthorizationErrorMessages.PERMISSION_DENIED);
        }

        List<CheerTalk> reportedCheerTalks = cheerTalkDynamicRepository.findReportedCheerTalksByLeagueId(
                leagueId, pageRequest.cursor(), pageRequest.size()
        );

        return reportedCheerTalks.stream()
                .map(cheerTalk -> new ReportedCheerTalkResponse(cheerTalk,
                        gameQueryRepository.findByIdWithLeague(cheerTalk.getGameTeamId()))).toList();
    }

}
