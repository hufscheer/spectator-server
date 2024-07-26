package com.sports.server.command.leagueteam.application;

import com.sports.server.auth.exception.AuthorizationErrorMessages;
import com.sports.server.command.league.domain.League;
import com.sports.server.command.leagueteam.domain.LeagueTeam;
import com.sports.server.command.leagueteam.domain.LeagueTeamPlayer;
import com.sports.server.command.leagueteam.domain.LeagueTeamPlayerRepository;
import com.sports.server.command.leagueteam.domain.LeagueTeamRepository;
import com.sports.server.command.leagueteam.domain.LogoImageDeletedEvent;
import com.sports.server.command.leagueteam.dto.LeagueTeamPlayerRequest;
import com.sports.server.command.leagueteam.dto.LeagueTeamRequest;
import com.sports.server.command.member.domain.Member;
import com.sports.server.common.application.EntityUtils;
import com.sports.server.common.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
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
    private final ApplicationEventPublisher eventPublisher;

    public void register(final Long leagueId, final Member manager, final LeagueTeamRequest.Register request) {
        League league = getLeagueAndCheckPermission(leagueId, manager);

        String imgUrl = changeLogoImageUrlToBeSaved(request.logoImageUrl());
        LeagueTeam leagueTeam = request.toEntity(manager, league, imgUrl);

        for (LeagueTeamPlayerRequest.Register player : request.players()) {
            leagueTeam.addPlayer(player.toEntity(leagueTeam));
        }

        leagueTeamRepository.save(leagueTeam);
    }

    public void update(Long leagueId, LeagueTeamRequest.Update request, Member manager, Long teamId) {
        getLeagueAndCheckPermission(leagueId, manager);
        LeagueTeam leagueTeam = leagueTeamRepository.findById(teamId);

        leagueTeam.updateInfo(request.name(), changeLogoImageUrlToBeSaved(request.logoImageUrl()));

        addPlayers(request, leagueTeam);
        updatePlayers(request, leagueTeam);
        deletePlayers(request, leagueTeam);
    }

    private void addPlayers(LeagueTeamRequest.Update request, LeagueTeam leagueTeam) {
        if (request.newPlayers() != null) {
            request.newPlayers().stream()
                    .map(lgp -> lgp.toEntity(leagueTeam))
                    .forEach(leagueTeam::addPlayer);
        }
    }

    private void updatePlayers(LeagueTeamRequest.Update request, LeagueTeam leagueTeam) {
        if (request.updatedPlayers() != null) {
            request.updatedPlayers().forEach(updateRequest -> {
                LeagueTeamPlayer player = entityUtils.getEntity(updateRequest.id(), LeagueTeamPlayer.class);
                leagueTeam.validateLeagueTeamPlayer(player);
                player.update(updateRequest.name(), updateRequest.number());
            });
        }
    }

    private void deletePlayers(LeagueTeamRequest.Update request, LeagueTeam leagueTeam) {
        if (request.deletedPlayerIds() != null) {
            request.deletedPlayerIds().stream()
                    .map(lgpId -> {
                        LeagueTeamPlayer lgp = entityUtils.getEntity(lgpId, LeagueTeamPlayer.class);
                        leagueTeam.validateLeagueTeamPlayer(lgp);
                        return lgp;
                    })
                    .forEach(lgp -> leagueTeamPlayerRepository.delete(lgp));
        }
    }

    public void deleteLogoImage(Long leagueId, Member manager, Long teamId) {
        getLeagueAndCheckPermission(leagueId, manager);

        LeagueTeam leagueTeam = entityUtils.getEntity(teamId, LeagueTeam.class);
        String logoImageUrl = leagueTeam.getLogoImageUrl();
        leagueTeam.deleteLogoImageUrl();

        eventPublisher.publishEvent(new LogoImageDeletedEvent(getKeyOfImageUrl(logoImageUrl)));
    }

    private String changeLogoImageUrlToBeSaved(String logoImageUrl) {
        if (!logoImageUrl.contains(originPrefix)) {
            throw new IllegalStateException("잘못된 이미지 url 입니다.");
        }
        return logoImageUrl.replace(originPrefix, replacePrefix);
    }

    private String getKeyOfImageUrl(String logoImageUrl) {
        return logoImageUrl.split(replacePrefix)[1];
    }

    private League getLeagueAndCheckPermission(final Long leagueId, final Member manager) {
        League league = entityUtils.getEntity(leagueId, League.class);

        if (!league.isManagedBy(manager)) {
            throw new UnauthorizedException(AuthorizationErrorMessages.PERMISSION_DENIED);
        }

        return league;
    }
}
