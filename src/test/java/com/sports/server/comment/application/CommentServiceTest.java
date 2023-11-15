package com.sports.server.comment.application;

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.sports.server.comment.dto.request.CommentRequestDto;
import com.sports.server.common.exception.CustomException;
import com.sports.server.support.ServiceTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class CommentServiceTest extends ServiceTest {

    @Autowired
    private CommentService commentService;

    @Test
    void 욕설이_포함된_댓글을_저장하려고_하면_예외가_발생한다() {

        //given
        CommentRequestDto commentRequestDto = new CommentRequestDto("ㅅㅂ", 1L);

        //when & then
        //when&then
        assertThrows(CustomException.class, () -> {
            commentService.register(commentRequestDto);
        });

    }

}
