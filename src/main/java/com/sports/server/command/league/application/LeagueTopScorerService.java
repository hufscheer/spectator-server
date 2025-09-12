package com.sports.server.command.league.application;

import com.sports.server.command.league.domain.League;
import com.sports.server.command.league.domain.LeagueTopScorer;
import com.sports.server.command.league.domain.LeagueTopScorerRepository;
import com.sports.server.command.player.domain.Player;
import com.sports.server.command.player.domain.PlayerRepository;
import com.sports.server.command.team.domain.PlayerGoalCountWithRank;
import com.sports.server.common.application.EntityUtils;
import com.sports.server.common.exception.NotFoundException;
import com.sports.server.query.support.PlayerInfoProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.function.UnaryOperator.identity;

@Service
@RequiredArgsConstructor
@Transactional
public class LeagueTopScorerService {

    private final LeagueTopScorerRepository leagueTopScorerRepository;
    private final PlayerRepository playerRepository;
    private final PlayerInfoProvider playerInfoProvider;
    private final EntityUtils entityUtils;

    public void updateTopScorersForLeague(Long leagueId) {

        League league = entityUtils.getEntity(leagueId, League.class);

        List<PlayerGoalCountWithRank> topScorers = playerInfoProvider.getLeagueTopScorers(leagueId, 20);
        
        leagueTopScorerRepository.deleteByLeagueId(leagueId);

        List<Long> playerIds = topScorers.stream()
                .map(PlayerGoalCountWithRank::playerId)
                .toList();
        
        List<Player> players = playerRepository.findAllById(playerIds);

        Map<Long, Player> playerMap = players.stream()
                .collect(Collectors.toMap(Player::getId, identity()));

        for (PlayerGoalCountWithRank scorerData : topScorers) {
            com.sports.server.command.player.domain.Player player = playerMap.get(scorerData.playerId());
            if (player == null) {
                throw new NotFoundException("존재하지 않는 선수입니다: " + scorerData.playerId());
            }

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
