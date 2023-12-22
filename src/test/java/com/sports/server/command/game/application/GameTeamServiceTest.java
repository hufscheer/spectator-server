package com.sports.server.command.game.application;

import com.sports.server.command.game.domain.GameTeam;
import com.sports.server.command.game.dto.GameTeamCheerRequestDto;
import com.sports.server.common.exception.CustomException;
import com.sports.server.support.ServiceTest;
import com.sports.server.support.fixture.GameTeamFixtureRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Sql(scripts = "/game-fixture.sql")
class GameTeamServiceTest extends ServiceTest {

    @Autowired
    private GameTeamService gameTeamService;

    @Autowired
    private GameTeamFixtureRepository gameTeamFixtureRepository;

    @Test
    void 경기에_참여하지_않는_팀을_응원하면_예외가_발생한다() {

        //given
        Long gameId = 1L;
        Long gameTeamId = 3L;
        GameTeamCheerRequestDto cheerRequestDto = new GameTeamCheerRequestDto(gameTeamId, 1);

        //when&then
        assertThrows(CustomException.class, () -> gameTeamService.updateCheerCount(gameId, cheerRequestDto));

    }

    @Test
    void 존재하지_않는_GameTeam에_대해서_요청을_보낼_경우_예외가_발생한다() {

        //given
        Long gameId = 1L;
        Long gameTeamId = 10000L;
        GameTeamCheerRequestDto cheerRequestDto = new GameTeamCheerRequestDto(gameTeamId, 1);

        //when&then
        assertThrows(CustomException.class, () -> gameTeamService.updateCheerCount(gameId, cheerRequestDto));

    }

    @Test
    void 동시에_응원_요청을_보낼_경우에도_정상적으로_요청이_반영된다() throws InterruptedException {
        // given
        ExecutorService executor = Executors.newFixedThreadPool(100);
        CountDownLatch latch = new CountDownLatch(100);

        long gameTeamId = 3L;
        GameTeamCheerRequestDto cheerRequestDto = new GameTeamCheerRequestDto(gameTeamId, 1);

        // when
        for (int i = 0; i < 100; i++) {
            executor.execute(() -> {
                gameTeamService.updateCheerCount(2L, cheerRequestDto);
                latch.countDown();
            });
        }
        latch.await();

        // then
        assertThat(gameTeamFixtureRepository.findById(gameTeamId))
                .map(GameTeam::getCheerCount)
                .get()
                .isEqualTo(101);
    }

}

