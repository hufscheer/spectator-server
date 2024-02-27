package com.sports.server.query.application.timeline;

import com.sports.server.command.game.domain.GameTeam;

import java.util.Comparator;
import java.util.List;
import java.util.Map;


public class ScoreSnapshot {

    private final Map<GameTeam, Integer> values;

    public ScoreSnapshot(Map<GameTeam, Integer> values) {
        this.values = values;
    }

    public Integer getScore(GameTeam team) {
        return values.get(team);
    }

    public List<GameTeam> getTeamsOrderById() {
        return values.keySet()
                .stream()
                .sorted(Comparator.comparingLong(GameTeam::getId))
                .toList();
    }
}
