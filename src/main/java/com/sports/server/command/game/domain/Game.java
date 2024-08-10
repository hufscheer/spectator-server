package com.sports.server.command.game.domain;

import com.sports.server.command.league.domain.League;
import com.sports.server.command.league.domain.Round;
import com.sports.server.command.member.domain.Member;
import com.sports.server.command.sport.domain.Sport;
import com.sports.server.common.domain.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "games")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Game extends BaseEntity<Game> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sport_id")
    private Sport sport;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    private Member manager;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "league_id")
    private League league;

    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GameTeam> teams = new ArrayList<>();

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "video_id")
    private String videoId;

    @Column(name = "quarter_changed_at")
    private LocalDateTime quarterChangedAt;

    @Column(name = "game_quarter", nullable = false)
    private String gameQuarter;

    @Column(name = "state", nullable = false)
    @Enumerated(EnumType.STRING)
    private GameState state;

    @Column(name = "round", nullable = false)
    private Round round;

    public void registerStarter(final LineupPlayer lineupPlayer) {
        this.teams.forEach(gameTeam -> gameTeam.registerStarter(lineupPlayer));
    }

    public void rollbackToCandidate(final LineupPlayer lineupPlayer) {
        this.teams.forEach(gameTeam -> gameTeam.rollbackToCandidate(lineupPlayer));
    }

    public GameTeam getTeam1() {
        return teams.stream()
                .min(Comparator.comparing(GameTeam::getId))
                .orElseThrow();
    }

    public GameTeam getTeam2() {
        return teams.stream()
                .max(Comparator.comparing(GameTeam::getId))
                .orElseThrow();
    }

    public void addTeam(GameTeam team) {
        teams.add(team);
    }

    public void score(LineupPlayer scorer) {
        GameTeam scoredTeam = teams.stream()
                .filter(scorer::isInTeam)
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("참여하지 않는 선수는 득점할 수 없습니다."));

        scoredTeam.score();
    }

    public boolean isMangedBy(Member member) {
        return manager.equals(member);
    }

    public void cancelScore(LineupPlayer scorer) {
        GameTeam scoredTeam = teams.stream()
                .filter(scorer::isInTeam)
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("참여하지 않는 선수는 득점을 취소할 수 없습니다."));

        scoredTeam.cancelScore();
    }
}
