package com.sports.server.command.team.application;

import com.sports.server.command.player.domain.Player;
import com.sports.server.command.player.domain.PlayerRepository;
import com.sports.server.command.team.domain.Team;
import com.sports.server.command.team.domain.TeamRepository;
import com.sports.server.command.team.dto.TeamRequest;
import com.sports.server.common.application.EntityUtils;
import com.sports.server.common.application.S3Service;
import com.sports.server.common.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class TeamService {

    @Value("${image.origin-prefix}")
    private String originPrefix;

    @Value("${image.replaced-prefix}")
    private String replacePrefix;

    private final TeamRepository teamRepository;
    private final PlayerRepository playerRepository;
    private final EntityUtils entityUtils;
    private final S3Service s3Service;

    public void register(final TeamRequest.Register request) {
        String imgUrl = changeLogoImageUrlToBeSaved(request.logoImageUrl());
        s3Service.doesFileExist(imgUrl);

        Team team = request.toEntity(imgUrl);
        teamRepository.save(team);

        if (request.teamPlayers() != null && !request.teamPlayers().isEmpty()) {
            addPlayersToTeam(team.getId(), request.teamPlayers());
        }
    }

    public void update(final TeamRequest.Update request, final Long teamId) {
        Team team = entityUtils.getEntity(teamId, Team.class);

        s3Service.doesFileExist(team.getLogoImageUrl());
        team.update(request.name(), request.logoImageUrl(), originPrefix, replacePrefix, request.unit(), request.teamColor());

        if (request.teamPlayers() != null) {
            addPlayersToTeam(teamId, request.teamPlayers());
        }
    }

    public void addPlayersToTeam(final Long teamId, final List<TeamRequest.TeamPlayerRegister> request){
        Team team = entityUtils.getEntity(teamId, Team.class);

        List<Long> playerIds = request.stream()
                .map(TeamRequest.TeamPlayerRegister::playerId)
                .toList();
        List<Player> players = playerRepository.findAllById(playerIds);

        Map<Long, Integer> playerJerseyNumbers = request.stream()
                .collect(Collectors.toMap(TeamRequest.TeamPlayerRegister::playerId, TeamRequest.TeamPlayerRegister::jerseyNumber));

        players.forEach(player -> {
            Integer jerseyNumber = playerJerseyNumbers.get(player.getId());
            team.addPlayer(player, jerseyNumber);
        });
    }

    public void deletePlayerFromTeam(final Long teamId, final Long playerId) {
        Team team = entityUtils.getEntity(teamId, Team.class);
        Player player = entityUtils.getEntity(playerId, Player.class);
        team.removeTeamPlayer(player);
    }

    public void deleteLogoImage(Long teamId) {
        Team team = entityUtils.getEntity(teamId, Team.class);
        team.deleteLogoImageUrl();
    }

    private String changeLogoImageUrlToBeSaved(String logoImageUrl) {
        if (!logoImageUrl.contains(originPrefix)) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "잘못된 이미지 url 입니다.");
        }
        return logoImageUrl.replace(originPrefix, replacePrefix);
    }
}
