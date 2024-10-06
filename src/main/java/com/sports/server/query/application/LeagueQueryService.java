package com.sports.server.query.application;

import static java.util.stream.Collectors.toMap;

import com.sports.server.command.game.domain.Game;
import com.sports.server.command.league.domain.League;
import com.sports.server.command.league.domain.LeagueProgress;
import com.sports.server.command.leagueteam.domain.LeagueTeam;
import com.sports.server.command.leagueteam.domain.LeagueTeamPlayer;
import com.sports.server.command.member.domain.Member;
import com.sports.server.common.application.EntityUtils;
import com.sports.server.common.exception.NotFoundException;
import com.sports.server.query.dto.response.LeagueDetailResponse;
import com.sports.server.query.dto.response.LeagueResponse;
import com.sports.server.query.dto.response.LeagueResponseToManage;
import com.sports.server.query.dto.response.LeagueResponseWithGames;
import com.sports.server.query.dto.response.LeagueResponseWithInProgressGames;
import com.sports.server.query.dto.response.LeagueSportResponse;
import com.sports.server.query.dto.response.LeagueTeamDetailResponse;
import com.sports.server.query.dto.response.LeagueTeamPlayerResponse;
import com.sports.server.query.dto.response.LeagueTeamResponse;
import com.sports.server.query.repository.GameQueryRepository;
import com.sports.server.query.repository.LeagueQueryRepository;
import com.sports.server.query.repository.LeagueSportQueryRepository;
import com.sports.server.query.repository.LeagueTeamDynamicRepository;
import com.sports.server.query.repository.LeagueTeamPlayerQueryRepository;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class LeagueQueryService {

    private final LeagueQueryRepository leagueQueryRepository;
    private final LeagueSportQueryRepository leagueSportQueryRepository;
    private final LeagueTeamDynamicRepository leagueTeamDynamicRepository;
    private final LeagueTeamPlayerQueryRepository leagueTeamPlayerQueryRepository;
    private final GameQueryRepository gameQueryRepository;
    private final EntityUtils entityUtils;

    public List<LeagueResponse> findLeagues(Integer year) {
        return leagueQueryRepository.findByYear(year)
                .stream()
                .map(LeagueResponse::new)
                .toList();
    }

    public List<LeagueSportResponse> findSportsByLeague(Long leagueId) {
        return leagueSportQueryRepository.findByLeagueId(leagueId)
                .stream()
                .map(LeagueSportResponse::new)
                .toList();
    }

    public List<LeagueTeamResponse> findTeamsByLeagueRound(Long leagueId, Integer round) {
        League league = entityUtils.getEntity(leagueId, League.class);

        return leagueTeamDynamicRepository.findByLeagueAndRound(league, round)
                .stream()
                .map(LeagueTeamResponse::new)
                .toList();
    }

    public LeagueDetailResponse findLeagueDetail(Long leagueId) {
        return leagueQueryRepository.findById(leagueId)
                .map(league -> {
                    return LeagueDetailResponse.of(league,
                            leagueTeamDynamicRepository.findByLeagueAndRound(league, null).size());
                })
                .orElseThrow(() -> new NotFoundException("존재하지 않는 리그입니다"));
    }

    public List<LeagueTeamPlayerResponse> findPlayersByLeagueTeam(Long leagueTeamId) {
        return leagueTeamPlayerQueryRepository.findByLeagueTeamId(leagueTeamId)
                .stream()
                .map(LeagueTeamPlayerResponse::new)
                .toList();
    }

    public List<LeagueResponseWithInProgressGames> findLeaguesByManager(final Member member) {
        List<League> leagues = leagueQueryRepository.findByManager(member);
        Map<League, List<Game>> gamesForLeagues = getGamesForLeague(leagues);

        return leagues.stream()
                .map(league -> LeagueResponseWithInProgressGames.of(
                        league,
                        LeagueProgress.getProgressDescription(LocalDateTime.now(), league),
                        gamesForLeagues.get(league)))
                .toList();
    }

    private Map<League, List<Game>> getGamesForLeague(List<League> leagues) {
        return leagues.stream()
                .collect(toMap(league -> league,
                        gameQueryRepository::findPlayingGamesByLeagueWithGameTeams));
    }


    public LeagueTeamDetailResponse findLeagueTeam(final Long leagueTeamId) {
        LeagueTeam leagueTeam = entityUtils.getEntity(leagueTeamId, LeagueTeam.class);
        List<LeagueTeamPlayer> leagueTeamPlayers = leagueTeamPlayerQueryRepository.findByLeagueTeamId(
                leagueTeam.getId());
        return LeagueTeamDetailResponse.of(leagueTeam, leagueTeamPlayers);
    }

    public LeagueResponseWithGames findLeagueAndGames(final Long leagueId) {
        League league = leagueQueryRepository.findByIdWithLeagueTeam(leagueId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 리그입니다"));
        List<Game> games = gameQueryRepository.findByLeagueWithGameTeams(league);
        return LeagueResponseWithGames.of(league, games);
    }

    public List<LeagueResponseToManage> findLeaguesByManagerToManage(final Member manager) {
        List<League> leagues = leagueQueryRepository.findByManagerToManage(manager);

        Comparator<League> comparator = Comparator.comparing(
                league -> leagueProgressOrderMap.get(
                        LeagueProgress.getProgressDescription(LocalDateTime.now(), league)));

        return leagues.stream()
                .sorted(comparator)
                .map(LeagueResponseToManage::of)
                .toList();
    }

    public static Map<String, Integer> leagueProgressOrderMap = Map.ofEntries(
            Map.entry(LeagueProgress.IN_PROGRESS.getDescription(), 1),
            Map.entry(LeagueProgress.BEFORE_START.getDescription(), 2),
            Map.entry(LeagueProgress.FINISHED.getDescription(), 3)
    );
}
