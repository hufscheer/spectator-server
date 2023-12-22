package com.sports.server.command.comment.application;

import com.sports.server.command.comment.dto.CommentRequestDto;
import com.sports.server.common.exception.CustomException;
import com.sports.server.support.ServiceTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertThrows;


public class CommentServiceTest extends ServiceTest {

    @Autowired
    private CommentService commentService;

    @ParameterizedTest
    @ValueSource(strings = {"ㅅㅂ", "개새", "ㅆㅂ"})
    void 욕설이_포함된_댓글을_저장하려고_하면_예외가_발생한다(String content) {

        //given
        CommentRequestDto commentRequestDto = new CommentRequestDto(content, 1L);

        //when & then
        assertThrows(CustomException.class, () -> commentService.register(commentRequestDto));

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

}
