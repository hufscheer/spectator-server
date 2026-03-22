package com.sports.server.command.team.application;

import com.sports.server.command.player.domain.Player;
import com.sports.server.command.player.domain.PlayerRepository;
import com.sports.server.command.player.exception.PlayerErrorMessages;
import com.sports.server.command.team.domain.*;
import com.sports.server.command.team.dto.TeamRequest;
import com.sports.server.common.application.EntityUtils;
import com.sports.server.common.application.S3Service;
import com.sports.server.common.exception.CustomException;
import com.sports.server.common.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
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
    private final TeamPlayerRepository teamPlayerRepository;
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

    public Long registerAndReturnId(final TeamRequest.Register request) {
        String imgUrl = changeLogoImageUrlToBeSaved(request.logoImageUrl());
        s3Service.doesFileExist(imgUrl);

        Team team = request.toEntity(imgUrl);
        teamRepository.save(team);
        return team.getId();
    }

    public void update(final TeamRequest.Update request, final Long teamId) {
        Team team = entityUtils.getEntity(teamId, Team.class);
        s3Service.doesFileExist(team.getLogoImageUrl());
        Unit unit = Optional.ofNullable(request.unit()).map(Unit::from).orElse(null);
        team.update(request.name(), request.logoImageUrl(), originPrefix, replacePrefix, unit, request.teamColor());

        if (request.teamPlayers() != null) {
            upsertPlayersToTeam(team, request.teamPlayers());
        }
    }

    public void delete(final Long teamId) {
        Team team = entityUtils.getEntity(teamId, Team.class);
        teamRepository.delete(team);
    }

    public void addPlayersToTeam(final Long teamId, final List<TeamRequest.TeamPlayerRegister> request) {
        Team team = entityUtils.getEntity(teamId, Team.class);

        List<Long> playerIds = request.stream().map(TeamRequest.TeamPlayerRegister::playerId).toList();
        List<Player> players = playerRepository.findAllById(playerIds);

        validateExistence(players, playerIds);

        Map<Long, Integer> jerseyNumbers = buildJerseyNumberMap(request);
        List<TeamPlayer> newTeamPlayers = players.stream()
                .map(player -> team.addPlayer(player, jerseyNumbers.get(player.getId())))
                .toList();

        teamPlayerRepository.saveAll(newTeamPlayers);
    }

    public void deleteTeamPlayer(final Long teamPlayerId) {
        TeamPlayer teamPlayer = entityUtils.getEntity(teamPlayerId, TeamPlayer.class);
        Team team = teamPlayer.getTeam();
        Player player = teamPlayer.getPlayer();
        team.removeTeamPlayer(player);
    }

    public void deleteLogoImage(Long teamId) {
        Team team = entityUtils.getEntity(teamId, Team.class);
        team.deleteLogoImageUrl();
    }

    private void upsertPlayersToTeam(Team team, List<TeamRequest.TeamPlayerRegister> request) {
        List<Long> playerIds = request.stream().map(TeamRequest.TeamPlayerRegister::playerId).toList();
        List<Player> players = playerRepository.findAllById(playerIds);

        validateExistence(players, playerIds);

        Map<Long, Integer> jerseyNumbers = buildJerseyNumberMap(request);
        List<TeamPlayer> existingTeamPlayers = teamPlayerRepository.findTeamPlayersWithPlayerByTeamId(team.getId());

        List<TeamPlayer> newTeamPlayers = new ArrayList<>();
        players.forEach(player -> {
            TeamPlayer teamPlayer = upsertPlayer(existingTeamPlayers, team, player, jerseyNumbers.get(player.getId()));
            if (teamPlayer.getId() == null) {
                newTeamPlayers.add(teamPlayer);
            }
        });

        if (!newTeamPlayers.isEmpty()) {
            teamPlayerRepository.saveAll(newTeamPlayers);
        }
    }

    private TeamPlayer upsertPlayer(List<TeamPlayer> existingTeamPlayers, Team team, Player player, Integer jerseyNumber) {
        return existingTeamPlayers.stream()
                .filter(tp -> tp.getPlayer().getId().equals(player.getId()))
                .findFirst()
                .map(tp -> {
                    tp.updateJerseyNumber(jerseyNumber);
                    return tp;
                })
                .orElseGet(() -> TeamPlayer.of(team, player, jerseyNumber));
    }

    private static void validateExistence(List<Player> players, List<Long> playerIds) {
        if (players.size() != new HashSet<>(playerIds).size()) {
            throw new NotFoundException(PlayerErrorMessages.PLAYER_NOT_EXIST_EXCEPTION);
        }
    }

    private static Map<Long, Integer> buildJerseyNumberMap(List<TeamRequest.TeamPlayerRegister> request) {
        return request.stream()
                .collect(Collectors.toMap(
                        TeamRequest.TeamPlayerRegister::playerId,
                        TeamRequest.TeamPlayerRegister::jerseyNumber
                ));
    }

    private String changeLogoImageUrlToBeSaved(String logoImageUrl) {
        if (!logoImageUrl.contains(originPrefix)) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "잘못된 이미지 url 입니다.");
        }
        return logoImageUrl.replace(originPrefix, replacePrefix);
    }
}
