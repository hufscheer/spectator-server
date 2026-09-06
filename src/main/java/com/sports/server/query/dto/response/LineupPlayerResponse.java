package com.sports.server.query.dto.response;

import com.sports.server.command.game.domain.GameTeam;
import com.sports.server.command.game.domain.LineupPlayer;
import com.sports.server.command.game.domain.LineupPlayerState;
import com.sports.server.command.game.domain.Position;

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
							.map(lineupPlayer -> new PlayerResponse(lineupPlayer, PositionDisplayLevel.DETAIL))
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
			this(gameTeam, lineupPlayers, resolveDisplayLevel(lineupPlayers));
		}

		private All(GameTeam gameTeam, List<LineupPlayer> lineupPlayers, PositionDisplayLevel displayLevel) {
			this(
					gameTeam.getId(),
					gameTeam.getTeam().getName(),
					lineupPlayers.stream()
							.filter(LineupPlayer::isPlaying)
							.sorted(order(displayLevel))
							.map(lineupPlayer -> new PlayerResponse(lineupPlayer, displayLevel))
							.toList(),
					lineupPlayers.stream()
							.filter(lineupPlayer -> !lineupPlayer.isPlaying())
							// 후보는 화면에 포지션을 노출하지 않는다 (등번호·이름·교체 표시만)
							.map(lineupPlayer -> new PlayerResponse(lineupPlayer, PositionDisplayLevel.HIDDEN))
							.toList()
			);
		}

		/**
		 * 팀의 선발 라인업을 기준으로 포지션 표시 수준을 정한다. 선수마다 상세도가 달라 보이지 않게 하려는
		 * 기획상의 게이트라, 판정은 선수 개별이 아니라 팀 단위로 한 번만 한다.
		 *
		 * <p>판정은 출전 여부(isPlaying)가 아니라 선발 여부(state)를 기준으로 한다. 출전 여부로 보면
		 * 포지션이 없는 후보가 교체 투입되는 순간 경기 도중에 포지션 표시가 사라진다.
		 */
		private static PositionDisplayLevel resolveDisplayLevel(List<LineupPlayer> lineupPlayers) {
			List<LineupPlayer> starters = lineupPlayers.stream()
					.filter(lineupPlayer -> lineupPlayer.getState() == LineupPlayerState.STARTER)
					.toList();
			if (hasNoRegisteredStarter(starters)) {
				return PositionDisplayLevel.HIDDEN;
			}
			if (hasCategoryOnlyStarter(starters)) {
				return PositionDisplayLevel.CATEGORY;
			}
			return PositionDisplayLevel.DETAIL;
		}

		private static boolean hasNoRegisteredStarter(List<LineupPlayer> starters) {
			return starters.isEmpty()
					|| starters.stream().anyMatch(starter -> starter.getPosition() == null);
		}

		private static boolean hasCategoryOnlyStarter(List<LineupPlayer> starters) {
			return starters.stream().anyMatch(starter -> starter.getPosition().isCategoryOnly());
		}

		/**
		 * 포지션을 노출할 때만 포지션 순으로, 그렇지 않으면 등번호 순으로 정렬한다. 순서 자체는
		 * {@link Position#DISPLAY_ORDER} 가 정하고 여기서는 어느 기준으로 정렬할지만 고른다.
		 *
		 * <p>포지션을 노출하는 경우에도 포지션이 없는 선수가 섞일 수 있다. 게이트는 선발(state) 기준인데
		 * 이 목록은 출전 여부(isPlaying) 기준이라, 포지션 없는 후보가 교체 투입되면 여기 들어온다.
		 */
		private static Comparator<LineupPlayer> order(PositionDisplayLevel displayLevel) {
			if (displayLevel == PositionDisplayLevel.HIDDEN) {
				return Comparator.comparing(LineupPlayer::getJerseyNumber,
						Comparator.nullsLast(Comparator.naturalOrder()));
			}
			return Comparator.comparing(
					lineupPlayer -> displayLevel.apply(lineupPlayer.getPosition()),
					Position.DISPLAY_ORDER);
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
		public PlayerResponse(LineupPlayer lineupPlayer, PositionDisplayLevel displayLevel) {
			this(
					lineupPlayer.getId(),
					lineupPlayer.getPlayer().getId(),
					lineupPlayer.getPlayer().getName(),
					lineupPlayer.getJerseyNumber(),
					displayLevel.apply(lineupPlayer.getPosition()),
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

	/**
	 * 관객 라인업에 포지션을 어느 상세도로 내보낼지. 팀 단위로 한 번 정해 그 팀의 모든 선수에 같게 적용한다.
	 */
	public enum PositionDisplayLevel {
		HIDDEN,
		CATEGORY,
		DETAIL;

		private Position apply(Position position) {
			if (this == HIDDEN || position == null) {
				return null;
			}
			return this == CATEGORY ? position.category() : position;
		}
	}
}
