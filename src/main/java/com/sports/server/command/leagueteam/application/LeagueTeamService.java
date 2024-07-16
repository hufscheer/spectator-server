package com.sports.server.command.leagueteam.application;

import com.sports.server.auth.exception.AuthorizationErrorMessages;
import com.sports.server.command.league.domain.League;
import com.sports.server.command.leagueteam.domain.LeagueTeam;
import com.sports.server.command.leagueteam.domain.LeagueTeamPlayer;
import com.sports.server.command.leagueteam.domain.LeagueTeamPlayerRepository;
import com.sports.server.command.leagueteam.domain.LeagueTeamRepository;
import com.sports.server.command.leagueteam.dto.LeagueTeamRegisterRequest;
import com.sports.server.command.leagueteam.dto.LeagueTeamUpdateRequest;
import com.sports.server.command.member.domain.Member;
import com.sports.server.common.application.EntityUtils;
import com.sports.server.common.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class LeagueTeamService {

    private final LeagueTeamRepository leagueTeamRepository;
    private final LeagueTeamPlayerRepository leagueTeamPlayerRepository;
    private final EntityUtils entityUtils;

    public void register(final Long leagueId, final Member manager, final LeagueTeamRegisterRequest request) {
        League league = getLeagueAndCheckPermission(leagueId, manager);

        LeagueTeam leagueTeam = request.toEntity(manager, league);
        request.players().stream()
                .map(lgp -> lgp.toEntity(leagueTeam))
                .forEach(leagueTeam::addPlayer);
        leagueTeamRepository.save(leagueTeam);
    }

    public void update(Long leagueId, LeagueTeamUpdateRequest request, Member manager, Long teamId) {
        getLeagueAndCheckPermission(leagueId, manager);
        LeagueTeam leagueTeam = entityUtils.getEntity(teamId, LeagueTeam.class);

        leagueTeam.update(request.name(), request.logoImageUrl());
        request.addPlayers().stream()
                .map(lgp -> lgp.toEntity(leagueTeam))
                .forEach(leagueTeam::addPlayer);

        // TODO: 해당 리그팀에 속한 플레이어가 아니라면 예외 던지기
        request.deletedPlayerIds().stream()
                .map(lgpId -> entityUtils.getEntity(lgpId, LeagueTeamPlayer.class))
                .forEach(lgp -> {
                    leagueTeamPlayerRepository.delete(lgp);
                });
    }

    private League getLeagueAndCheckPermission(final Long leagueId, final Member manager) {
        League league = entityUtils.getEntity(leagueId, League.class);

        if (!league.isManagedBy(manager)) {
            throw new UnauthorizedException(AuthorizationErrorMessages.PERMISSION_DENIED);
        }

        return league;
    }
}
