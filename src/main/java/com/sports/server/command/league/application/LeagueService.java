package com.sports.server.command.league.application;

import com.sports.server.command.league.domain.*;
import com.sports.server.command.team.domain.Team;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sports.server.auth.exception.AuthorizationErrorMessages;
import com.sports.server.command.league.dto.LeagueRequest;
import com.sports.server.command.member.domain.Member;
import com.sports.server.common.application.EntityUtils;
import com.sports.server.common.exception.UnauthorizedException;

import lombok.RequiredArgsConstructor;


@Service
@Transactional
@RequiredArgsConstructor
public class LeagueService {

    private final EntityUtils entityUtils;
    private final LeagueRepository leagueRepository;
	private final LeagueTeamRepository leagueTeamRepository;

	public void register(final Member manager, final LeagueRequest.Register request) {
		leagueRepository.save(request.toEntity(manager));
	}

	public void update(final Member manager, final LeagueRequest.Update request, final Long leagueId) {
		League league = findValidatedLeague(leagueId, manager);
		league.updateInfo(request.name(), request.startAt(), request.endAt(), Round.from(request.maxRound()));
	}

    public void delete(final Member manager, final Long leagueId) {
        League league = findValidatedLeague(leagueId, manager);
        leagueRepository.delete(league);
    }

	public void registerTeam(final Long leagueId, final LeagueRequest.TeamRegister request, final Member manager) {
		League league = findValidatedLeague(leagueId, manager);
		Team team = entityUtils.getEntity(request.teamId(), Team.class);

		if (leagueTeamRepository.existsByLeagueIdAndTeamId(leagueId, team.getId())) {
			throw new IllegalArgumentException("이미 해당 리그에 참가중인 팀입니다.");
		}

		LeagueTeam leagueTeam = LeagueTeam.of(league, team);
		leagueTeamRepository.save(leagueTeam);
	}

	public void removeTeamFromLeague(final Member manager, final Long leagueId, final Long teamId){
		League league = findValidatedLeague(leagueId, manager);
		Team team = entityUtils.getEntity(teamId, Team.class);

		LeagueTeam leagueTeam = leagueTeamRepository.findByLeagueIdAndTeamId(leagueId, teamId)
				.orElseThrow(() -> new IllegalArgumentException("리그에 참가중인 팀이 아닙니다."));

		league.removeLeagueTeam(leagueTeam);
		team.removeLeagueTeam(leagueTeam);
	}

	private League findValidatedLeague(final Long leagueId, final Member manager) {
		League league = entityUtils.getEntity(leagueId, League.class);
		if (!league.isManagedBy(manager)) {
			throw new UnauthorizedException(AuthorizationErrorMessages.PERMISSION_DENIED);
		}
		return league;
	}
}
