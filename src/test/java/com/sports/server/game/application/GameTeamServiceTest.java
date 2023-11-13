package com.sports.server.game.application;

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.sports.server.common.exception.CustomException;
import com.sports.server.game.dto.request.GameTeamCheerRequestDto;
import com.sports.server.support.isolation.DatabaseIsolation;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

@SpringBootTest
@DatabaseIsolation
@Sql(scripts = "/game-fixture.sql")
public class GameTeamServiceTest {

    @Autowired
    private GameTeamService gameTeamService;

    @Test
    void 경기에_참여하지_않는_팀을_응원하면_예외가_발생한다() {

        //given
        Long gameId = 1L;
        Long gameTeamId = 3L;
        GameTeamCheerRequestDto cheerRequestDto = new GameTeamCheerRequestDto(gameTeamId, 1);

        //when&then
        assertThrows(CustomException.class, () -> {
            gameTeamService.updateCheerCount(gameId, cheerRequestDto);
        });

    }

    @Test
    void 존재하지_않는_GameTeam에_대해서_요청을_보낼_경우_예외가_발생한다() {

        //given
        Long gameId = 1L;
        Long gameTeamId = 10000L;
        GameTeamCheerRequestDto cheerRequestDto = new GameTeamCheerRequestDto(gameTeamId, 1);

        //when&then
        assertThrows(CustomException.class, () -> {
            gameTeamService.updateCheerCount(gameId, cheerRequestDto);
        });

    }
}
