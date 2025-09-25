package com.sports.server.query.application;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sports.server.command.cheertalk.domain.CheerTalk;
import com.sports.server.command.league.domain.League;
import com.sports.server.command.member.domain.Member;
import com.sports.server.common.application.EntityUtils;
import com.sports.server.common.application.PermissionValidator;
import com.sports.server.common.dto.PageRequestDto;
import com.sports.server.query.dto.response.CheerTalkResponse;
import com.sports.server.query.repository.CheerTalkDynamicRepository;
import com.sports.server.query.repository.GameQueryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CheerTalkQueryService {

	private final CheerTalkDynamicRepository cheerTalkDynamicRepository;

	private final GameQueryRepository gameQueryRepository;

	private final EntityUtils entityUtils;

	public List<CheerTalkResponse.ForManager> getAllUnblockedCheerTalks(final PageRequestDto pageRequest){
		List<CheerTalk> cheerTalks = cheerTalkDynamicRepository.findAllUnblocked(
				pageRequest.cursor(), pageRequest.size());

		return cheerTalks.stream()
				.map(CheerTalkResponse.ForManager::new)
				.collect(Collectors.toList());
	}

	public List<CheerTalkResponse.ForManager> getAllBlockedCheerTalks(final PageRequestDto pageRequest){
		List<CheerTalk> cheerTalks = cheerTalkDynamicRepository.findAllBlocked(
				pageRequest.cursor(), pageRequest.size());

		return cheerTalks.stream()
				.map(CheerTalkResponse.ForManager::new).toList();
	}

	public List<CheerTalkResponse.ForSpectator> getCheerTalksByGameId(final Long gameId,
		final PageRequestDto pageRequest) {
		List<CheerTalk> cheerTalks = cheerTalkDynamicRepository.findByGameIdOrderByStartTime(
			gameId, pageRequest.cursor(), pageRequest.size()
		);

		List<CheerTalkResponse.ForSpectator> responses = cheerTalks.stream()
			.map(CheerTalkResponse.ForSpectator::new)
			.collect(Collectors.toList());

		Collections.reverse(responses);
		return responses;
	}

	public List<CheerTalkResponse.ForManager> getReportedCheerTalksByLeagueId(final Long leagueId,
		final PageRequestDto pageRequest,
		final Member manager) {
		League league = entityUtils.getEntity(leagueId, League.class);
		PermissionValidator.checkPermission(league, manager);

		List<CheerTalk> reportedCheerTalks = cheerTalkDynamicRepository.findReportedCheerTalksByLeagueId(
			leagueId, pageRequest.cursor(), pageRequest.size()
		);

		return reportedCheerTalks.stream()
			.map(cheerTalk -> new CheerTalkResponse.ForManager(cheerTalk,
				gameQueryRepository.findByGameTeamIdWithLeague(cheerTalk.getGameTeamId()))).toList();
	}

	public List<CheerTalkResponse.ForManager> getUnblockedCheerTalksByLeagueId(Long leagueId,
		PageRequestDto pageRequest,
		Member manager) {
		League league = entityUtils.getEntity(leagueId, League.class);
		PermissionValidator.checkPermission(league, manager);

		List<CheerTalk> cheerTalks = cheerTalkDynamicRepository.findUnblockedCheerTalksByLeagueId(
			leagueId, pageRequest.cursor(), pageRequest.size()
		);

		return cheerTalks.stream()
			.map(cheerTalk -> new CheerTalkResponse.ForManager(cheerTalk,
				gameQueryRepository.findByGameTeamIdWithLeague(cheerTalk.getGameTeamId()))).toList();
	}

	public List<CheerTalkResponse.ForManager> getBlockedCheerTalksByLeagueId(final Long leagueId,
		final PageRequestDto pageable,
		final Member member) {
		League league = entityUtils.getEntity(leagueId, League.class);

		PermissionValidator.checkPermission(league, member);

		List<CheerTalk> blockedCheerTalks = cheerTalkDynamicRepository.findBlockedCheerTalksByLeagueId(
			leagueId, pageable.cursor(), pageable.size());

		return blockedCheerTalks.stream()
			.map(cheerTalk -> new CheerTalkResponse.ForManager(cheerTalk,
				gameQueryRepository.findByGameTeamIdWithLeague(cheerTalk.getGameTeamId()))).toList();
	}
}
