package com.sports.server.command.team.application;

import com.sports.server.command.game.domain.GameTeamRepository;
import com.sports.server.command.league.domain.LeagueTeamRepository;
import com.sports.server.command.member.domain.Member;
import com.sports.server.command.player.domain.Player;
import com.sports.server.command.player.domain.PlayerRepository;
import com.sports.server.command.player.exception.PlayerErrorMessages;
import com.sports.server.command.team.domain.*;
import com.sports.server.command.team.dto.TeamRequest;
import com.sports.server.command.team.exception.TeamErrorMessages;
import com.sports.server.common.application.EntityUtils;
import com.sports.server.common.application.PermissionValidator;
import com.sports.server.common.application.S3Service;
import com.sports.server.common.exception.BadRequestException;
import com.sports.server.common.exception.CustomException;
import com.sports.server.common.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
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
    private final GameTeamRepository gameTeamRepository;
    private final LeagueTeamRepository leagueTeamRepository;
    private final TeamPlayerRepository teamPlayerRepository;
    private final PlayerRepository playerRepository;
    private final UnitRepository unitRepository;
    private final EntityUtils entityUtils;
    private final S3Service s3Service;
    private final ApplicationEventPublisher eventPublisher;

    public void register(final Member member, final TeamRequest.Register request) {
        String imgUrl = changeLogoImageUrlToBeSaved(request.logoImageUrl());
        s3Service.doesFileExist(extractS3Key(request.logoImageUrl()));

        Unit unit = findUnit(request.unit(), member.getOrganization().getId());
        Team team = request.toEntity(imgUrl, unit);
        teamRepository.save(team);
        eventPublisher.publishEvent(new LogoImageNormalizationRequestedEvent(request.logoImageUrl()));

        if (request.teamPlayers() != null && !request.teamPlayers().isEmpty()) {
            addPlayersToTeam(member, team.getId(), request.teamPlayers());
        }
    }

    public Long registerAndReturnId(final Member member, final TeamRequest.Register request) {
        String imgUrl = changeLogoImageUrlToBeSaved(request.logoImageUrl());
        s3Service.doesFileExist(extractS3Key(request.logoImageUrl()));

        Unit unit = findUnit(request.unit(), member.getOrganization().getId());
        Team team = request.toEntity(imgUrl, unit);
        teamRepository.save(team);
        eventPublisher.publishEvent(new LogoImageNormalizationRequestedEvent(request.logoImageUrl()));
        return team.getId();
    }

    public void update(final Member member, final TeamRequest.Update request, final Long teamId) {
        Team team = entityUtils.getEntity(teamId, Team.class);
        PermissionValidator.checkPermission(team, member);

        Unit unit = Optional.ofNullable(request.unit())
                .map(unitName -> findUnit(unitName, team.getUnit().getOrganization().getId()))
                .orElse(null);
        String resolvedLogoUrl = resolveLogoImageUrl(request.logoImageUrl(), team);
        team.update(request.name(), resolvedLogoUrl, unit, request.teamColor());
        if (resolvedLogoUrl != null) {
            eventPublisher.publishEvent(new LogoImageNormalizationRequestedEvent(request.logoImageUrl()));
        }

        if (request.teamPlayers() != null) {
            upsertPlayersToTeam(member, team, request.teamPlayers());
        }
    }

    public void delete(final Member member, final Long teamId) {
        Team team = entityUtils.getEntity(teamId, Team.class);
        PermissionValidator.checkPermission(team, member);
        validateNotReferenced(team);
        teamRepository.delete(team);
    }

    /**
     * 팀은 소프트 삭제(@SQLDelete)라 삭제해도 teams 행이 남는다. 그런데 조회 시에는
     * @Where(is_deleted = 0) 때문에 그 행을 찾지 못한다.
     *
     * <p>그래서 game_teams·league_teams 가 계속 이 팀을 가리키고 있으면, 나중에 그 연관을
     * 타고 팀을 로드하는 순간 EntityNotFoundException 이 터져 조회 API 가 통째로 500 이 된다.
     * 매니저가 대회 화면을 아예 열지 못하게 되므로 남은 참조가 있으면 삭제를 막는다.
     */
    private void validateNotReferenced(final Team team) {
        if (gameTeamRepository.existsByTeamId(team.getId())) {
            throw new CustomException(HttpStatus.CONFLICT, TeamErrorMessages.TEAM_IN_GAME_DELETE_EXCEPTION);
        }
        if (leagueTeamRepository.existsByTeamId(team.getId())) {
            throw new CustomException(HttpStatus.CONFLICT, TeamErrorMessages.TEAM_IN_LEAGUE_DELETE_EXCEPTION);
        }
    }

    public void addPlayersToTeam(final Member member, final Long teamId, final List<TeamRequest.TeamPlayerRegister> request) {
        Team team = entityUtils.getEntity(teamId, Team.class);
        PermissionValidator.checkPermission(team, member);

        List<Player> players = fetchAndValidatePlayers(request);
        validatePlayersOrganization(players, member);
        Map<Long, TeamRequest.TeamPlayerRegister> requests = buildRequestMap(request);

        List<TeamPlayer> newTeamPlayers = players.stream()
                .map(player -> {
                    TeamRequest.TeamPlayerRegister playerRequest = requests.get(player.getId());
                    return team.addPlayer(player, playerRequest.jerseyNumber());
                })
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

    private void upsertPlayersToTeam(Member member, Team team, List<TeamRequest.TeamPlayerRegister> request) {
        List<Player> players = fetchAndValidatePlayers(request);
        validatePlayersOrganization(players, member);
        Map<Long, TeamRequest.TeamPlayerRegister> requests = buildRequestMap(request);
        Map<Long, TeamPlayer> existingTeamPlayersMap = buildExistingTeamPlayerMap(team.getId());

        updateExistingPlayers(players, requests, existingTeamPlayersMap);

        List<TeamPlayer> newTeamPlayers = createNewTeamPlayers(team, players, requests, existingTeamPlayersMap);
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

    private void updateExistingPlayers(List<Player> players, Map<Long, TeamRequest.TeamPlayerRegister> requests,
                                        Map<Long, TeamPlayer> existingTeamPlayersMap) {
        players.stream()
                .filter(player -> existingTeamPlayersMap.containsKey(player.getId()))
                .forEach(player -> {
                    TeamRequest.TeamPlayerRegister playerRequest = requests.get(player.getId());
                    TeamPlayer teamPlayer = existingTeamPlayersMap.get(player.getId());
                    teamPlayer.updateJerseyNumber(playerRequest.jerseyNumber());
                });
    }

    private List<TeamPlayer> createNewTeamPlayers(Team team, List<Player> players,
                                                   Map<Long, TeamRequest.TeamPlayerRegister> requests,
                                                   Map<Long, TeamPlayer> existingTeamPlayersMap) {
        return players.stream()
                .filter(player -> !existingTeamPlayersMap.containsKey(player.getId()))
                .map(player -> {
                    TeamRequest.TeamPlayerRegister playerRequest = requests.get(player.getId());
                    return TeamPlayer.of(team, player, playerRequest.jerseyNumber());
                })
                .toList();
    }

    private static void validateExistence(List<Player> players, List<Long> playerIds) {
        if (players.size() != new HashSet<>(playerIds).size()) {
            throw new NotFoundException(PlayerErrorMessages.PLAYER_NOT_EXIST_EXCEPTION);
        }
    }

    private void validatePlayersOrganization(List<Player> players, Member member) {
        players.forEach(player -> PermissionValidator.checkPermission(player, member));
    }

    private Map<Long, TeamRequest.TeamPlayerRegister> buildRequestMap(
            List<TeamRequest.TeamPlayerRegister> request) {
        return request.stream()
                .collect(Collectors.toMap(
                        TeamRequest.TeamPlayerRegister::playerId,
                        Function.identity()
                ));
    }

    private String resolveLogoImageUrl(String requestLogoImageUrl, Team team) {
        if (requestLogoImageUrl == null || requestLogoImageUrl.equals(team.getLogoImageUrl())) {
            return null;
        }
        String convertedUrl = changeLogoImageUrlToBeSaved(requestLogoImageUrl);
        if (convertedUrl.equals(team.getLogoImageUrl())) {
            return null;
        }
        return convertedUrl;
    }

    private Unit findUnit(String unitName, Long organizationId) {
        return unitRepository.findByNameAndOrganizationId(unitName, organizationId)
                .orElseThrow(() -> new NotFoundException(TeamErrorMessages.UNIT_NOT_FOUND_EXCEPTION));
    }

    private String changeLogoImageUrlToBeSaved(String logoImageUrl) {
        if (!logoImageUrl.contains(originPrefix)) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "잘못된 이미지 url 입니다.");
        }
        return logoImageUrl.replace(originPrefix, replacePrefix);
    }

    private String extractS3Key(String url) {
        if (url == null || url.isBlank()) {
            return null;
        }
        if (url.startsWith(originPrefix)) {
            return url.substring(originPrefix.length());
        }
        if (url.startsWith(replacePrefix)) {
            return url.substring(replacePrefix.length());
        }
        return null;
    }
}
