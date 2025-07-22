package com.sports.server.command.team.application;

import com.sports.server.command.league.domain.League;
import com.sports.server.command.leagueteam.domain.LeagueTeam;
import com.sports.server.command.leagueteam.domain.LeagueTeamPlayer;
import com.sports.server.command.team.domain.LeagueTeamRepository;
import com.sports.server.command.team.domain.Team;
import com.sports.server.command.team.dto.LeagueTeamPlayerRequest;
import com.sports.server.command.team.dto.TeamRequest;
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
public class TeamService {

    @Value("${image.origin-prefix}")
    private String originPrefix;

    @Value("${image.replaced-prefix}")
    private String replacePrefix;

    private final LeagueTeamRepository leagueTeamRepository;
    private final EntityUtils entityUtils;
    private final S3Service s3Service;

    public void register(final Long leagueId, final Member manager, final TeamRequest.Register request) {
        League league = entityUtils.getEntity(leagueId, League.class);
        PermissionValidator.checkPermission(league, manager);

        String imgUrl = changeLogoImageUrlToBeSaved(request.logoImageUrl());
        s3Service.doesFileExist(imgUrl);

        Team team = request.toEntity(name, manager, league, imgUrl);

        for (LeagueTeamPlayerRequest.Register player : request.players()) {
            team.addPlayer(player.toEntity(team));
        }

        leagueTeamRepository.save(team);
    }

    public void update(Long leagueId, TeamRequest.Update request, Member manager, Long teamId) {
        League league = entityUtils.getEntity(leagueId, League.class);
        PermissionValidator.checkPermission(league, manager);

        Team team = getTeam(teamId);

        team.updateInfo(request.name(), request.logoImageUrl(), originPrefix, replacePrefix);
        s3Service.doesFileExist(team.getLogoImageUrl());

        addPlayers(request, team);
        updatePlayers(request, team);
        deletePlayers(request, team);
    }

    public void delete(Long leagueId, Member manager, Long teamId) {
        League league = entityUtils.getEntity(leagueId, League.class);
        PermissionValidator.checkPermission(league, manager);

        Team leagueTeam = entityUtils.getEntity(teamId, Team.class);
        leagueTeam.isParticipate(league);

        leagueTeam.deleteLogoImageUrl();
        leagueTeamRepository.delete(team);
    }

    private Team getLeagueTeam(final Long leagueTeamId) {
        return leagueTeamRepository.findById(leagueTeamId)
                .orElseThrow(() -> new NotFoundException("해당 리그팀이 존재하지 않습니다."));
    }


    private void addPlayers(TeamRequest.Update request, Team team) {
        if (request.newPlayers() != null) {
            request.newPlayers().stream()
                    .map(lgp -> lgp.toEntity(team))
                    .forEach(team::addPlayer);
        }
    }

    private void updatePlayers(TeamRequest.Update request, Team team) {
        if (request.updatedPlayers() != null) {
            request.updatedPlayers().forEach(updateRequest -> {
                LeagueTeamPlayer player = entityUtils.getEntity(updateRequest.id(), LeagueTeamPlayer.class);
                team.validateTeamPlayer(player);
                player.update(updateRequest.name(), updateRequest.number(), updateRequest.studentNumber());
            });
        }
    }

    private void deletePlayers(TeamRequest.Update request, Team team) {
        if (request.deletedPlayerIds() != null) {
            request.deletedPlayerIds().stream()
                    .map(lgpId -> {
                        LeagueTeamPlayer lgp = entityUtils.getEntity(lgpId, LeagueTeamPlayer.class);
                        team.validateLeagueTeamPlayer(lgp);
                        return lgp;
                    })
                    .forEach(team::deletePlayer);
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
