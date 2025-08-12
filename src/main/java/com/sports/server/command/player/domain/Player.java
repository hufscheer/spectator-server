package com.sports.server.command.player.domain;

import com.sports.server.command.league.domain.League;
import com.sports.server.command.league.domain.LeagueTopScorer;
import com.sports.server.command.team.domain.TeamPlayer;
import com.sports.server.common.domain.BaseEntity;
import com.sports.server.common.exception.CustomException;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Getter
@Table(name = "players")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Player extends BaseEntity<Player> {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "student_number", nullable = true, unique = true)
    private String studentNumber;
    
    @OneToMany(mappedBy = "player", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TeamPlayer> teamPlayers = new ArrayList<>();

    @OneToMany(mappedBy = "player", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LeagueTopScorer> leagueTopScorers = new ArrayList<>();

    @Builder
    public Player(@NonNull String name, @NonNull String studentNumber) {
        validateStudentNumber(studentNumber);
        this.name = name;
        this.studentNumber = studentNumber;
    }

    public void update(String name, String studentNumber) {
        validateStudentNumber(studentNumber);
        this.name = name;
        this.studentNumber = studentNumber;
    }

    public void addTeamPlayer(TeamPlayer teamPlayer) {
        this.teamPlayers.add(teamPlayer);
    }

    public void removeTeamPlayer(TeamPlayer teamPlayer) {
        this.teamPlayers.remove(teamPlayer);
    }

    public void addLeagueTopScorer(LeagueTopScorer leagueTopScorer) {
        this.leagueTopScorers.add(leagueTopScorer);
    }

    public void removeLeagueTopScorer(LeagueTopScorer leagueTopScorer) {
        this.leagueTopScorers.remove(leagueTopScorer);
    }

    private void validateStudentNumber(String studentNumber) {
        if (studentNumber != null && !studentNumber.matches("^[0-9]{9}$")) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "학생번호는 9자리 숫자여야 합니다.");
        }
    }

    public LeagueTopScorer findLeagueTopScorer(League league) {
        return this.leagueTopScorers.stream()
                .filter(lts -> lts.getLeague().equals(league))
                .findFirst()
                .orElse(null);
    }

    public boolean isTopScorerInLeague(League league) {
        return findLeagueTopScorer(league) != null;
    }

    public LeagueTopScorer addAsTopScorerToLeague(League league, Integer ranking, Integer goalCount) {
        LeagueTopScorer leagueTopScorer = new LeagueTopScorer(league, this, ranking, goalCount);
        this.leagueTopScorers.add(leagueTopScorer);
        return leagueTopScorer;
    }

    public void updateTopScorerInfo(League league, Integer ranking, Integer goalCount) {
        LeagueTopScorer leagueTopScorer = findLeagueTopScorer(league);
        if (leagueTopScorer != null) {
            leagueTopScorer.updateRanking(ranking);
            leagueTopScorer.updateGoalCount(goalCount);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Player player)) return false;
        return Objects.equals(getStudentNumber(), player.getStudentNumber());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getStudentNumber());
    }
}