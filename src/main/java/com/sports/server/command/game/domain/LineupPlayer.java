package com.sports.server.command.game.domain;

import static com.sports.server.command.game.domain.LineupPlayerState.CANDIDATE;
import static com.sports.server.command.game.domain.LineupPlayerState.STARTER;

import com.sports.server.command.player.domain.Player;
import com.sports.server.common.domain.BaseEntity;
import com.sports.server.common.exception.CustomException;
import jakarta.persistence.*;

import java.util.Objects;

import lombok.*;
import org.springframework.http.HttpStatus;

@Entity
@Getter
@Builder
@Table(name = "lineup_players")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LineupPlayer extends BaseEntity<LineupPlayer> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_team_id")
    private GameTeam gameTeam;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @Column(name = "jersey_number", nullable = true)
    private Integer jerseyNumber;

    @Column(name = "is_captain", nullable = false)
    private boolean isCaptain;

    @Column(name = "state", nullable = false)
    @Enumerated(EnumType.STRING)
    private LineupPlayerState state;

    @Column(name = "is_playing", nullable = false)
    private boolean isPlaying;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "replaced_player_id", nullable = true)
    private LineupPlayer replacedPlayer;

    private LineupPlayer(@NonNull GameTeam gameTeam, @NonNull Player player, @NonNull LineupPlayerState state, Integer jerseyNumber, boolean isCaptain){
        this.gameTeam = gameTeam;
        this.player = player;
        this.state = state;
        this.jerseyNumber = jerseyNumber;
        this.isCaptain = isCaptain;
    }

    public static LineupPlayer of(GameTeam gameTeam, Player player, LineupPlayerState state, Integer jerseyNumber, boolean isCaptain) {
        return new LineupPlayer(gameTeam, player, state, jerseyNumber, isCaptain);
    }

    public boolean isReplaced() {
        return replacedPlayer != null;
    }

    public void recordReplacedPlayer(LineupPlayer replacedPlayer) {
        this.replacedPlayer = replacedPlayer;
    }

    public void deleteReplacedPlayer() {
        this.replacedPlayer = null;
    }

    public void changeStateToStarter() {
        if (this.state == STARTER) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "이미 선발로 등록된 선수입니다.");
        }
        this.state = STARTER;
        activatePlayerInGame();
    }

    public void changeStateToCandidate() {
        if (this.state == CANDIDATE) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "이미 후보로 등록된 선수입니다.");
        }
        this.state = CANDIDATE;
        if (this.isCaptain) {
            this.isCaptain = false;
        }
        deactivatePlayerInGame();
    }

    public boolean isSameTeam(LineupPlayer other) {
        return isInTeam(other.getGameTeam());
    }

    public boolean isInTeam(GameTeam team) {
        return Objects.equals(this.gameTeam, team);
    }

    public void activatePlayerInGame() {
        this.isPlaying = true;
    }

    public void deactivatePlayerInGame() {
        this.isPlaying = false;
    }

    public void changePlayerToCaptain() {
        if (this.isCaptain) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "이미 주장으로 등록된 선수입니다.");
        }
        this.isCaptain = true;
    }

    public void revokeCaptainFromPlayer() {
        if (!this.isCaptain) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "해당 선수는 주장이 아닙니다.");
        }
        this.isCaptain = false;
    }
}
