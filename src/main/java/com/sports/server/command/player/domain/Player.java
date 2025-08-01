package com.sports.server.command.player.domain;

import com.sports.server.command.league.domain.League;
import com.sports.server.command.league.domain.LeagueTopScorer;
import com.sports.server.command.team.domain.Team;
import com.sports.server.command.team.domain.TeamPlayer;
import com.sports.server.common.domain.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    @Column(name = "student_number", nullable = true)
    private String studentNumber;
    
    @OneToMany(mappedBy = "player", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TeamPlayer> teamPlayers = new ArrayList<>();

    @OneToMany(mappedBy = "player", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LeagueTopScorer> leagueTopScorers = new ArrayList<>();

    public Player(String name, String studentNumber) {
        validateStudentNumber(studentNumber);
        this.name = name;
        this.studentNumber = studentNumber;
    }

    private void validateStudentNumber(String studentNumber) {
        if (studentNumber != null && !studentNumber.matches("^[0-9]{9}$")) {
            throw new IllegalArgumentException("학생번호는 9자리 숫자여야 합니다.");
        }
    }

    public void update(String name, String studentNumber) {
        this.name = name;
        this.studentNumber = studentNumber;

        if (!Objects.equals(this.studentNumber, studentNumber)) {
            if (studentNumber != null){
                validateStudentNumber(studentNumber);
            }
            this.studentNumber = studentNumber;
        }
    }
    
    public void addTeam(Team team) {
        TeamPlayer teamPlayer = new TeamPlayer(team, this);
        this.teamPlayers.add(teamPlayer);
        team.getTeamPlayers().add(teamPlayer);
    }
    
    public void removeTeam(Team team) {
        TeamPlayer teamPlayer = findTeamPlayer(team);
        if (teamPlayer != null) {
            this.teamPlayers.remove(teamPlayer);
            team.getTeamPlayers().remove(teamPlayer);
        }
    }

    public void addLeagueTopScorer(LeagueTopScorer leagueTopScorer) {
        this.leagueTopScorers.add(leagueTopScorer);
    }

    public void removeLeagueTopScorer(LeagueTopScorer leagueTopScorer) {
        this.leagueTopScorers.remove(leagueTopScorer);
    }

    private TeamPlayer findTeamPlayer(Team team) {
        return this.teamPlayers.stream()
                .filter(tp -> tp.getTeam().equals(team))
                .findFirst()
                .orElse(null);
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
        return leagueTopScorer;
    }

    public void updateTopScorerInfo(League league, Integer ranking, Integer goalCount) {
        LeagueTopScorer leagueTopScorer = findLeagueTopScorer(league);
        if (leagueTopScorer != null) {
            leagueTopScorer.updateRanking(ranking);
            leagueTopScorer.updateGoalCount(goalCount);
        }
    }
}