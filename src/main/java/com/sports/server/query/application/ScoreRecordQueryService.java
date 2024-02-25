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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ScoreRecordQueryService {

    private final ScoreRecordQueryRepository scoreRecordQueryRepository;
    private final GameTeamQueryRepository gameTeamQueryRepository;

    public List<RecordResponse> findByGameId(Long gameId) {
        List<GameTeam> gameTeams = gameTeamQueryRepository.findAllByGameWithTeam(gameId);
        Map<GameTeam, Integer> scores = gameTeams.stream()
                .collect(toMap(gameTeam -> gameTeam, gameTeam -> 0));

        List<ScoreRecord> scoreRecords = scoreRecordQueryRepository.findByGameId(gameId);

        List<RecordResponse> responses = new ArrayList<>();
        for (ScoreRecord scoreRecord : scoreRecords) {
            GameTeam gameTeam = scoreRecord.getRecord().getGameTeam();
            int score = scoreRecord.getScore();
            scores.put(gameTeam, scores.get(gameTeam) + score);
            List<ScoreRecordResponse.History> histories = gameTeams.stream()
                    .map(team -> new ScoreRecordResponse.History(
                            team.getLeagueTeam().getName(),
                            team.getLeagueTeam().getLogoImageUrl(),
                            scores.get(team)))
                    .toList();
            ScoreRecordResponse scoreRecordResponse = new ScoreRecordResponse(score, histories);
            responses.add(RecordResponse.from(scoreRecord, scoreRecordResponse));
        }
        Collections.reverse(responses);
        return responses;
    }
}
