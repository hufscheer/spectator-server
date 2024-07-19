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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class LeagueTeamService {

    @Value("${image.origin-prefix}")
    private String originPrefix;

    @Value("${image.replaced-prefix}")
    private String replacePrefix;

    private final LeagueTeamRepository leagueTeamRepository;
    private final LeagueTeamPlayerRepository leagueTeamPlayerRepository;
    private final EntityUtils entityUtils;

    public void register(final Long leagueId, final Member manager, final LeagueTeamRegisterRequest request) {
        League league = getLeagueAndCheckPermission(leagueId, manager);

        LeagueTeam leagueTeam = request.toEntity(manager, league, changeLogoImageUrl(request.logoImageUrl()));
        request.players().stream()
                .map(lgp -> lgp.toEntity(leagueTeam))
                .forEach(leagueTeam::addPlayer);
        leagueTeamRepository.save(leagueTeam);
    }

    public void update(Long leagueId, LeagueTeamUpdateRequest request, Member manager, Long teamId) {
        getLeagueAndCheckPermission(leagueId, manager);
        LeagueTeam leagueTeam = leagueTeamRepository.findById(teamId);

        leagueTeam.update(request.name(), changeLogoImageUrl(request.logoImageUrl()));
        request.addPlayers().stream()
                .map(lgp -> lgp.toEntity(leagueTeam))
                .forEach(leagueTeam::addPlayer);

        request.deletedPlayerIds().stream()
                .map(lgpId -> {
                    LeagueTeamPlayer lgp = entityUtils.getEntity(lgpId, LeagueTeamPlayer.class);
                    leagueTeam.validateLeagueTeamPlayer(lgp);
                    return lgp;
                })
                .forEach(lgp -> leagueTeamPlayerRepository.delete(lgp));
    }

    private String changeLogoImageUrl(String logoImageUrl) {
        if (!logoImageUrl.contains(originPrefix)) {
            throw new IllegalStateException("잘못된 이미지 url 입니다.");
        }
        return logoImageUrl.replace(originPrefix, replacePrefix);
    }

    private League getLeagueAndCheckPermission(final Long leagueId, final Member manager) {
        League league = entityUtils.getEntity(leagueId, League.class);

        if (!league.isManagedBy(manager)) {
            throw new UnauthorizedException(AuthorizationErrorMessages.PERMISSION_DENIED);
        }

        return league;
    }
}
