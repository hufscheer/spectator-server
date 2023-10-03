package com.sports.server.game.domain;

import com.sports.server.member.domain.Member;
import com.sports.server.team.domain.Team;
import jakarta.persistence.*;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne
    @JoinColumn(name = "first_team_id")
    private Team firstTeam;

    @ManyToOne
    @JoinColumn(name = "second_team_id")
    private Team secondTeam;

    @Column(name = "first_team_score", nullable = false)
    private int firstTeamScore;

    @Column(name = "second_team_score", nullable = false)
    private int secondTeamScore;

}