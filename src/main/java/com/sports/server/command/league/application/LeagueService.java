package com.sports.server.command.league.application;

import com.sports.server.command.league.domain.*;
import com.sports.server.command.league.exception.LeagueErrorMessages;
import com.sports.server.command.team.domain.Team;
import com.sports.server.command.team.domain.TeamRepository;
import com.sports.server.command.team.exception.TeamErrorMessages;
import com.sports.server.common.exception.CustomException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sports.server.auth.exception.AuthorizationErrorMessages;
import com.sports.server.command.league.dto.LeagueRequest;
import com.sports.server.command.member.domain.Member;
import com.sports.server.common.application.EntityUtils;
import com.sports.server.common.exception.UnauthorizedException;

import lombok.RequiredArgsConstructor;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class LeagueService {

    private final EntityUtils entityUtils;
    private final LeagueRepository leagueRepository;
	private final LeagueTeamRepository leagueTeamRepository;
	private final TeamRepository teamRepository;

	public void register(final Member manager, final LeagueRequest.Register request) {
		League league = leagueRepository.save(request.toEntity(manager));

		List<Team> teams = teamRepository.findAllById(request.teamIds());
		if (teams.size() != request.teamIds().size()) {
			throw new CustomException(HttpStatus.BAD_REQUEST, TeamErrorMessages.TEAM_NOT_FOUND_EXCEPTION);
		}

		List<LeagueTeam> leagueTeams = teams.stream()
				.map(team -> LeagueTeam.of(league, team))
				.toList();

		leagueTeamRepository.saveAll(leagueTeams);
	}

	public void update(final Member manager, final LeagueRequest.Update request, final Long leagueId) {
		League league = findValidatedLeague(leagueId, manager);
		league.updateInfo(request.name(), request.startAt(), request.endAt(), Round.from(request.maxRound()));
	}

    public void delete(final Member manager, final Long leagueId) {
        League league = findValidatedLeague(leagueId, manager);
        leagueRepository.delete(league);
    }

	public League addTeams(final Member administrator, final Long leagueId, final LeagueRequest.Teams request) {
		League league = leagueRepository.findWithTeamsById(leagueId)
				.orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "리그를 찾을 수 없습니다."));
		findValidatedLeague(leagueId, administrator);

		List<Long> requestedTeamIds = request.teamIds();
		List<Team> teams = teamRepository.findAllById(requestedTeamIds);
		if (teams.size() != requestedTeamIds.size()) {
			throw new CustomException(HttpStatus.BAD_REQUEST, TeamErrorMessages.TEAMS_NOT_EXIST_INCLUDED_EXCEPTION);
		}

		List<Long> existingTeamIds = leagueTeamRepository.findTeamIdsByLeagueIdAndTeamIdIn(leagueId, requestedTeamIds);
		List<Team> teamsToAdd = teams.stream()
				.filter(team -> !existingTeamIds.contains(team.getId()))
				.toList();

		if (teamsToAdd.isEmpty()) {
			throw new CustomException(HttpStatus.BAD_REQUEST, TeamErrorMessages.INVALID_LEAGUE_TEAMS_REQUEST_EXCEPTION);
		}

		List<LeagueTeam> leagueTeams = teamsToAdd.stream()
				.map(team -> LeagueTeam.of(league, team))
				.toList();

		leagueTeamRepository.saveAll(leagueTeams);
		return league;
	}

	public void removeTeams(final Member manager, final Long leagueId, final LeagueRequest.Teams request){
		findValidatedLeague(leagueId, manager);
		List<Long> teamIdsToRemove = request.teamIds();

		long foundCount = leagueTeamRepository.countByLeagueIdAndTeamIdIn(leagueId, teamIdsToRemove);
		if (foundCount != teamIdsToRemove.size()) {
			throw new CustomException(HttpStatus.BAD_REQUEST, LeagueErrorMessages.TEAMS_NOT_EXIST_IN_LEAGUE_TEAM_EXCEPTION);
		}

		leagueTeamRepository.deleteByLeagueIdAndTeamIdIn(leagueId, request.teamIds()); // bulk 연산
	}

	private League findValidatedLeague(final Long leagueId, final Member manager) {
		League league = entityUtils.getEntity(leagueId, League.class);
		if (!league.isManagedBy(manager)) {
			throw new UnauthorizedException(AuthorizationErrorMessages.PERMISSION_DENIED);
		}
		return league;
	}
}
