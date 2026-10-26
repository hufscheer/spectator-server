package com.sports.server.command.league.application;

import static java.util.function.UnaryOperator.identity;

import com.sports.server.command.league.domain.League;
import com.sports.server.command.league.domain.LeagueRepository;
import com.sports.server.command.league.domain.LeagueTopScorer;
import com.sports.server.command.league.domain.LeagueTopScorerRepository;
import com.sports.server.command.player.domain.Player;
import com.sports.server.command.player.domain.PlayerRepository;
import com.sports.server.command.team.domain.PlayerGoalCountWithRank;
import com.sports.server.common.exception.NotFoundException;
import com.sports.server.query.support.PlayerInfoProvider;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class LeagueTopScorerService {

    private final LeagueTopScorerRepository leagueTopScorerRepository;
    private final PlayerRepository playerRepository;
    private final PlayerInfoProvider playerInfoProvider;
    private final LeagueRepository leagueRepository;

    // 커밋 직후 콜백에서 불리므로 새 트랜잭션으로 연다.
    // 대회 행을 첫 쿼리로 잠가 같은 대회의 재집계를 줄 세우고, 잠근 뒤에 읽어야 앞서 커밋된 경기를 모두 본다
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateTopScorersForLeague(Long leagueId) {

        League league = leagueRepository.findByIdForUpdate(leagueId)
                .orElseThrow(() -> new NotFoundException(League.class.getSimpleName() + "을(를) 찾을 수 없습니다"));

        List<PlayerGoalCountWithRank> topScorers = playerInfoProvider.getLeagueTopScorers(leagueId, 20);

        leagueTopScorerRepository.deleteAllByLeagueId(leagueId);

        List<Long> playerIds = topScorers.stream()
                .map(PlayerGoalCountWithRank::playerId)
                .toList();

        List<Player> players = playerRepository.findAllById(playerIds);

        Map<Long, Player> playerMap = players.stream()
                .collect(Collectors.toMap(Player::getId, identity()));

        Map<Long, Long> teamIdByPlayerId = playerInfoProvider.getLeagueScorerTeamIds(leagueId, playerIds);

        List<LeagueTopScorer> topScorersToBeSaved = new ArrayList<>();

        for (PlayerGoalCountWithRank scorerData : topScorers) {
            com.sports.server.command.player.domain.Player player = playerMap.get(scorerData.playerId());
            if (player == null) {
                throw new NotFoundException("존재하지 않는 선수입니다: " + scorerData.playerId());
            }

            LeagueTopScorer topScorer = new LeagueTopScorer(
                    league,
                    player,
                    scorerData.rank().intValue(),
                    scorerData.goalCount().intValue(),
                    teamIdByPlayerId.get(scorerData.playerId())
            );
            topScorersToBeSaved.add(topScorer);
        }

        leagueTopScorerRepository.saveAll(topScorersToBeSaved);
    }
}
