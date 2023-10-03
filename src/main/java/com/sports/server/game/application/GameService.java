package com.sports.server.game.application;

import com.sports.server.game.domain.Game;
import com.sports.server.game.domain.GameRepository;
import com.sports.server.game.dto.request.GameRegisterRequestDto;
import com.sports.server.game.dto.response.GameResponseDto;
import com.sports.server.team.application.TeamService;
import com.sports.server.team.domain.Team;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GameService {

    private final GameRepository gameRepository;
    private final TeamService teamService;

    @Transactional
    public void register(final GameRegisterRequestDto requestDto) {
        Team firstTeam = teamService.findTeamWithId(requestDto.getFirstTeamId());
        Team secondTeam = teamService.findTeamWithId(requestDto.getSecondTeamId());

        // TODO: Member 로그인한 사용자로 변경하기
        Game game = requestDto.toEntity(firstTeam, secondTeam);
        gameRepository.save(game);
    }

    public GameResponseDto getOneGame(final Long gameId) {
        Game game = findGameWithId(gameId);
        return new GameResponseDto(game);
    }

    private Game findGameWithId(final Long gameId) {
        return gameRepository.findById(gameId).orElseThrow(IllegalArgumentException::new);
    }

}
