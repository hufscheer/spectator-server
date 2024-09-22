package com.sports.server.query.application;

import com.sports.server.auth.exception.AuthorizationErrorMessages;
import com.sports.server.command.cheertalk.domain.CheerTalk;
import com.sports.server.command.league.domain.League;
import com.sports.server.command.member.domain.Member;
import com.sports.server.common.application.EntityUtils;
import com.sports.server.common.dto.PageRequestDto;
import com.sports.server.common.exception.UnauthorizedException;
import com.sports.server.query.dto.response.CheerTalkResponse;
import com.sports.server.query.dto.response.CheerTalkResponse.ForManager;
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

    public List<CheerTalkResponse.ForSpectator> getCheerTalksByGameId(final Long gameId,
                                                                      final PageRequestDto pageRequest) {
        List<CheerTalk> cheerTalks = cheerTalkDynamicRepository.findByGameIdOrderByStartTime(
                gameId, pageRequest.cursor(), pageRequest.size()
        );

        List<CheerTalkResponse.ForSpectator> responses = cheerTalks.stream()
                .map(CheerTalkResponse.ForSpectator::new)
                .collect(Collectors.toList());

        Collections.reverse(responses);
        return responses;
    }

    public List<CheerTalkResponse.ForManager> getReportedCheerTalksByLeagueId(final Long leagueId,
                                                                              final PageRequestDto pageRequest,
                                                                              final Member manager) {
        checkPermission(leagueId, manager);

        List<CheerTalk> reportedCheerTalks = cheerTalkDynamicRepository.findReportedCheerTalksByLeagueId(
                leagueId, pageRequest.cursor(), pageRequest.size()
        );

        return reportedCheerTalks.stream()
                .map(cheerTalk -> new CheerTalkResponse.ForManager(cheerTalk,
                        gameQueryRepository.findByGameTeamIdWithLeague(cheerTalk.getGameTeamId()))).toList();
    }

    public List<CheerTalkResponse.ForManager> getAllCheerTalksByLeagueId(Long leagueId, PageRequestDto pageRequest,
                                                                         Member manager) {
        checkPermission(leagueId, manager);

        List<CheerTalk> cheerTalks = cheerTalkDynamicRepository.findCheerTalksByLeagueId(
                leagueId, pageRequest.cursor(), pageRequest.size()
        );

        return cheerTalks.stream()
                .map(cheerTalk -> new ForManager(cheerTalk,
                        gameQueryRepository.findByGameTeamIdWithLeague(cheerTalk.getGameTeamId()))).toList();
    }

    private void checkPermission(final Long leagueId, final Member manager) {
        League league = entityUtils.getEntity(leagueId, League.class);

        if (!league.isManagedBy(manager)) {
            throw new UnauthorizedException(AuthorizationErrorMessages.PERMISSION_DENIED);
        }
    }


}
