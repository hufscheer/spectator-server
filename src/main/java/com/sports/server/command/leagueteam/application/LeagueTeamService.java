package com.sports.server.command.leagueteam.application;

import com.sports.server.auth.exception.AuthorizationErrorMessages;
import com.sports.server.command.league.domain.League;
import com.sports.server.command.leagueteam.domain.LeagueTeam;
import com.sports.server.command.leagueteam.domain.LeagueTeamRepository;
import com.sports.server.command.leagueteam.dto.LeagueTeamRegisterRequest;
import com.sports.server.command.member.domain.Member;
import com.sports.server.common.application.EntityUtils;
import com.sports.server.common.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LeagueTeamService {

    private final LeagueTeamRepository leagueTeamRepository;
    private final EntityUtils entityUtils;

    @Transactional
    public void register(final Long leagueId, final Member manager, final LeagueTeamRegisterRequest request) {
        League league = entityUtils.getEntity(leagueId, League.class);

        if (!league.isManagedBy(manager)) {
            throw new UnauthorizedException(AuthorizationErrorMessages.PERMISSION_DENIED);
        }

        LeagueTeam leagueTeam = request.toEntity(manager, league);
        request.players().stream()
                .map(lgp -> lgp.toEntity(leagueTeam))
                .forEach(leagueTeam::addPlayer);
        leagueTeamRepository.save(leagueTeam);
    }
}
