package com.sports.server.command.team.application;

import com.sports.server.command.player.domain.Player;
import com.sports.server.command.team.domain.Team;
import com.sports.server.command.team.domain.TeamRepository;
import com.sports.server.command.team.dto.TeamRequest;
import com.sports.server.common.application.EntityUtils;
import com.sports.server.common.application.S3Service;
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

    private final TeamRepository teamRepository;
    private final EntityUtils entityUtils;
    private final S3Service s3Service;

    public void register(final TeamRequest.Register request) {
        String imgUrl = changeLogoImageUrlToBeSaved(request.logoImageUrl());
        s3Service.doesFileExist(imgUrl);

        Team team = request.toEntity(imgUrl);
        teamRepository.save(team);
    }

    public void update(TeamRequest.Update request, Long teamId) {
        Team team = entityUtils.getEntity(teamId, Team.class);
        team.update(request.name(), request.logoImageUrl(), originPrefix, replacePrefix, request.unit(), request.teamColor());
        s3Service.doesFileExist(team.getLogoImageUrl());
    }

    public void addPlayerToTeam(final Long teamId, final TeamRequest.PlayerIdRequest request){
        Team team = entityUtils.getEntity(teamId, Team.class);
        Player player = entityUtils.getEntity(request.playerId(), Player.class);
        team.addTeamPlayer(player);
    }

    public void deletePlayerFromTeam(final Long teamId, final TeamRequest.PlayerIdRequest request) {
        Team team = entityUtils.getEntity(teamId, Team.class);
        Player player = entityUtils.getEntity(request.playerId(), Player.class);
        team.removeTeamPlayer(player);
    }

    public void deleteLogoImage(Long teamId) {
        Team team = entityUtils.getEntity(teamId, Team.class);
        team.deleteLogoImageUrl();
    }

    private String changeLogoImageUrlToBeSaved(String logoImageUrl) {
        if (!logoImageUrl.contains(originPrefix)) {
            throw new IllegalStateException("잘못된 이미지 url 입니다.");
        }
        return logoImageUrl.replace(originPrefix, replacePrefix);
    }
}
