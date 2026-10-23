package com.sports.server.command.game.domain;

import com.sports.server.command.game.exception.GameErrorMessages;
import com.sports.server.command.league.domain.SportType;
import com.sports.server.command.team.domain.Team;
import com.sports.server.common.domain.BaseEntity;
import com.sports.server.common.exception.BadRequestException;
import com.sports.server.common.exception.ExceptionMessages;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "game_teams")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GameTeam extends BaseEntity<GameTeam> {

    private static final int MAXIMUM_OF_CHEER_COUNT = 500;
    private static final int MAXIMUM_OF_TOTAL_CHEER_COUNT = 100_000_000;
    private static final int MINIMUM_OF_CHEER_COUNT = 0;
    private static final int PK_SCORE_VALUE = 1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id")
    private Game game;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    @OneToMany(mappedBy = "gameTeam", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LineupPlayer> lineupPlayers = new ArrayList<>();

    @Column(name = "cheer_count", nullable = false)
    private int cheerCount;

    @Column(name = "score", nullable = false)
    private int score;

    @Column(name = "pk_score", nullable = false)
    private int pkScore;

    @Column(name = "result")
    @Enumerated(EnumType.STRING)
    private GameResult result;

    public void validateCheerCountOfGameTeam(final int cheerCount) {
        if (cheerCount >= MAXIMUM_OF_CHEER_COUNT || cheerCount <= MINIMUM_OF_CHEER_COUNT) {
            throw new BadRequestException(ExceptionMessages.GAME_TEAM_INVALID_CHEER_COUNT_RANGE);
        }
        if (this.cheerCount + cheerCount > MAXIMUM_OF_TOTAL_CHEER_COUNT) {
            throw new BadRequestException(ExceptionMessages.GAME_TEAM_CHEER_COUNT_LIMIT_EXCEEDED);
        }
    }

    public boolean matchGame(final Game game) {
        return this.game.equals(game);
    }

    public SportType getSportType() {
        return game.getSportType();
    }

    public boolean hasTeam() {
        return team != null;
    }

    public Long getTeamId() {
        return team.getId();
    }

    public String getTeamName() {
        return team.getName();
    }

    public void registerStarter(final LineupPlayer lineupPlayer) {
        // 이미 선발이면 기존 "이미 선발" 예외가 나야 하므로 후보일 때만 정원을 본다
        if (lineupPlayer.getState() == LineupPlayerState.CANDIDATE) {
            validateStarterCapacity();
        }
        lineupPlayer.changeStateToStarter();
    }

    public void rollbackToCandidate(final LineupPlayer lineupPlayer) {
        lineupPlayer.changeStateToCandidate();
    }

    public void score(int value) {
        this.score += value;
    }

    public void scoreInPk() {
        this.pkScore += PK_SCORE_VALUE;
    }

    public void cancelScore(int value) {
        this.score = Math.max(0, this.score - value);
    }

    public void cancelPkScore() {
        if (this.pkScore > 0) {
            this.pkScore -= PK_SCORE_VALUE;
        }
    }

    private GameTeam(Game game, Team team) {
        this.game = game;
        this.team = team;
        this.cheerCount = 0;
        this.score = 0;
    }

    public static GameTeam of(Game game, Team team) {
        return new GameTeam(game, team);
    }

    public void addLineupPlayer(final LineupPlayer lineupPlayer) {
        if (!this.lineupPlayers.contains(lineupPlayer)) {
            this.lineupPlayers.add(lineupPlayer);
        }
    }

    // 선발이 늘어나는 쓰기에서만 부른다. 이미 정원을 넘긴 팀의 조회, 후보 전환, 삭제는 막지 않는다
    public void validateStarterCapacity() {
        int limit = getSportType().getStarterLimit();
        long starterCount = this.lineupPlayers.stream()
                .filter(lp -> lp.getState() == LineupPlayerState.STARTER)
                .count();
        if (starterCount >= limit) {
            throw new BadRequestException(String.format(GameErrorMessages.STARTER_LIMIT_EXCEEDED, limit));
        }
    }

    public boolean hasCaptain() {
        return this.lineupPlayers.stream().anyMatch(LineupPlayer::isCaptain);
    }

    public void removeLineupPlayer(LineupPlayer lineupPlayer) {
        this.lineupPlayers.remove(lineupPlayer);
    }

    public void changePlayerToCaptain(final LineupPlayer lineupPlayer) {
        validateLineupPlayer(lineupPlayer);
        lineupPlayers.stream()
                .filter(lp -> lp.isCaptain() && !lp.equals(lineupPlayer))
                .forEach(LineupPlayer::revokeCaptainFromPlayer);
        if (!lineupPlayer.isCaptain()) {
            lineupPlayer.changePlayerToCaptain();
        }
    }

    public void revokeCaptainFromPlayer(final LineupPlayer lineupPlayer) {
        validateLineupPlayer(lineupPlayer);
        lineupPlayer.revokeCaptainFromPlayer();
    }

    private void validateLineupPlayer(final LineupPlayer lineupPlayer) {
        boolean exists = this.lineupPlayers.stream()
                .anyMatch(lp -> lp.equals(lineupPlayer));

        if (!exists) {
            throw new BadRequestException(ExceptionMessages.GAME_TEAM_PLAYER_NOT_IN_TEAM);
        }
    }

    public void markAsWinner() {
        this.result = GameResult.WIN;
    }

    public void markAsLoser() {
        this.result = GameResult.LOSE;
    }

    public void markAsDraw() {
        this.result = GameResult.DRAW;
    }

    public void clearResult() {
        this.result = null;
    }
}