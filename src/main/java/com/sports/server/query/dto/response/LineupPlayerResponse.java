package com.sports.server.query.dto.response;

import com.sports.server.command.game.domain.GameTeam;
import com.sports.server.command.game.domain.LineupPlayer;
import com.sports.server.command.game.domain.LineupPlayerState;
import com.sports.server.command.team.domain.Position;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class LineupPlayerResponse {

	public record Playing(
			Long gameTeamId,
			String teamName,
			String teamColor,
			List<PlayerResponse> gameTeamPlayers
	) {
		public Playing(GameTeam gameTeam, List<LineupPlayer> lineupPlayers) {
			this(
					gameTeam.getId(),
					gameTeam.getTeam().getName(),
					gameTeam.getTeam().getTeamColor(),
					lineupPlayers.stream()
							// 매니저가 기록을 남길 때 쓰는 출전 선수 목록이라 관객용 게이트를 적용하지 않는다
							.map(lineupPlayer -> new PlayerResponse(lineupPlayer, true))
							.toList()
			);
		}
	}

	public record All(
			Long gameTeamId,
			String teamName,
			List<PlayerResponse> starterPlayers,
			List<PlayerResponse> candidatePlayers
	) {
		public All(GameTeam gameTeam, List<LineupPlayer> lineupPlayers) {
			this(gameTeam, lineupPlayers, showsPosition(lineupPlayers));
		}

		private All(GameTeam gameTeam, List<LineupPlayer> lineupPlayers, boolean showsPosition) {
			this(
					gameTeam.getId(),
					gameTeam.getTeam().getName(),
					lineupPlayers.stream()
							.filter(LineupPlayer::isPlaying)
							.sorted(order(showsPosition))
							.map(lineupPlayer -> new PlayerResponse(lineupPlayer, showsPosition))
							.toList(),
					lineupPlayers.stream()
							.filter(lineupPlayer -> !lineupPlayer.isPlaying())
							.map(lineupPlayer -> new PlayerResponse(lineupPlayer, showsPosition))
							.toList()
			);
		}

		/**
		 * 선발 전원의 포지션이 등록된 경우에만 포지션을 노출한다. 일부만 등록된 불완전한 라인업이
		 * 관객에게 보이지 않도록 하는 기획상의 게이트다.
		 *
		 * <p>판정은 출전 여부(isPlaying)가 아니라 선발 여부(state)를 기준으로 한다. 출전 여부로 보면
		 * 포지션이 없는 후보가 교체 투입되는 순간 경기 도중에 포지션 표시가 사라진다.
		 */
		private static boolean showsPosition(List<LineupPlayer> lineupPlayers) {
			List<LineupPlayer> starters = lineupPlayers.stream()
					.filter(lineupPlayer -> lineupPlayer.getState() == LineupPlayerState.STARTER)
					.toList();
			return !starters.isEmpty()
					&& starters.stream().allMatch(lineupPlayer -> lineupPlayer.getPosition() != null);
		}

		/**
		 * 포지션을 노출할 때만 포지션 순(축구 FW→MF→DF→GK, 농구 PG→SG→SF→PF→C)으로 정렬하고,
		 * 그렇지 않으면 등번호 순으로 정렬한다.
		 *
		 * <p>포지션을 노출하는 경우에도 포지션이 없는 선수가 섞일 수 있다. 게이트는 선발(state) 기준인데
		 * 이 목록은 출전 여부(isPlaying) 기준이라, 포지션 없는 후보가 교체 투입되면 여기 들어온다.
		 * 그런 선수는 뒤로 보낸다.
		 */
		private static Comparator<LineupPlayer> order(boolean showsPosition) {
			if (!showsPosition) {
				return Comparator.comparing(LineupPlayer::getJerseyNumber,
						Comparator.nullsLast(Comparator.naturalOrder()));
			}
			return Comparator.comparingInt(lineupPlayer -> lineupPlayer.getPosition() == null
					? Integer.MAX_VALUE
					: lineupPlayer.getPosition().getDisplayOrder());
		}
	}

	public record PlayerResponse(
			Long lineupPlayerId,
			Long playerId,
			String playerName,
			Integer jerseyNumber,
			Position position,
			boolean isCaptain,
			LineupPlayerState state,
			boolean isReplaced,
			PlayerSummary replacedPlayer
	) {
		public PlayerResponse(LineupPlayer lineupPlayer, boolean showsPosition) {
			this(
					lineupPlayer.getId(),
					lineupPlayer.getPlayer().getId(),
					lineupPlayer.getPlayer().getName(),
					lineupPlayer.getJerseyNumber(),
					showsPosition ? lineupPlayer.getPosition() : null,
					lineupPlayer.isCaptain(),
					lineupPlayer.getState(),
					lineupPlayer.isReplaced(),
					Optional.ofNullable(lineupPlayer.getReplacedPlayer())
							.map(PlayerSummary::new)
							.orElse(null));
		}
	}

	public record PlayerSummary(
			Long id,
			String playerName,
			int number
	) {
		public PlayerSummary(LineupPlayer lineupPlayer) {
			this(
					lineupPlayer.getId(),
					lineupPlayer.getPlayer().getName(),
					lineupPlayer.getJerseyNumber());
		}
	}
}
