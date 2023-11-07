package com.sports.server.game.domain;

import com.sports.server.league.domain.League;
import com.sports.server.member.domain.Member;
import com.sports.server.record.domain.Record;
import com.sports.server.sport.domain.Sport;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "games")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "sport_id")
    private Sport sport;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne
    @JoinColumn(name = "league_id")
    private League league;

    @OneToMany(mappedBy = "game")
    private List<Record> records = new ArrayList<>();

    @OneToMany(mappedBy = "game")
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

}
