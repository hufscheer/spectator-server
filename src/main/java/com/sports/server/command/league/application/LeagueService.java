package com.sports.server.command.league.application;

import com.sports.server.command.league.domain.LeagueTeam;
import com.sports.server.command.team.domain.Team;
import com.sports.server.command.team.domain.TeamRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sports.server.auth.exception.AuthorizationErrorMessages;
import com.sports.server.command.league.domain.League;
import com.sports.server.command.league.domain.LeagueRepository;
import com.sports.server.command.league.domain.Round;
import com.sports.server.command.league.dto.LeagueRequestDto;
import com.sports.server.command.member.domain.Member;
import com.sports.server.command.organization.domain.Organization;
import com.sports.server.common.application.EntityUtils;
import com.sports.server.common.exception.CustomException;
import com.sports.server.common.exception.UnauthorizedException;

import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class LeagueService {
    private final EntityUtils entityUtils;
    private final LeagueRepository leagueRepository;
    private final TeamRepository teamRepository;

    public void register(final Member manager, final LeagueRequestDto.Register request) {
        League league = request.toEntity(manager);
        leagueRepository.save(league);

        if (request.teamIds() != null && !request.teamIds().isEmpty()) {
            addTeamsToLeague(league, request.teamIds());
        }
    }

    public void update(final Member manager, final LeagueRequestDto.Update request, final Long leagueId) {
        League league = entityUtils.getEntity(leagueId, League.class);
        if (!league.isManagedBy(manager)) {
            throw new CustomException(HttpStatus.FORBIDDEN, "해당 대회를 수정할 권한이 없습니다.");
        }

        league.updateInfo(request.name(), request.startAt(), request.endAt(), Round.from(request.maxRound()));

        if (request.teamIds() != null) {
            updateLeagueTeams(league, request.teamIds());
        }
    }

    public void delete(final Member manager, final Long leagueId) {
        League league = entityUtils.getEntity(leagueId, League.class);
        if (!league.isManagedBy(manager)) {
            throw new UnauthorizedException(AuthorizationErrorMessages.PERMISSION_DENIED);
        }

        leagueRepository.delete(league);
    }

    private void addTeamsToLeague(League league, List<Long> teamIds) {
        List<Team> teams = teamRepository.findAllById(teamIds);
        
        for (Team team : teams) {
            // 이미 연결되어 있는지 확인
            boolean alreadyLinked = league.getLeagueTeams().stream()
                    .anyMatch(lt -> lt.getTeam().getId().equals(team.getId()));
                    
            if (!alreadyLinked) {
                LeagueTeam.of(league, team);
            }
        }
    }

    private void updateLeagueTeams(League league, List<Long> teamIds) {
        // 현재 리그에 연결된 팀 ID 목록
        Set<Long> currentTeamIds = league.getLeagueTeams().stream()
                .map(lt -> lt.getTeam().getId())
                .collect(Collectors.toSet());
        
        // 추가할 팀 ID 목록 (새로 전달된 ID 중 현재 연결되지 않은 것)
        Set<Long> teamsToAdd = teamIds.stream()
                .filter(id -> !currentTeamIds.contains(id))
                .collect(Collectors.toSet());
        
        // 제거할 팀 목록 (현재 연결된 ID 중 새로 전달되지 않은 것)
        Set<Long> teamsToRemove = currentTeamIds.stream()
                .filter(id -> !teamIds.contains(id))
                .collect(Collectors.toSet());

        if (!teamsToRemove.isEmpty()) {
            league.getLeagueTeams().removeIf(leagueTeam -> 
                    teamsToRemove.contains(leagueTeam.getTeam().getId()));
        }
        
        // 추가할 팀 처리
        if (!teamsToAdd.isEmpty()) {
            List<Team> teamsToAddEntities = teamRepository.findAllById(new ArrayList<>(teamsToAdd));
            for (Team team : teamsToAddEntities) {
                LeagueTeam.of(league, team);
            }
        }
    }
}