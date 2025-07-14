package com.sports.server.command.newEntity.player.domain;

import com.sports.server.command.newEntity.league.domain.LeagueTopScorer;
import com.sports.server.command.newEntity.team.domain.TeamPlayer;
import com.sports.server.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(name = "players")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Player extends BaseEntity<Player> {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "student_number")
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
        
        if (studentNumber != null) {
            validateStudentNumber(studentNumber);
            this.studentNumber = studentNumber;
        }
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
}