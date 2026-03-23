package com.sports.server.query.application;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.sports.server.command.game.domain.Game;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sports.server.command.cheertalk.domain.CheerTalk;
import com.sports.server.command.member.domain.Member;
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
		List<CheerTalk> reportedCheerTalks = cheerTalkDynamicRepository.findReportedCheerTalksByAdminId(
				admin.getId(), pageRequest.cursor(), pageRequest.size()
		);

		return toManagerResponses(reportedCheerTalks);
	}

	public List<CheerTalkResponse.ForManager> getUnblockedCheerTalksByAdmin(final PageRequestDto pageRequest, final Member admin) {
		List<CheerTalk> cheerTalks = cheerTalkDynamicRepository.findUnblockedCheerTalksByAdminId(
				admin.getId(), pageRequest.cursor(), pageRequest.size()
		);

		return toManagerResponses(cheerTalks);
	}

	public List<CheerTalkResponse.ForManager> getBlockedCheerTalksByAdmin(final PageRequestDto pageRequest, final Member admin) {
		List<CheerTalk> blockedCheerTalks = cheerTalkDynamicRepository.findBlockedCheerTalksByAdminId(
				admin.getId(), pageRequest.cursor(), pageRequest.size()
		);

		return toManagerResponses(blockedCheerTalks);
	}

	private List<CheerTalkResponse.ForManager> toManagerResponses(List<CheerTalk> cheerTalks) {
		return cheerTalks.stream()
				.map(this::toManagerResponse)
				.filter(Objects::nonNull)
				.toList();
	}

	private CheerTalkResponse.ForManager toManagerResponse(CheerTalk cheerTalk) {
		Game game = gameQueryRepository.findByGameTeamIdWithLeague(cheerTalk.getGameTeamId());
		if (game == null) {
			return null;
		}
		return new CheerTalkResponse.ForManager(cheerTalk, game);
	}
}
