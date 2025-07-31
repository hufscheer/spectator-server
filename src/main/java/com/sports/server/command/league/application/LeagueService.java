package com.sports.server.command.league.application;

import com.sports.server.command.league.domain.*;
import com.sports.server.command.player.domain.Player;
import com.sports.server.command.team.domain.Team;
import com.sports.server.command.team.domain.TeamPlayerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sports.server.auth.exception.AuthorizationErrorMessages;
import com.sports.server.command.league.dto.LeagueRequest;
import com.sports.server.command.member.domain.Member;
import com.sports.server.common.application.EntityUtils;
import com.sports.server.common.exception.UnauthorizedException;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Set;

@Service
@Transactional
@RequiredArgsConstructor
public class LeagueService {

    private final EntityUtils entityUtils;
    private final LeagueRepository leagueRepository;
	private final LeagueTeamRepository leagueTeamRepository;
	private final TeamPlayerRepository teamPlayerRepository;

	public void register(final Member manager, final LeagueRequest.Register request) {
		leagueRepository.save(request.toEntity(manager));
	}

	public void update(final Member manager, final LeagueRequest.Update request, final Long leagueId) {
		League league = findLeagueAndValidatePermission(leagueId, manager);
		league.updateInfo(request.name(), request.startAt(), request.endAt(), Round.from(request.maxRound()));
	}

    public void delete(final Member manager, final Long leagueId) {
        League league = findLeagueAndValidatePermission(leagueId, manager);
        leagueRepository.delete(league);
    }

	public void registerTeamWithPlayers(final Long leagueId, final LeagueRequest.TeamAndPlayersRegister request, final Member manager) {
		League league = findLeagueAndValidatePermission(leagueId, manager);
		Team team = entityUtils.getEntity(request.teamId(), Team.class);

		if (leagueTeamRepository.existsByLeagueIdAndTeamId(leagueId, team.getId())) {
			throw new IllegalArgumentException("이미 해당 리그에 참가중인 팀입니다.");
		}

		LeagueTeam leagueTeam = LeagueTeam.of(league, team);
		validateTeamPlayers(team, request.players());

		request.players().forEach(playerInfo -> {
			Player player = entityUtils.getEntity(playerInfo.playerId(), Player.class);
			LeagueTeamPlayer.of(leagueTeam, player);
		});

		leagueTeamRepository.save(leagueTeam);
	}

	public void removeTeamFromLeague(final Member manager, final Long leagueId, final Long teamId){
		League league = findLeagueAndValidatePermission(leagueId, manager);

		LeagueTeam leagueTeam = leagueTeamRepository.findByLeagueIdAndTeamId(leagueId, teamId)
				.orElseThrow(() -> new IllegalArgumentException("리그에 참가중인 팀이 아닙니다."));

		league.removeLeagueTeam(leagueTeam);
		leagueTeam.getTeam().removeLeagueTeam(leagueTeam);
	}

	public void removePlayerFromLeagueTeamPlayers(final Long leagueId, final Long leagueTeamPlayerId, final Member manager) {
		findLeagueAndValidatePermission(leagueId, manager);
		LeagueTeamPlayer leagueTeamPlayer = entityUtils.getEntity(leagueTeamPlayerId, LeagueTeamPlayer.class);

		LeagueTeam leagueTeam = leagueTeamPlayer.getLeagueTeam();
		leagueTeam.removeLeagueTeamPlayer(leagueTeamPlayer);
	}

	private League findLeagueAndValidatePermission(final Long leagueId, final Member manager) {
		League league = entityUtils.getEntity(leagueId, League.class);
		if (!league.isManagedBy(manager)) {
			throw new UnauthorizedException(AuthorizationErrorMessages.PERMISSION_DENIED);
		}
		return league;
	}

	private void validateTeamPlayers(final Team team, final List<LeagueRequest.PlayerInfo> playersToRegister) {
		Set<Long> teamPlayerIds = teamPlayerRepository.findPlayerIdsByTeam(team);
		for (LeagueRequest.PlayerInfo playerInfo : playersToRegister) {
			if (!teamPlayerIds.contains(playerInfo.playerId())) {
				throw new IllegalArgumentException("팀 소속이 아닌 선수를 리그에 등록할 수 없습니다.");
			}
		}
	}
}
