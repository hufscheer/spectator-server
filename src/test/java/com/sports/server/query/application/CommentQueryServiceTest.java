package com.sports.server.query.application;

import com.sports.server.query.dto.response.CommentResponse;
import com.sports.server.common.dto.PageRequestDto;
import com.sports.server.support.ServiceTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Sql(scripts = "/comment-fixture.sql")
class CommentQueryServiceTest extends ServiceTest {

    @Autowired
    private CommentQueryService commentQueryService;

    @Test
    void 댓글_조회시_경기에_참여하는_팀의_아이디_순서대로_정수가_반환된다() {
        //given
        Long gameId = 1L;
        PageRequestDto pageRequestDto = new PageRequestDto(null, 14);

        // when
        List<CommentResponse> commentsByGameId = commentQueryService.getCommentsByGameId(gameId, pageRequestDto);

        // then
        Map<Long, Integer> orderOfGameTeams = new HashMap<>();
        orderOfGameTeams.put(1L, 1);
        orderOfGameTeams.put(2L, 2);

        for (CommentResponse commentResponse : commentsByGameId) {
            assertEquals((int) orderOfGameTeams.get(commentResponse.gameTeamId()), commentResponse.order());
        }
    }
}
