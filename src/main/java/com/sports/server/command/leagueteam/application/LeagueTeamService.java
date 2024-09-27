package com.sports.server.command.leagueteam.application;

import com.sports.server.command.league.domain.League;
import com.sports.server.command.leagueteam.domain.LeagueTeam;
import com.sports.server.command.leagueteam.domain.LeagueTeamPlayer;
import com.sports.server.command.leagueteam.domain.LeagueTeamRepository;
import com.sports.server.command.leagueteam.dto.LeagueTeamPlayerRequest;
import com.sports.server.command.leagueteam.dto.LeagueTeamRequest;
import com.sports.server.command.member.domain.Member;
import com.sports.server.common.application.EntityUtils;
import com.sports.server.common.application.PermissionValidator;
import com.sports.server.common.application.S3Service;
import com.sports.server.common.exception.NotFoundException;
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
    private final EntityUtils entityUtils;
    private final S3Service s3Service;

    public void register(final Long leagueId, final Member manager, final LeagueTeamRequest.Register request) {
        League league = entityUtils.getEntity(leagueId, League.class);
        PermissionValidator.checkPermission(league, manager);

        String imgUrl = changeLogoImageUrlToBeSaved(request.logoImageUrl());
        s3Service.doesFileExist(imgUrl);
        LeagueTeam leagueTeam = request.toEntity(manager, league, imgUrl);

        for (LeagueTeamPlayerRequest.Register player : request.players()) {
            leagueTeam.addPlayer(player.toEntity(leagueTeam));
        }

        leagueTeamRepository.save(leagueTeam);
    }

    public void update(Long leagueId, LeagueTeamRequest.Update request, Member manager, Long teamId) {
        League league = entityUtils.getEntity(leagueId, League.class);
        PermissionValidator.checkPermission(league, manager);

        LeagueTeam leagueTeam = getLeagueTeam(teamId);

        leagueTeam.updateInfo(request.name(), request.logoImageUrl(), originPrefix, replacePrefix);
        s3Service.doesFileExist(leagueTeam.getLogoImageUrl());

        addPlayers(request, leagueTeam);
        updatePlayers(request, leagueTeam);
        deletePlayers(request, leagueTeam);
    }

    public void delete(Long leagueId, Member manager, Long teamId) {
        League league = entityUtils.getEntity(leagueId, League.class);
        PermissionValidator.checkPermission(league, manager);

        LeagueTeam leagueTeam = entityUtils.getEntity(teamId, LeagueTeam.class);
        leagueTeam.isParticipate(league);

        leagueTeam.deleteLogoImageUrl();
        leagueTeamRepository.delete(leagueTeam);
    }

    private LeagueTeam getLeagueTeam(final Long leagueTeamId) {
        return leagueTeamRepository.findById(leagueTeamId)
                .orElseThrow(() -> new NotFoundException("해당 리그팀이 존재하지 않습니다."));
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
                    .forEach(leagueTeam::deletePlayer);
        }
    }

    public void deleteLogoImage(Long leagueId, Member manager, Long teamId) {
        League league = entityUtils.getEntity(leagueId, League.class);
        PermissionValidator.checkPermission(league, manager);

        LeagueTeam leagueTeam = entityUtils.getEntity(teamId, LeagueTeam.class);
        leagueTeam.deleteLogoImageUrl();
    }

    private String changeLogoImageUrlToBeSaved(String logoImageUrl) {
        if (!logoImageUrl.contains(originPrefix)) {
            throw new IllegalStateException("잘못된 이미지 url 입니다.");
        }
        return logoImageUrl.replace(originPrefix, replacePrefix);
    }
}
