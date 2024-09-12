package com.sports.server.command.game.domain;

import static com.sports.server.command.game.domain.LineupPlayerState.CANDIDATE;
import static com.sports.server.command.game.domain.LineupPlayerState.STARTER;

import com.sports.server.common.domain.BaseEntity;
import com.sports.server.common.exception.CustomException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Entity
@Getter
@Table(name = "lineup_players")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LineupPlayer extends BaseEntity<LineupPlayer> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_team_id")
    private GameTeam gameTeam;

    @Column(name = "league_team_player_id", nullable = false)
    private Long leagueTeamPlayerId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", nullable = true)
    private String description;

    @Column(name = "number", nullable = true)
    private int number;

    @Column(name = "is_captain", nullable = false)
    private boolean isCaptain;

    @Column(name = "state", nullable = false)
    @Enumerated(EnumType.STRING)
    private LineupPlayerState state;

    public void changeStateToStarter() {
        if (this.state == STARTER) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "이미 선발로 등록된 선수입니다.");
        }
        this.state = STARTER;
    }

    public void changeStateToCandidate() {
        if (this.state == CANDIDATE) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "이미 후보로 등록된 선수입니다.");
        }
        this.state = CANDIDATE;
    }

    public boolean isSameTeam(LineupPlayer other) {
        return isInTeam(other.getGameTeam());
    }

    public boolean isInTeam(GameTeam team) {
        return Objects.equals(this.gameTeam, team);
    }

    public LineupPlayer(GameTeam gameTeam, Long leagueTeamPlayerId, String name, int number,
                        boolean isCaptain, LineupPlayerState state) {
        this.gameTeam = gameTeam;
        this.leagueTeamPlayerId = leagueTeamPlayerId;
        this.name = name;
        this.number = number;
        this.isCaptain = isCaptain;
        this.state = state;
    }

    public void changePlayerToCaptain() {
        if (this.isCaptain) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "이미 주장으로 등록된 선수입니다.");
        }
        this.isCaptain = true;
    }

    public void revokeCaptainFromPlayer(LineupPlayer lineupPlayer) {
        if (!isCaptain) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "해당 선수는 주장이 아닙니다.");
        }

        this.isCaptain = false;
    }
}
