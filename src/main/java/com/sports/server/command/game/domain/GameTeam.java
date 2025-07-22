package com.sports.server.command.game.domain;

import com.sports.server.command.team.domain.Player;
import com.sports.server.command.team.domain.Team;
import com.sports.server.common.domain.BaseEntity;
import com.sports.server.common.exception.CustomException;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Entity
@Getter
@Table(name = "game_teams")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GameTeam extends BaseEntity<GameTeam> {

    private static final int MAXIMUM_OF_CHEER_COUNT = 500;
    private static final int MAXIMUM_OF_TOTAL_CHEER_COUNT = 100_000_000;
    private static final int MINIMUM_OF_CHEER_COUNT = 0;
    private static final int SCORE_VALUE = 1;
    private static final int PK_SCORE_VALUE = 1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id")
    private Game game;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "league_team_id")
    private Team team;

    @OneToMany(mappedBy = "gameTeam", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LineupPlayer> lineupPlayers = new ArrayList<>();

    @Column(name = "cheer_count", nullable = false)
    private int cheerCount;

    @Column(name = "score", nullable = false)
    private int score;

    @Column(name = "pk_score", nullable = false)
    private int pkScore;

    public void validateCheerCountOfGameTeam(final int cheerCount) {
        if (cheerCount >= MAXIMUM_OF_CHEER_COUNT || cheerCount <= MINIMUM_OF_CHEER_COUNT) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "잘못된 범위의 응원 요청 횟수입니다.");
        }
        if (this.cheerCount + cheerCount > MAXIMUM_OF_TOTAL_CHEER_COUNT) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "총 응원 횟수가 한계에 도달했습니다.");
        }
    }

    public boolean matchGame(final Game game) {
        return this.game.equals(game);
    }

    public void registerStarter(final LineupPlayer lineupPlayer) {
        lineupPlayer.changeStateToStarter();
    }

    public void rollbackToCandidate(final LineupPlayer lineupPlayer) {
        lineupPlayer.changeStateToCandidate();
    }

    public void score() {
        this.score += SCORE_VALUE;
    }

    public void scoreInPk() {
        this.pkScore += PK_SCORE_VALUE;
    }

    public void cancelScore() {
        if (this.score > 0) {
            this.score -= SCORE_VALUE;
        }
    }

    public void cancelPkScore() {
        if (this.pkScore > 0) {
            this.pkScore -= PK_SCORE_VALUE;
        }
    }

    public GameTeam(Game game, Team team) {
        this.game = game;
        this.team = team;
        this.cheerCount = 0;
        this.score = 0;
    }

    public void registerLineup(Player player) {
        LineupPlayer lineupPlayer = new LineupPlayer(
                this,
                player.getId(),
                player.getName(),
                player.getNumber(),
                false,
                LineupPlayerState.CANDIDATE);

        this.lineupPlayers.add(lineupPlayer);
    }

    public void changePlayerToCaptain(final LineupPlayer lineupPlayer) {
        validateLineupPlayer(lineupPlayer);
        isCaptainExists(lineupPlayer);
        lineupPlayer.changePlayerToCaptain();
    }

    public void revokeCaptainFromPlayer(final LineupPlayer lineupPlayer) {
        validateLineupPlayer(lineupPlayer);
        lineupPlayer.revokeCaptainFromPlayer();
    }

    private void validateLineupPlayer(final LineupPlayer lineupPlayer) {
        boolean exists = this.lineupPlayers.stream()
                .anyMatch(lp -> lp.equals(lineupPlayer));

        if (!exists) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "해당 게임팀에 속하지 않는 선수입니다.");
        }
    }

    private void isCaptainExists(final LineupPlayer lineupPlayer) {
        boolean captainExists = lineupPlayers.stream()
                .anyMatch(lp -> lp.isCaptain() && !lp.equals(lineupPlayer));

        if (captainExists) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "이미 등록된 주장이 존재합니다.");
        }
    }


}