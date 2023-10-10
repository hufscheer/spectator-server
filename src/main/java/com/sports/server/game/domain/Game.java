package com.sports.server.game.domain;

import com.sports.server.member.domain.Member;
import com.sports.server.record.domain.Record;
import com.sports.server.team.domain.Team;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "sports_name", nullable = false)
    private String sportsName;

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

    @Enumerated(value = EnumType.STRING)
    @Column(name = "game_status", nullable = false)
    private GameStatus gameStatus;

    @Column(name = "status_changed_at", nullable = false)
    private LocalDateTime statusChangedAt;

    @Column(name = "video_url", nullable = true)
    private String videoUrl;

    @OneToMany(mappedBy = "game")
    private List<Record> records = new ArrayList<>();

}