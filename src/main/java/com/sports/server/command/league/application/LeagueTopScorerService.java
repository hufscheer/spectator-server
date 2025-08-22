package com.sports.server.command.league.application;

import com.sports.server.command.league.domain.League;
import com.sports.server.command.league.domain.LeagueRepository;
import com.sports.server.command.league.domain.LeagueTopScorer;
import com.sports.server.command.league.domain.LeagueTopScorerRepository;
import com.sports.server.command.player.domain.PlayerRepository;
import com.sports.server.command.team.domain.PlayerGoalCountWithRank;
import com.sports.server.query.support.PlayerInfoProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class LeagueTopScorerService {

    private final LeagueTopScorerRepository leagueTopScorerRepository;
    private final LeagueRepository leagueRepository;
    private final PlayerRepository playerRepository;
    private final PlayerInfoProvider playerInfoProvider;

    public void updateTopScorersForLeague(Long leagueId) {
        League league = leagueRepository.findWithTeamsById(leagueId)
                .orElseThrow(() -> new IllegalArgumentException("League not found: " + leagueId));

        List<PlayerGoalCountWithRank> topScorers = playerInfoProvider.getLeagueTop20Scorers(leagueId);
        
        leagueTopScorerRepository.deleteByLeagueId(leagueId);

        List<Long> playerIds = topScorers.stream()
                .map(PlayerGoalCountWithRank::playerId)
                .toList();
        
        List<com.sports.server.command.player.domain.Player> players = playerRepository.findAllById(playerIds);

        for (PlayerGoalCountWithRank scorerData : topScorers) {
            com.sports.server.command.player.domain.Player player = players.stream()
                    .filter(p -> p.getId().equals(scorerData.playerId()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Player not found: " + scorerData.playerId()));

            LeagueTopScorer topScorer = new LeagueTopScorer(
                    league,
                    player,
                    scorerData.rank().intValue(),
                    scorerData.goalCount().intValue()
            );
            leagueTopScorerRepository.save(topScorer);
        }
    }
}
