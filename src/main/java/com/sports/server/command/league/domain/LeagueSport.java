package com.sports.server.command.league.domain;

import com.sports.server.common.domain.BaseEntity;
import com.sports.server.command.sport.domain.Sport;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "league_sports")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class LeagueSport extends BaseEntity<LeagueSport> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sport_id")
    private Sport sport;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "league_id")
    private League league;


}
