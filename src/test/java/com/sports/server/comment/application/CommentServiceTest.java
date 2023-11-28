package com.sports.server.comment.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sports.server.comment.dto.request.CommentRequestDto;
import com.sports.server.comment.dto.response.CommentResponse;
import com.sports.server.common.dto.PageRequestDto;
import com.sports.server.common.exception.CustomException;
import com.sports.server.support.ServiceTest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

@Sql(scripts = "/comment-fixture.sql")
public class CommentServiceTest extends ServiceTest {

    @Autowired
    private CommentService commentService;

    @ParameterizedTest
    @ValueSource(strings = {"ㅅㅂ", "개새", "ㅆㅂ"})
    void 욕설이_포함된_댓글을_저장하려고_하면_예외가_발생한다(String content) {

        //given
        CommentRequestDto commentRequestDto = new CommentRequestDto(content, 1L);

        //when & then
        assertThrows(CustomException.class, () -> {
            commentService.register(commentRequestDto);
        });

    }

    @ParameterizedTest
    @ValueSource(strings = {"안녕", "파이팅", "할 수 있어!"})
    void 욕설이_포함되지_않은_댓글은_정상적으로_저장된다(String content) {

        //given
        CommentRequestDto commentRequestDto = new CommentRequestDto(content, 1L);

        //when & then
        assertThatCode(() -> commentService.register(commentRequestDto))
                .doesNotThrowAnyException();
    }

    @Test
    void 댓글_조회시_경기에_참여하는_팀의_아이디_순서대로_정수가_반환된다() {

        //given
        Long gameId = 1L;
        PageRequestDto pageRequestDto = new PageRequestDto(null, 14);

        // when
        List<CommentResponse> commentsByGameId = commentService.getCommentsByGameId(gameId, pageRequestDto);

        // then
        Map<Long, Integer> orderOfGameTeams = new HashMap<>();
        orderOfGameTeams.put(1L, 1);
        orderOfGameTeams.put(2L, 2);

        for (CommentResponse commentResponse : commentsByGameId) {
            assertEquals((int) orderOfGameTeams.get(commentResponse.gameTeamId()), commentResponse.order());
        }
    }

}
