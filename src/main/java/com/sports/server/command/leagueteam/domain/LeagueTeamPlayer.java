package com.sports.server.command.leagueteam.domain;

import com.sports.server.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "league_team_players")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LeagueTeamPlayer extends BaseEntity<LeagueTeamPlayer> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "league_team_id", nullable = false)
    private LeagueTeam leagueTeam;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "number", nullable = true)
    private int number;

    // TODO: 데이터베이스 정리 이후 중복 검사 로직 추가
    @Column(name = "student_number", nullable = true)
    private String studentNumber;

    public LeagueTeamPlayer(LeagueTeam leagueTeam, String name, int number, String studentNumber) {
        validateStudentNumber(studentNumber);
        this.leagueTeam = leagueTeam;
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
        validateStudentNumber(studentNumber);
        this.studentNumber = studentNumber;
    }
}
