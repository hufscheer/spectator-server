package com.sports.server.command.leagueteam.domain;

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

    @Column(name = "description")
    private String description;

    @Column(name = "number", nullable = true)
    private int number;

    // TODO: 데이터베이스 정리 이후 중복 검사 로직 추가
    @Column(name = "student_number", nullable = true)
    private String studentNumber;
    
    @OneToMany(mappedBy = "player", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TeamPlayer> teamPlayers = new ArrayList<>();

    public Player(String name, int number, String studentNumber) {
        validateStudentNumber(studentNumber);
        this.name = name;
        this.number = number;
        this.studentNumber = studentNumber;
    }

    private void validateStudentNumber(String studentNumber) {
        if (studentNumber != null && !studentNumber.matches("^[0-9]{9}$")) {
            throw new IllegalArgumentException("학생번호는 9자리 숫자여야 합니다.");
        }
    }

    public void update(String name, int number, String studentNumber) {
        this.name = name;
        this.number = number;

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
    
    private TeamPlayer findTeamPlayer(Team team) {
        return this.teamPlayers.stream()
                .filter(tp -> tp.getTeam().equals(team))
                .findFirst()
                .orElse(null);
    }
}