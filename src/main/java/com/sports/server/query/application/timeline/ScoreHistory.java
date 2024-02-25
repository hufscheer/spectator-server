package com.sports.server.query.application.timeline;

import com.sports.server.command.game.domain.GameTeam;
import com.sports.server.command.record.domain.ScoreRecord;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ScoreHistory {

    private final Map<ScoreRecord, ScoreSnapshot> snapshots;

    public static ScoreHistory of(List<ScoreRecord> scoreRecords, List<GameTeam> gameTeams) {
        Map<ScoreRecord, ScoreSnapshot> snapshots = new HashMap<>();
        Map<GameTeam, Integer> scores = initializeScores(gameTeams);
        scoreRecords.forEach(record -> {
            applyScore(scores, record);
            ScoreSnapshot snapshot = generateSnapshot(scores, gameTeams);
            snapshots.put(record, snapshot);
        });
        return new ScoreHistory(snapshots);
    }

    private static Map<GameTeam, Integer> initializeScores(List<GameTeam> gameTeams) {
        return gameTeams.stream()
                .collect(toMap(gameTeam -> gameTeam, gameTeam -> 0));
    }

    private static void applyScore(Map<GameTeam, Integer> scores, ScoreRecord record) {
        GameTeam gameTeam = record.getRecord().getGameTeam();
        int score = record.getScore();
        scores.put(gameTeam, scores.get(gameTeam) + score);
    }

    private static ScoreSnapshot generateSnapshot(Map<GameTeam, Integer> scores,
                                                  List<GameTeam> gameTeams) {
        Map<GameTeam, Integer> snapshot = new HashMap<>();
        gameTeams.forEach(team -> snapshot.put(team, scores.get(team)));
        return new ScoreSnapshot(snapshot);
    }

    public List<ScoreRecord> getScoreRecordsOrderByTimeDesc() {
        return snapshots.keySet().stream()
                .sorted((r1, r2) -> Integer.compare(r2.getRecord().getRecordedAt(), r1.getRecord().getRecordedAt()))
                .toList();
    }

    public ScoreSnapshot getSnapshot(ScoreRecord scoreRecord) {
        return snapshots.get(scoreRecord);
    }
}
