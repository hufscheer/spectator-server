package com.sports.server.command.league.application;

import com.sports.server.command.league.domain.League;
import com.sports.server.command.league.domain.LeagueRepository;
import com.sports.server.command.league.domain.LeagueStatistics;
import com.sports.server.command.league.domain.LeagueStatisticsRepository;
import com.sports.server.command.league.domain.LeagueTeam;
import com.sports.server.command.league.domain.LeagueTeamRanking;
import com.sports.server.command.league.domain.LeagueTeamRef;
import com.sports.server.command.league.domain.LeagueTeamRepository;
import com.sports.server.command.league.domain.LeagueTeamTally;
import com.sports.server.command.league.domain.TeamTotal;
import com.sports.server.command.team.domain.Team;
import com.sports.server.command.team.domain.TeamRepository;
import com.sports.server.common.exception.NotFoundException;
import com.sports.server.query.repository.LeagueTeamQueryRepository;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class LeagueCheerRankingService {

    private final LeagueRepository leagueRepository;
    private final LeagueTeamRepository leagueTeamRepository;
    private final LeagueTeamQueryRepository leagueTeamQueryRepository;
    private final LeagueStatisticsRepository leagueStatisticsRepository;
    private final TeamRepository teamRepository;

    // 커밋 직후 콜백에서 불리므로 새 트랜잭션으로 연다.
    // 대회 행을 첫 쿼리로 잠가 같은 대회의 재집계와 통계 행 생성을 줄 세운다
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateCheerRankingsForLeague(Long leagueId) {
        League league = leagueRepository.findByIdForUpdate(leagueId)
                .orElseThrow(() -> new NotFoundException(League.class.getSimpleName() + "을(를) 찾을 수 없습니다"));

        updateTotals(leagueId);

        // 같은 트랜잭션의 변경을 읽도록 먼저 내보낸다
        leagueTeamRepository.flush();
        List<LeagueTeamTally> tallies = leagueTeamQueryRepository.findTalliesByLeagueId(leagueId);

        LeagueStatistics statistics = leagueStatisticsRepository.findByLeagueId(leagueId);
        if (statistics == null) {
            statistics = LeagueStatistics.of(league);
            leagueStatisticsRepository.save(statistics);
        }
        statistics.updateMostCheeredTeam(firstTeam(tallies, LeagueTeamTally::cheerCount));
        statistics.updateMostCheerTalksTeam(firstTeam(tallies, LeagueTeamTally::talkCount));
    }

    // 삭제된 팀을 가리키는 리그팀의 team 필드는 건드리지 않고 FK 값으로 맞춘다
    private void updateTotals(Long leagueId) {
        Map<Long, Long> cheerByTeamId = toMap(leagueTeamRepository.sumCheerCountsOfFinishedGames(leagueId));
        Map<Long, Long> talkByTeamId = toMap(leagueTeamRepository.countActiveCheerTalksOfFinishedGames(leagueId));
        Map<Long, LeagueTeam> leagueTeamById = leagueTeamRepository.findByLeagueId(leagueId).stream()
                .collect(Collectors.toMap(LeagueTeam::getId, Function.identity()));

        for (LeagueTeamRef ref : leagueTeamRepository.findRefsByLeagueId(leagueId)) {
            leagueTeamById.get(ref.leagueTeamId()).updateTotals(
                    toInt(cheerByTeamId.get(ref.teamId())),
                    toInt(talkByTeamId.get(ref.teamId())));
        }
    }

    private Map<Long, Long> toMap(List<TeamTotal> totals) {
        return totals.stream().collect(Collectors.toMap(TeamTotal::teamId, TeamTotal::total));
    }

    private int toInt(Long value) {
        return value == null ? 0 : (int) Math.min(value, Integer.MAX_VALUE);
    }

    private Team firstTeam(List<LeagueTeamTally> tallies, ToIntFunction<LeagueTeamTally> countOf) {
        return LeagueTeamRanking.rank(tallies, countOf).stream()
                .findFirst()
                .flatMap(ranked -> teamRepository.findAllById(List.of(ranked.tally().teamId())).stream().findFirst())
                .orElse(null);
    }
}
