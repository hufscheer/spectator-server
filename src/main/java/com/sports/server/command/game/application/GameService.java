package com.sports.server.command.game.application;

import com.sports.server.auth.exception.AuthorizationErrorMessages;
import com.sports.server.command.game.domain.Game;
import com.sports.server.command.game.domain.GameRepository;
import com.sports.server.command.game.domain.GameTeam;
import com.sports.server.command.game.domain.LineupPlayer;
import com.sports.server.command.game.domain.LineupPlayerState;
import com.sports.server.command.game.dto.GameRequestDto;
import com.sports.server.command.league.domain.League;
import com.sports.server.command.leagueteam.domain.LeagueTeam;
import com.sports.server.command.member.domain.Member;
import com.sports.server.command.sport.domain.Sport;
import com.sports.server.command.sport.domain.SportRepository;
import com.sports.server.common.application.EntityUtils;
import com.sports.server.common.exception.NotFoundException;
import com.sports.server.common.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GameService {

    private final EntityUtils entityUtils;
    private final GameRepository gameRepository;
    private final SportRepository sportRepository;
    private static final String nameOfSport = "축구";

    @Transactional
    public void register(final Long leagueId,
                         final GameRequestDto.Register requestDto,
                         final Member manager
    ) {
        // Game 저장
        Sport sport = getSport(nameOfSport);
        League league = getLeagueAndCheckPermission(leagueId, manager);
        Game game = requestDto.toEntity(sport, manager, league);

        // GameTeam 저장
        LeagueTeam leagueTeam1 = entityUtils.getEntity(requestDto.idOfTeam1(), LeagueTeam.class);
        LeagueTeam leagueTeam2 = entityUtils.getEntity(requestDto.idOfTeam2(), LeagueTeam.class);
        GameTeam gameTeam1 = new GameTeam(game, leagueTeam1);
        GameTeam gameTeam2 = new GameTeam(game, leagueTeam2);

        game.addTeam(gameTeam1);
        game.addTeam(gameTeam2);

        // LeagueTeamPlayer >> LineupPlayer 로 복사
        leagueTeam1.getLeagueTeamPlayers().stream()
                .map(lgp -> new LineupPlayer(
                        gameTeam1, lgp.getId(), lgp.getName(), lgp.getNumber(), false, LineupPlayerState.CANDIDATE))
                .forEach(gameTeam1::registerLineup);

        leagueTeam2.getLeagueTeamPlayers().stream()
                .map(lgp -> new LineupPlayer(
                        gameTeam1, lgp.getId(), lgp.getName(), lgp.getNumber(), false, LineupPlayerState.CANDIDATE)
                ).forEach(gameTeam2::registerLineup);

        gameRepository.save(game);
    }

    private Sport getSport(String nameOfSport) {
        return sportRepository.findByName(nameOfSport)
                .orElseThrow(() -> new NotFoundException("해당 이름을 가진 스포츠가 존재하지 않습니다."));
    }

    private League getLeagueAndCheckPermission(final Long leagueId, final Member manager) {
        League league = entityUtils.getEntity(leagueId, League.class);

        if (!league.isManagedBy(manager)) {
            throw new UnauthorizedException(AuthorizationErrorMessages.PERMISSION_DENIED);
        }

        return league;
    }
}
