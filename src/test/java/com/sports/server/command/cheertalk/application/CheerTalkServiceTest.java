package com.sports.server.command.cheertalk.application;

import com.sports.server.command.cheertalk.dto.CheerTalkRequest;
import com.sports.server.command.member.domain.Member;
import com.sports.server.common.application.EntityUtils;
import com.sports.server.common.exception.CustomException;
import com.sports.server.support.ServiceTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Sql("/cheer-talk-fixture.sql")
public class CheerTalkServiceTest extends ServiceTest {

    @Autowired
    private CheerTalkService cheerTalkService;

    @Autowired
    private EntityUtils entityUtils;

    private Long managerId;
    private Long leagueId;
    private Member manager;

    @BeforeEach
    void setUp() {
        managerId = 1L;
        leagueId = 1L;
        manager = entityUtils.getEntity(1L, Member.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"ㅅㅂ", "개새", "ㅆㅂ"})
    void 욕설이_포함된_응원톡을_저장하려고_하면_예외가_발생한다(String content) {

        //given
        CheerTalkRequest cheerTalkRequest = new CheerTalkRequest(content, 1L);

        //when & then
        assertThrows(CustomException.class, () -> cheerTalkService.register(cheerTalkRequest));

    }

    @ParameterizedTest
    @ValueSource(strings = {"안녕", "파이팅", "할 수 있어!"})
    void 욕설이_포함되지_않은_응원톡은_정상적으로_저장된다(String content) {

        //given
        CheerTalkRequest cheerTalkRequest = new CheerTalkRequest(content, 1L);

        //when & then
        assertThatCode(() -> cheerTalkService.register(cheerTalkRequest))
                .doesNotThrowAnyException();
    }
}
