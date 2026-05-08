package com.sports.server.query.application;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sports.server.command.cheertalk.domain.CheerTalk;
import com.sports.server.command.game.domain.Game;
import com.sports.server.command.league.domain.League;
import com.sports.server.command.member.domain.Member;
import com.sports.server.common.application.PermissionValidator;
import com.sports.server.common.dto.CursorPageResponse;
import com.sports.server.common.dto.PageRequestDto;
import com.sports.server.common.exception.NotFoundException;
import com.sports.server.query.dto.GameTeamGameInfoDto;
import com.sports.server.query.dto.response.CheerTalkResponse;
import com.sports.server.query.repository.CheerTalkDynamicRepository;
import com.sports.server.query.repository.GameQueryRepository;
import com.sports.server.query.repository.LeagueQueryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CheerTalkQueryService {

	private final CheerTalkDynamicRepository cheerTalkDynamicRepository;
	private final GameQueryRepository gameQueryRepository;
	private final LeagueQueryRepository leagueQueryRepository;

	public CursorPageResponse<CheerTalkResponse.ForSpectator> getCheerTalksByGameId(final Long gameId, final PageRequestDto pageRequest) {
		Game game = getGame(gameId);
		List<CheerTalk> cheerTalks = cheerTalkDynamicRepository.findByGameIdOrderByStartTime(
			gameId, pageRequest.cursor(), pageRequest.size()
		);

		boolean hasNext = cheerTalks.size() > pageRequest.size();
		List<CheerTalk> sliced = hasNext ? cheerTalks.subList(0, pageRequest.size()) : cheerTalks;
		Long nextCursor = hasNext ? sliced.get(sliced.size() - 1).getId() : null;

		List<CheerTalkResponse.ForSpectator> responses = new ArrayList<>(sliced.stream()
			.map(cheerTalk -> new CheerTalkResponse.ForSpectator(cheerTalk, game))
			.toList());
		Collections.reverse(responses);

		return new CursorPageResponse<>(responses, nextCursor, hasNext);
	}

	public CursorPageResponse<CheerTalkResponse.ForManager> getReportedCheerTalksByAdmin(final PageRequestDto pageRequest, final Member admin) {
		List<CheerTalk> cheerTalks = cheerTalkDynamicRepository.findReportedCheerTalksByAdminId(
				admin.getId(), pageRequest.cursor(), pageRequest.size()
		);
		return toForManagerPageResponse(cheerTalks, pageRequest.size());
	}

	public CursorPageResponse<CheerTalkResponse.ForManager> getUnblockedCheerTalksByAdmin(final PageRequestDto pageRequest, final Member admin) {
		List<CheerTalk> cheerTalks = cheerTalkDynamicRepository.findUnblockedCheerTalksByAdminId(
				admin.getId(), pageRequest.cursor(), pageRequest.size()
		);
		return toForManagerPageResponse(cheerTalks, pageRequest.size());
	}

	public CursorPageResponse<CheerTalkResponse.ForManager> getBlockedCheerTalksByAdmin(final PageRequestDto pageRequest, final Member admin) {
		List<CheerTalk> cheerTalks = cheerTalkDynamicRepository.findBlockedCheerTalksByAdminId(
				admin.getId(), pageRequest.cursor(), pageRequest.size()
		);
		return toForManagerPageResponse(cheerTalks, pageRequest.size());
	}

	public CursorPageResponse<CheerTalkResponse.ForManager> getReportedCheerTalksByLeagueId(final Long leagueId, final PageRequestDto pageRequest, final Member member) {
		League league = getLeague(leagueId);
		PermissionValidator.checkPermission(league, member);
		List<CheerTalk> cheerTalks = cheerTalkDynamicRepository.findReportedCheerTalksByLeagueId(
				leagueId, pageRequest.cursor(), pageRequest.size()
		);
		return toForManagerPageResponse(cheerTalks, pageRequest.size());
	}

	public CursorPageResponse<CheerTalkResponse.ForManager> getUnblockedCheerTalksByLeagueId(final Long leagueId, final PageRequestDto pageRequest, final Member member) {
		League league = getLeague(leagueId);
		PermissionValidator.checkPermission(league, member);
		List<CheerTalk> cheerTalks = cheerTalkDynamicRepository.findUnblockedCheerTalksByLeagueId(
				leagueId, pageRequest.cursor(), pageRequest.size()
		);
		return toForManagerPageResponse(cheerTalks, pageRequest.size());
	}

	public CursorPageResponse<CheerTalkResponse.ForManager> getBlockedCheerTalksByLeagueId(final Long leagueId, final PageRequestDto pageRequest, final Member member) {
		League league = getLeague(leagueId);
		PermissionValidator.checkPermission(league, member);
		List<CheerTalk> cheerTalks = cheerTalkDynamicRepository.findBlockedCheerTalksByLeagueId(
				leagueId, pageRequest.cursor(), pageRequest.size()
		);
		return toForManagerPageResponse(cheerTalks, pageRequest.size());
	}

	public CursorPageResponse<CheerTalkResponse.ForManager> getReportedCheerTalksByGameId(final Long gameId, final PageRequestDto pageRequest, final Member member) {
		Game game = getGame(gameId);
		PermissionValidator.checkPermission(game, member);
		List<CheerTalk> cheerTalks = cheerTalkDynamicRepository.findReportedCheerTalksByGameId(
				gameId, pageRequest.cursor(), pageRequest.size()
		);
		return toForManagerPageResponse(cheerTalks, pageRequest.size());
	}

	public CursorPageResponse<CheerTalkResponse.ForManager> getBlockedCheerTalksByGameId(final Long gameId, final PageRequestDto pageRequest, final Member member) {
		Game game = getGame(gameId);
		PermissionValidator.checkPermission(game, member);
		List<CheerTalk> cheerTalks = cheerTalkDynamicRepository.findBlockedCheerTalksByGameId(
				gameId, pageRequest.cursor(), pageRequest.size()
		);
		return toForManagerPageResponse(cheerTalks, pageRequest.size());
	}

	private League getLeague(Long leagueId) {
		return leagueQueryRepository.findById(leagueId)
				.orElseThrow(() -> new NotFoundException("존재하지 않는 리그입니다."));
	}

	private Game getGame(Long gameId) {
		return gameQueryRepository.findById(gameId)
				.orElseThrow(() -> new NotFoundException("존재하지 않는 경기입니다."));
	}

	private CursorPageResponse<CheerTalkResponse.ForManager> toForManagerPageResponse(List<CheerTalk> cheerTalks, int size) {
		boolean hasNext = cheerTalks.size() > size;
		List<CheerTalk> sliced = hasNext ? cheerTalks.subList(0, size) : cheerTalks;
		Long nextCursor = hasNext ? sliced.get(sliced.size() - 1).getId() : null;

		if (sliced.isEmpty()) {
			return new CursorPageResponse<>(Collections.emptyList(), null, false);
		}

		List<Long> gameTeamIds = sliced.stream()
				.map(CheerTalk::getGameTeamId)
				.distinct()
				.toList();

		Map<Long, GameTeamGameInfoDto> gameInfoMap = gameQueryRepository.findGameInfoByGameTeamIds(gameTeamIds)
				.stream()
				.collect(Collectors.toMap(GameTeamGameInfoDto::gameTeamId, Function.identity()));

		List<CheerTalkResponse.ForManager> content = sliced.stream()
				.map(ct -> {
					GameTeamGameInfoDto info = gameInfoMap.get(ct.getGameTeamId());
					return new CheerTalkResponse.ForManager(
							ct.getId(), info.gameId(), info.leagueId(), ct.getContent(),
							ct.getGameTeamId(), ct.getCreatedAt(), ct.isBlocked(),
							info.gameName(), info.leagueName()
					);
				})
				.toList();

		return new CursorPageResponse<>(content, nextCursor, hasNext);
	}
}
