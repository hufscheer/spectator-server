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
        saveGameTeams(game, requestDto);
    }

    private void saveGameTeams(Game game, GameRequestDto.Register requestDto) {

        LeagueTeam leagueTeam1 = entityUtils.getEntity(requestDto.idOfTeam1(), LeagueTeam.class);
        LeagueTeam leagueTeam2 = entityUtils.getEntity(requestDto.idOfTeam2(), LeagueTeam.class);

        GameTeam gameTeam1 = new GameTeam(game, leagueTeam1);
        GameTeam gameTeam2 = new GameTeam(game, leagueTeam1);

        game.addTeam(gameTeam1);
        game.addTeam(gameTeam2);

        leagueTeam1.getLeagueTeamPlayers().stream()
                .forEach(gameTeam1::registerLineup);
        leagueTeam2.getLeagueTeamPlayers().stream()
                .forEach(gameTeam2::registerLineup);
    }

    private Game saveGame(Long leagueId, Member manager, GameRequestDto.Register requestDto) {
        Sport sport = sportRepository.findByName(NAME_OF_SPORT)
                .orElseThrow(() -> new NotFoundException("해당 이름을 가진 스포츠가 존재하지 않습니다."));
        League league = getLeagueAndCheckPermission(leagueId, manager);
        Game game = requestDto.toEntity(sport, manager, league);
        gameRepository.save(game);
        return game;
    }

    private League getLeagueAndCheckPermission(final Long leagueId, final Member manager) {
        League league = entityUtils.getEntity(leagueId, League.class);

        if (!league.isManagedBy(manager)) {
            throw new UnauthorizedException(AuthorizationErrorMessages.PERMISSION_DENIED);
        }

        return league;
    }
}