package com.sports.server.query.application;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sports.server.command.cheertalk.domain.CheerTalk;
import com.sports.server.command.member.domain.Member;
import com.sports.server.common.dto.PageRequestDto;
import com.sports.server.query.dto.GameTeamGameInfoDto;
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

	public List<CheerTalkResponse.ForSpectator> getCheerTalksByGameId(final Long gameId, final PageRequestDto pageRequest) {
		List<CheerTalk> cheerTalks = cheerTalkDynamicRepository.findByGameIdOrderByStartTime(
			gameId, pageRequest.cursor(), pageRequest.size()
		);

		List<CheerTalkResponse.ForSpectator> responses = cheerTalks.stream()
			.map(CheerTalkResponse.ForSpectator::new)
			.collect(Collectors.toList());

		Collections.reverse(responses);
		return responses;
	}

	public List<CheerTalkResponse.ForManager> getReportedCheerTalksByAdmin(final PageRequestDto pageRequest, final Member admin) {
		List<CheerTalk> cheerTalks = cheerTalkDynamicRepository.findReportedCheerTalksByAdminId(
				admin.getId(), pageRequest.cursor(), pageRequest.size()
		);
		return toForManagerResponses(cheerTalks);
	}

	public List<CheerTalkResponse.ForManager> getUnblockedCheerTalksByAdmin(final PageRequestDto pageRequest, final Member admin) {
		List<CheerTalk> cheerTalks = cheerTalkDynamicRepository.findUnblockedCheerTalksByAdminId(
				admin.getId(), pageRequest.cursor(), pageRequest.size()
		);
		return toForManagerResponses(cheerTalks);
	}

	public List<CheerTalkResponse.ForManager> getBlockedCheerTalksByAdmin(final PageRequestDto pageRequest, final Member admin) {
		List<CheerTalk> cheerTalks = cheerTalkDynamicRepository.findBlockedCheerTalksByAdminId(
				admin.getId(), pageRequest.cursor(), pageRequest.size()
		);
		return toForManagerResponses(cheerTalks);
	}

	public List<CheerTalkResponse.ForManager> getReportedCheerTalksByLeagueId(final Long leagueId, final PageRequestDto pageRequest) {
		List<CheerTalk> cheerTalks = cheerTalkDynamicRepository.findReportedCheerTalksByLeagueId(
				leagueId, pageRequest.cursor(), pageRequest.size()
		);
		return toForManagerResponses(cheerTalks);
	}

	public List<CheerTalkResponse.ForManager> getUnblockedCheerTalksByLeagueId(final Long leagueId, final PageRequestDto pageRequest) {
		List<CheerTalk> cheerTalks = cheerTalkDynamicRepository.findUnblockedCheerTalksByLeagueId(
				leagueId, pageRequest.cursor(), pageRequest.size()
		);
		return toForManagerResponses(cheerTalks);
	}

	public List<CheerTalkResponse.ForManager> getBlockedCheerTalksByLeagueId(final Long leagueId, final PageRequestDto pageRequest) {
		List<CheerTalk> cheerTalks = cheerTalkDynamicRepository.findBlockedCheerTalksByLeagueId(
				leagueId, pageRequest.cursor(), pageRequest.size()
		);
		return toForManagerResponses(cheerTalks);
	}

	public List<CheerTalkResponse.ForManager> getReportedCheerTalksByGameId(final Long gameId, final PageRequestDto pageRequest) {
		List<CheerTalk> cheerTalks = cheerTalkDynamicRepository.findReportedCheerTalksByGameId(
				gameId, pageRequest.cursor(), pageRequest.size()
		);
		return toForManagerResponses(cheerTalks);
	}

	public List<CheerTalkResponse.ForManager> getBlockedCheerTalksByGameId(final Long gameId, final PageRequestDto pageRequest) {
		List<CheerTalk> cheerTalks = cheerTalkDynamicRepository.findBlockedCheerTalksByGameId(
				gameId, pageRequest.cursor(), pageRequest.size()
		);
		return toForManagerResponses(cheerTalks);
	}

	private List<CheerTalkResponse.ForManager> toForManagerResponses(List<CheerTalk> cheerTalks) {
		List<Long> gameTeamIds = cheerTalks.stream()
				.map(CheerTalk::getGameTeamId)
				.distinct()
				.toList();

		Map<Long, GameTeamGameInfoDto> gameInfoMap = gameQueryRepository.findGameInfoByGameTeamIds(gameTeamIds)
				.stream()
				.collect(Collectors.toMap(GameTeamGameInfoDto::gameTeamId, Function.identity()));

		return cheerTalks.stream()
				.map(ct -> {
					GameTeamGameInfoDto info = gameInfoMap.get(ct.getGameTeamId());
					return new CheerTalkResponse.ForManager(
							ct.getId(), info.gameId(), info.leagueId(), ct.getContent(),
							ct.getGameTeamId(), ct.getCreatedAt(), ct.isBlocked(),
							info.gameName(), info.leagueName()
					);
				})
				.toList();
	}
}
