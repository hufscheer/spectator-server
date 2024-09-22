package com.sports.server.query.dto.response;

import com.sports.server.command.cheertalk.domain.CheerTalk;
import com.sports.server.command.game.domain.Game;
import java.time.LocalDateTime;

public class CheerTalkResponse {
	public record ForSpectator(
		Long cheerTalkId,
		String content,
		Long gameTeamId,
		LocalDateTime createdAt,
		Boolean isBlocked
	) {
		public ForSpectator(CheerTalk cheerTalk) {
			this(
				cheerTalk.getId(),
				checkCheerTalkIsBlocked(cheerTalk),
				cheerTalk.getGameTeamId(),
				cheerTalk.getCreatedAt(),
				cheerTalk.isBlocked()
			);
		}

		private static String checkCheerTalkIsBlocked(CheerTalk cheerTalk) {
			if (cheerTalk.isBlocked()) {
				return null;
			}
			return cheerTalk.getContent();
		}
	}

	public record Reported(
		Long cheerTalkId,
		Long gameId,
		Long leagueId,
		String content,
		Long gameTeamId,
		LocalDateTime createdAt,
		Boolean isBlocked,
		String gameName,
		String leagueName
	) {
		public Reported(CheerTalk cheerTalk, Game game) {
			this(
				cheerTalk.getId(),
				game.getId(),
				game.getLeague().getId(),
				cheerTalk.getContent(),
				cheerTalk.getGameTeamId(),
				cheerTalk.getCreatedAt(),
				cheerTalk.isBlocked(),
				game.getName(),
				game.getLeague().getName()
			);
		}
	}

	public record Blocked(
		Long cheerTalkId,
		Long gameId,
		Long LeagueId,
		String content,
		LocalDateTime createdAt,
		String gameName,
		String leagueName
	) {
		public Blocked(CheerTalk cheerTalk, Game game) {
			this(
				cheerTalk.getId(),
				game.getId(),
				game.getLeague().getId(),
				cheerTalk.getContent(),
				cheerTalk.getCreatedAt(),
				game.getName(),
				game.getLeague().getName());
		}
	}
}

