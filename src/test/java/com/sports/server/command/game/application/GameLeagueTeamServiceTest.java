package com.sports.server.command.game.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.sports.server.command.game.domain.GameTeam;
import com.sports.server.command.game.dto.CheerCountUpdateRequest;
import com.sports.server.common.exception.CustomException;
import com.sports.server.support.ServiceTest;
import com.sports.server.support.fixture.GameTeamFixtureRepository;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

@Sql(scripts = "/game-fixture.sql")
class GameLeagueTeamServiceTest extends ServiceTest {

    private static final int MAXIMUM_OF_TOTAL_CHEER_COUNT = 100_000_000;
    private static final int MAXIMUM_OF_CHEER_COUNT = 500;

    @Autowired
    private GameTeamService gameTeamService;

    @Autowired
    private GameTeamFixtureRepository gameTeamFixtureRepository;

    @Test
    void 경기에_참여하지_않는_팀을_응원하면_예외가_발생한다() {

        //given
        Long gameId = 1L;
        Long gameTeamId = 3L;
        CheerCountUpdateRequest cheerRequestDto = new CheerCountUpdateRequest(gameTeamId, 1);

        //when&then
        assertThrows(CustomException.class, () -> gameTeamService.updateCheerCount(gameId, cheerRequestDto));

    }

    @Test
    void 존재하지_않는_GameTeam에_대해서_요청을_보낼_경우_예외가_발생한다() {

        //given
        Long gameId = 1L;
        Long gameTeamId = 10000L;
        CheerCountUpdateRequest cheerRequestDto = new CheerCountUpdateRequest(gameTeamId, 1);

        //when&then
        assertThrows(CustomException.class, () -> gameTeamService.updateCheerCount(gameId, cheerRequestDto));

    }

    @Test
    void 동시에_응원_요청을_보낼_경우에도_정상적으로_요청이_반영된다() throws InterruptedException {
        // given
        ExecutorService executor = Executors.newFixedThreadPool(100);
        CountDownLatch latch = new CountDownLatch(100);

        long gameTeamId = 3L;
        CheerCountUpdateRequest cheerRequestDto = new CheerCountUpdateRequest(gameTeamId, 1);

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

    @Test
    void 총_응원_횟수가_제한에_도달했을_시_예외를_반환한다() {

        //given
        Long gameId = 1L;
        Long gameTeamId = 10000L;
        CheerCountUpdateRequest cheerRequestDto = new CheerCountUpdateRequest(gameTeamId, MAXIMUM_OF_TOTAL_CHEER_COUNT);

        //when&then
        assertThrows(CustomException.class, () -> gameTeamService.updateCheerCount(gameId, cheerRequestDto));
    }

    @Test
    void 응원_횟수가_제한에_도달했을_시_예외를_반환한다() {

        //given
        Long gameId = 1L;
        Long gameTeamId = 10000L;
        CheerCountUpdateRequest cheerRequestDto = new CheerCountUpdateRequest(gameTeamId, MAXIMUM_OF_CHEER_COUNT);

        //when&then
        assertThrows(CustomException.class, () -> gameTeamService.updateCheerCount(gameId, cheerRequestDto));
    }

}
