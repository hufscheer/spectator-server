package com.sports.server.command.game.domain;

import static com.sports.server.command.timeline.exception.TimelineErrorMessage.GAME_ALREADY_FINISHED;

import com.sports.server.command.game.exception.GameErrorMessages;
import com.sports.server.command.league.domain.League;
import com.sports.server.command.league.domain.Round;
import com.sports.server.command.member.domain.Member;
import com.sports.server.command.sport.domain.Quarter;
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
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;

@Entity
@Getter
@Table(name = "games")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Game extends BaseEntity<Game> implements ManagedEntity {

    private static final String NAME_OF_PK_QUARTER = "승부차기";
    private static final String NAME_OF_FIRST_HALF_QUARTER = "전반전";

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

    @Column(name = "is_pk_taken", nullable = false)
    private Boolean isPkTaken;

    public void registerStarter(final LineupPlayer lineupPlayer) {
        GameTeam gameTeam = lineupPlayer.getGameTeam();
        validateGameTeam(gameTeam);
        gameTeam.registerStarter(lineupPlayer);
    }

    public void rollbackToCandidate(final LineupPlayer lineupPlayer) {
        GameTeam gameTeam = lineupPlayer.getGameTeam();
        validateGameTeam(gameTeam);
        gameTeam.rollbackToCandidate(lineupPlayer);
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
        findTeamOf(scorer, GameErrorMessages.PLAYER_NOT_PARTICIPANT_SCORE_EXCEPTION).score();
    }

    public void scoreInPk(LineupPlayer scorer) {
        findTeamOf(scorer, GameErrorMessages.PLAYER_NOT_PARTICIPANT_PK_SCORE_EXCEPTION).scoreInPk();
    }

    public void cancelScore(LineupPlayer scorer) {
        findTeamOf(scorer, GameErrorMessages.PLAYER_NOT_PARTICIPANT_CANCEL_SCORE_EXCEPTION).cancelScore();
    }

    public void cancelPkScore(LineupPlayer scorer) {
        findTeamOf(scorer, GameErrorMessages.PLAYER_NOT_PARTICIPANT_CANCEL_SCORE_EXCEPTION).cancelPkScore();
    }

    public void issueWarningCard(LineupPlayer scorer){
        findTeamOf(scorer, GameErrorMessages.PLAYER_NOT_PARTICIPANT_ISSUE_WARNING_CARD_EXCEPTION);
    }

    public void cancelWarningCard(LineupPlayer scorer){
        findTeamOf(scorer, GameErrorMessages.PLAYER_NOT_PARTICIPANT_CANCEL_WARNING_CARD_EXCEPTION);
    }

    private GameTeam findTeamOf(LineupPlayer scorer, String errorMessage) {
        return teams.stream()
                .filter(scorer::isInTeam)
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException(errorMessage));
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

    public void updateRound(Round round) {
        this.round = round;
    }


    public Game(Sport sport, Member manager, League league, String name, LocalDateTime startTime,
                String videoId, String gameQuarter, GameState state, Round round, boolean isPkTaken) {
        this.sport = sport;
        this.manager = manager;
        this.league = league;
        this.name = name;
        this.startTime = startTime;
        this.videoId = videoId;
        this.gameQuarter = gameQuarter;
        this.state = state;
        this.round = round;
        this.isPkTaken = isPkTaken;
    }

    public void changePlayerToCaptain(final LineupPlayer lineupPlayer) {
        GameTeam gameTeam = lineupPlayer.getGameTeam();
        validateGameTeam(gameTeam);
        gameTeam.changePlayerToCaptain(lineupPlayer);
    }

    public void revokeCaptainFromPlayer(final LineupPlayer lineupPlayer) {
        GameTeam gameTeam = lineupPlayer.getGameTeam();
        validateGameTeam(gameTeam);
        gameTeam.revokeCaptainFromPlayer(lineupPlayer);
    }

    private void validateGameTeam(final GameTeam gameTeam) {
        if (this.teams.stream().noneMatch(team -> team.getId().equals(gameTeam.getId()))) {
            throw new CustomException(HttpStatus.BAD_REQUEST, GameErrorMessages.GAME_TEAM_NOT_PARTICIPANT_EXCEPTION);
        }
    }

    public void updateState(GameState state) {
        this.state = state;
    }

    public void play() {
        this.state = GameState.PLAYING;
        updateQuarter(sport.getAfterStartQuarter());
    }

    public void end() {
        this.state = GameState.FINISHED;
        updateQuarter(sport.getEndQuarter());
    }

    public void updateQuarter(Quarter quarter) {
        this.gameQuarter = quarter.getName();

        if (gameQuarter.equals(NAME_OF_FIRST_HALF_QUARTER)) {
            this.state = GameState.PLAYING;
        }

        if (gameQuarter.equals(NAME_OF_PK_QUARTER)) {
            startPk();
        }

        this.quarterChangedAt = LocalDateTime.now();
    }

    public void updateQuarter(Quarter quarter, LocalDateTime changedAt) {
        if (this.gameQuarter.equals(NAME_OF_PK_QUARTER)) {
            cancelPk();
        }

        this.gameQuarter = quarter.getName();
        this.quarterChangedAt = changedAt;
    }

    private void startPk() {
        this.isPkTaken = true;
    }

    private void cancelPk() {
        this.isPkTaken = false;
    }

    public Quarter getQuarter() {
        return sport.getQuarters()
                .stream()
                .filter(quarter -> quarter.getName().equals(gameQuarter))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(GameErrorMessages.QUARTER_NOT_EXIST_EXCEPTION));
    }

    public void checkStateForTimeline() {
        if (this.getState().equals(GameState.FINISHED)) {
            throw new CustomException(HttpStatus.BAD_REQUEST, GAME_ALREADY_FINISHED);
        }
    }

    @Override
    public boolean isManagedBy(Member manager) {
        return manager.getId() == 1 || this.manager.equals(manager);
    }
}
