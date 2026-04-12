package com.sports.server.command.team.application;

import com.sports.server.command.member.domain.Member;
import com.sports.server.command.player.domain.Player;
import com.sports.server.command.player.domain.PlayerRepository;
import com.sports.server.command.player.exception.PlayerErrorMessages;
import com.sports.server.command.team.domain.*;
import com.sports.server.command.team.dto.TeamRequest;
import com.sports.server.common.application.EntityUtils;
import com.sports.server.common.application.PermissionValidator;
import com.sports.server.common.application.S3Service;
import com.sports.server.common.exception.CustomException;
import com.sports.server.common.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
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

    public void register(final Member member, final TeamRequest.Register request) {
        String imgUrl = changeLogoImageUrlToBeSaved(request.logoImageUrl());
        s3Service.doesFileExist(imgUrl);

        Team team = request.toEntity(imgUrl);
        team.setOrganization(member.getOrganization());
        teamRepository.save(team);

        if (request.teamPlayers() != null && !request.teamPlayers().isEmpty()) {
            addPlayersToTeam(member, team.getId(), request.teamPlayers());
        }
    }

    public Long registerAndReturnId(final Member member, final TeamRequest.Register request) {
        String imgUrl = changeLogoImageUrlToBeSaved(request.logoImageUrl());
        s3Service.doesFileExist(imgUrl);

        Team team = request.toEntity(imgUrl);
        team.setOrganization(member.getOrganization());
        teamRepository.save(team);
        return team.getId();
    }

    public void update(final Member member, final TeamRequest.Update request, final Long teamId) {
        Team team = entityUtils.getEntity(teamId, Team.class);
        PermissionValidator.checkPermission(team, member);

        s3Service.doesFileExist(team.getLogoImageUrl());
        Unit unit = Optional.ofNullable(request.unit()).map(Unit::from).orElse(null);
        team.update(request.name(), request.logoImageUrl(), originPrefix, replacePrefix, unit, request.teamColor());

        if (request.teamPlayers() != null) {
            upsertPlayersToTeam(team, request.teamPlayers());
        }
    }

    public void delete(final Member member, final Long teamId) {
        Team team = entityUtils.getEntity(teamId, Team.class);
        PermissionValidator.checkPermission(team, member);
        teamRepository.delete(team);
    }

    public void addPlayersToTeam(final Member member, final Long teamId, final List<TeamRequest.TeamPlayerRegister> request) {
        Team team = entityUtils.getEntity(teamId, Team.class);
        PermissionValidator.checkPermission(team, member);

        List<Player> players = fetchAndValidatePlayers(request);
        Map<Long, Integer> jerseyNumbers = buildJerseyNumberMap(request);

        List<TeamPlayer> newTeamPlayers = players.stream()
                .map(player -> team.addPlayer(player, jerseyNumbers.get(player.getId())))
                .toList();

        teamPlayerRepository.saveAll(newTeamPlayers);
    }

    public void deleteTeamPlayer(final Member member, final Long teamPlayerId) {
        TeamPlayer teamPlayer = entityUtils.getEntity(teamPlayerId, TeamPlayer.class);
        Team team = teamPlayer.getTeam();
        PermissionValidator.checkPermission(team, member);

        Player player = teamPlayer.getPlayer();
        team.removeTeamPlayer(player);
        teamPlayerRepository.delete(teamPlayer);
    }

    public void deleteLogoImage(final Member member, Long teamId) {
        Team team = entityUtils.getEntity(teamId, Team.class);
        PermissionValidator.checkPermission(team, member);
        team.deleteLogoImageUrl();
    }

    private void upsertPlayersToTeam(Team team, List<TeamRequest.TeamPlayerRegister> request) {
        List<Player> players = fetchAndValidatePlayers(request);
        Map<Long, Integer> jerseyNumbers = buildJerseyNumberMap(request);
        Map<Long, TeamPlayer> existingTeamPlayersMap = buildExistingTeamPlayerMap(team.getId());

        updateExistingPlayers(players, jerseyNumbers, existingTeamPlayersMap);

        List<TeamPlayer> newTeamPlayers = createNewTeamPlayers(team, players, jerseyNumbers, existingTeamPlayersMap);
        if (!newTeamPlayers.isEmpty()) {
            teamPlayerRepository.saveAll(newTeamPlayers);
        }
    }

    private List<Player> fetchAndValidatePlayers(List<TeamRequest.TeamPlayerRegister> request) {
        List<Long> playerIds = request.stream().map(TeamRequest.TeamPlayerRegister::playerId).toList();
        List<Player> players = playerRepository.findAllById(playerIds);
        validateExistence(players, playerIds);
        return players;
    }

    private Map<Long, TeamPlayer> buildExistingTeamPlayerMap(Long teamId) {
        return teamPlayerRepository.findTeamPlayersWithPlayerByTeamId(teamId)
                .stream()
                .collect(Collectors.toMap(tp -> tp.getPlayer().getId(), Function.identity()));
    }

    private void updateExistingPlayers(List<Player> players, Map<Long, Integer> jerseyNumbers,
                                        Map<Long, TeamPlayer> existingTeamPlayersMap) {
        players.stream()
                .filter(player -> existingTeamPlayersMap.containsKey(player.getId()))
                .forEach(player -> existingTeamPlayersMap.get(player.getId())
                        .updateJerseyNumber(jerseyNumbers.get(player.getId())));
    }

    private List<TeamPlayer> createNewTeamPlayers(Team team, List<Player> players,
                                                   Map<Long, Integer> jerseyNumbers,
                                                   Map<Long, TeamPlayer> existingTeamPlayersMap) {
        return players.stream()
                .filter(player -> !existingTeamPlayersMap.containsKey(player.getId()))
                .map(player -> TeamPlayer.of(team, player, jerseyNumbers.get(player.getId())))
                .toList();
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
