package com.sports.server.command.game.domain;

import com.sports.server.command.leagueteam.LeagueTeam;
import com.sports.server.common.domain.BaseEntity;
import com.sports.server.common.exception.CustomException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id")
    private Game game;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "league_team_id")
    private LeagueTeam leagueTeam;

    @Column(name = "cheer_count", nullable = false)
    private int cheerCount;

    @Column(name = "score", nullable = false)
    private int score;

    public void validateCheerCountOfGameTeam(final int cheerCount) {
        if (cheerCount >= MAXIMUM_OF_CHEER_COUNT) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "응원 횟수가 한계에 도달했습니다.");
        }
        if (this.cheerCount + cheerCount > MAXIMUM_OF_TOTAL_CHEER_COUNT) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "총 응원 횟수가 한계에 도달했습니다.");
        }
    }

    public boolean matchGame(final Game game) {
        return this.game.equals(game);
    }

}



