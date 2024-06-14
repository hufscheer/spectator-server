package com.sports.server.query.application;

import com.sports.server.common.exception.NotFoundException;
import com.sports.server.query.dto.response.LeagueDetailResponse;
import com.sports.server.query.dto.response.LeagueResponse;
import com.sports.server.query.dto.response.LeagueSportResponse;
import com.sports.server.query.dto.response.LeagueTeamPlayerResponse;
import com.sports.server.query.dto.response.LeagueTeamResponse;
import com.sports.server.query.repository.LeagueQueryRepository;
import com.sports.server.query.repository.LeagueSportQueryRepository;
import com.sports.server.query.repository.LeagueTeamPlayerQueryRepository;
import com.sports.server.query.repository.LeagueTeamQueryRepository;

import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class LeagueQueryService {

	private final LeagueQueryRepository leagueQueryRepository;
	private final LeagueSportQueryRepository leagueSportQueryRepository;
	private final LeagueTeamQueryRepository leagueTeamQueryRepository;
	private final LeagueTeamPlayerQueryRepository leagueTeamPlayerQueryRepository;

	public List<LeagueResponse> findLeagues(Integer year) {
		return leagueQueryRepository.findByYear(year)
			.stream()
			.map(LeagueResponse::new)
			.toList();
	}

	public List<LeagueSportResponse> findSportsByLeague(Long leagueId) {
		return leagueSportQueryRepository.findByLeagueId(leagueId)
			.stream()
			.map(LeagueSportResponse::new)
			.toList();
	}

	public List<LeagueTeamResponse> findTeamsByLeague(Long leagueId, Integer round) {
		if (round != null) {
			return leagueTeamQueryRepository.findByLeagueIdAndRound(leagueId, round)
				.stream()
				.map(LeagueTeamResponse::new)
				.toList();
		}
		return leagueTeamQueryRepository.findByLeagueId(leagueId)
			.stream()
			.map(LeagueTeamResponse::new)
			.toList();
	}

	public LeagueDetailResponse findLeagueDetail(Long leagueId) {
		return leagueQueryRepository.findById(leagueId)
			.map(LeagueDetailResponse::new)
			.orElseThrow(() -> new NotFoundException("존재하지 않는 리그입니다"));
	}

	public List<LeagueTeamPlayerResponse> findPlayersByLeagueTeam(Long leagueTeamId) {
		return leagueTeamPlayerQueryRepository.findByLeagueTeamId(leagueTeamId)
			.stream()
			.map(LeagueTeamPlayerResponse::new)
			.toList();
	}
}
