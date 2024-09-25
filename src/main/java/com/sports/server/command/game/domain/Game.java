package com.sports.server.command.game.domain;

import com.sports.server.command.game.dto.GameRequestDto;
import com.sports.server.command.league.domain.League;
import com.sports.server.command.league.domain.Round;
import com.sports.server.command.member.domain.Member;
import com.sports.server.command.sport.domain.Sport;
import com.sports.server.common.domain.BaseEntity;
import com.sports.server.common.domain.ManagedEntity;
import com.sports.server.common.exception.CustomException;
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
import org.springframework.util.StringUtils;
import org.springframework.http.HttpStatus;

@Entity
@Getter
@Table(name = "games")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Game extends BaseEntity<Game> implements ManagedEntity {

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

    public void cancelScore(LineupPlayer scorer) {
        GameTeam scoredTeam = teams.stream()
                .filter(scorer::isInTeam)
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("참여하지 않는 선수는 득점을 취소할 수 없습니다."));

        scoredTeam.cancelScore();
    }

    public void updateName(String name) {
        if (StringUtils.hasText(name)) {
            this.name = name;
        }
    }

    public void updateStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public void updateVideoId(String videoId) {
        if (StringUtils.hasText(videoId)) {
            this.videoId = videoId;
        }
    }

    public void updateGameQuarter(String gameQuarter) {
        if (StringUtils.hasText(gameQuarter)) {
            this.gameQuarter = gameQuarter;
        }
    }

    public void updateState(GameState state) {
        this.state = state;
    }

    public void updateRound(Round round) {
        this.round = round;
    }


    public Game(Sport sport, Member manager, League league, String name, LocalDateTime startTime,
                String videoId, String gameQuarter, GameState state, Round round) {
        this.sport = sport;
        this.manager = manager;
        this.league = league;
        this.name = name;
        this.startTime = startTime;
        this.videoId = videoId;
        this.gameQuarter = gameQuarter;
        this.state = state;
        this.round = round;
    }

    public void changePlayerToCaptain(final GameTeam gameTeam, final LineupPlayer lineupPlayer) {
        validateGameTeam(gameTeam);
        gameTeam.changePlayerToCaptain(lineupPlayer);
    }

    public void revokeCaptainFromPlayer(final GameTeam gameTeam, final LineupPlayer lineupPlayer) {
        validateGameTeam(gameTeam);
        gameTeam.revokeCaptainFromPlayer(lineupPlayer);
    }

    private void validateGameTeam(final GameTeam gameTeam) {
        if (!teams.contains(gameTeam)) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "해당 게임팀은 이 게임에 포함되지 않습니다.");
        }
    }

    @Override
    public boolean isManagedBy(Member manager) {
        return manager.equals(manager);
    }
}
