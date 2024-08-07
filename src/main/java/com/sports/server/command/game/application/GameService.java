package com.sports.server.command.game.application;

import com.sports.server.auth.exception.AuthorizationErrorMessages;
import com.sports.server.command.game.domain.Game;
import com.sports.server.command.game.domain.GameRepository;
import com.sports.server.command.game.domain.GameTeam;
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
    private static final String NAME_OF_SPORT = "축구";

    @Transactional
    public void register(final Long leagueId,
                         final GameRequestDto.Register requestDto,
                         final Member manager) {
        Game game = saveGame(leagueId, manager, requestDto);
        saveGameTeamsAndCopyPlayers(requestDto, game);

        gameRepository.save(game);
    }

    private void saveGameTeamsAndCopyPlayers(GameRequestDto.Register requestDto, Game game) {
        LeagueTeam leagueTeam1 = getLeagueTeam(requestDto.idOfTeam1());
        LeagueTeam leagueTeam2 = getLeagueTeam(requestDto.idOfTeam2());

        GameTeam gameTeam1 = createGameTeam(game, leagueTeam1);
        GameTeam gameTeam2 = createGameTeam(game, leagueTeam2);

        game.addTeam(gameTeam1);
        game.addTeam(gameTeam2);

        copyPlayersToLineup(gameTeam1, leagueTeam1);
        copyPlayersToLineup(gameTeam2, leagueTeam2);
    }

    private LeagueTeam getLeagueTeam(Long teamId) {
        return entityUtils.getEntity(teamId, LeagueTeam.class);
    }

    private GameTeam createGameTeam(Game game, LeagueTeam leagueTeam) {
        return new GameTeam(game, leagueTeam);
    }

    private void copyPlayersToLineup(GameTeam gameTeam, LeagueTeam leagueTeam) {
        leagueTeam.getLeagueTeamPlayers().stream()
                .forEach(gameTeam::registerLineup);
    }

    private Game saveGame(Long leagueId, Member manager, GameRequestDto.Register requestDto) {
        Sport sport = getSport(NAME_OF_SPORT);
        League league = getLeagueAndCheckPermission(leagueId, manager);
        return requestDto.toEntity(sport, manager, league);
    }

    private Sport getSport(String sportName) {
        return sportRepository.findByName(sportName)
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