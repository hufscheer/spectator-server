package com.sports.server.query.application;

import com.sports.server.command.game.domain.GameTeam;
import com.sports.server.command.record.domain.ScoreRecord;
import com.sports.server.query.dto.response.RecordResponse;
import com.sports.server.query.dto.response.ScoreRecordResponse;
import com.sports.server.query.repository.GameTeamQueryRepository;
import com.sports.server.query.repository.ScoreRecordQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ScoreRecordQueryService implements RecordQueryService {

    private final ScoreRecordQueryRepository scoreRecordQueryRepository;
    private final GameTeamQueryRepository gameTeamQueryRepository;

    @Override
    public List<RecordResponse> findByGameId(Long gameId) {
        List<GameTeam> gameTeams = gameTeamQueryRepository.findAllByGameWithTeam(gameId);
        List<ScoreRecord> scoreRecords = scoreRecordQueryRepository.findByGameId(gameId);
        return mapToResponses(scoreRecords, gameTeams);
    }

    private List<RecordResponse> mapToResponses(List<ScoreRecord> scoreRecords,
                                                List<GameTeam> gameTeams) {
        Map<GameTeam, Integer> scores = initializeScores(gameTeams);
        List<RecordResponse> responses = scoreRecords.stream()
                .map(record -> mapToResponse(gameTeams, scores, record))
                .collect(toList());
        Collections.reverse(responses);
        return responses;
    }

    private Map<GameTeam, Integer> initializeScores(List<GameTeam> gameTeams) {
        return gameTeams.stream()
                .collect(toMap(gameTeam -> gameTeam, gameTeam -> 0));
    }

    private RecordResponse mapToResponse(List<GameTeam> gameTeams,
                                         Map<GameTeam, Integer> scores,
                                         ScoreRecord scoreRecord) {
        GameTeam gameTeam = scoreRecord.getRecord().getGameTeam();
        int score = scoreRecord.getScore();

        scores.put(gameTeam, scores.get(gameTeam) + score);

        List<ScoreRecordResponse.History> histories = generateHistories(scores, gameTeams);
        return RecordResponse.from(
                scoreRecord,
                new ScoreRecordResponse(score, histories)
        );
    }

    private List<ScoreRecordResponse.History> generateHistories(Map<GameTeam, Integer> scores,
                                                                List<GameTeam> gameTeams) {
        return gameTeams.stream()
                .map(team -> new ScoreRecordResponse.History(
                        team.getLeagueTeam().getName(),
                        team.getLeagueTeam().getLogoImageUrl(),
                        scores.get(team)))
                .toList();
    }
}
