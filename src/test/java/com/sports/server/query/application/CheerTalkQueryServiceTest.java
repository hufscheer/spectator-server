package com.sports.server.query.application;

import com.sports.server.query.dto.response.CheerTalkResponse;
import com.sports.server.common.dto.PageRequestDto;
import com.sports.server.support.ServiceTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Sql(scripts = "/cheer-talk-fixture.sql")
class CheerTalkQueryServiceTest extends ServiceTest {

    @Autowired
    private CheerTalkQueryService cheerTalkQueryService;

    @Test
    void 댓글_조회시_경기에_참여하는_팀의_아이디_순서대로_정수가_반환된다() {
        //given
        Long gameId = 1L;
        PageRequestDto pageRequestDto = new PageRequestDto(null, 14);

        // when
        List<CheerTalkResponse> commentsByGameId = cheerTalkQueryService.getCheerTalksByGameId(gameId, pageRequestDto);

        // then
        Map<Long, Integer> orderOfGameTeams = new HashMap<>();
        orderOfGameTeams.put(1L, 1);
        orderOfGameTeams.put(2L, 2);

        for (CheerTalkResponse cheerTalkResponse : commentsByGameId) {
            assertEquals((int) orderOfGameTeams.get(cheerTalkResponse.gameTeamId()), cheerTalkResponse.order());
        }
    }
}
