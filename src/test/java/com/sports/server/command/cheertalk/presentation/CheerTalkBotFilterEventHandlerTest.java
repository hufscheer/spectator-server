package com.sports.server.command.cheertalk.presentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sports.server.command.cheertalk.domain.BotType;
import com.sports.server.command.cheertalk.domain.CheerTalk;
import com.sports.server.command.cheertalk.domain.CheerTalkBlockStatus;
import com.sports.server.command.cheertalk.domain.CheerTalkCreateEvent;
import com.sports.server.command.cheertalk.infra.KorUnsmileClient;
import com.sports.server.common.application.EntityUtils;
import com.sports.server.support.ServiceTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;

@Sql("/cheer-talk-fixture.sql")
public class CheerTalkBotFilterEventHandlerTest extends ServiceTest {

    private static final Long CHEER_TALK_ID = 2L;
    private static final Long GAME_ID = 1L;

    @Autowired
    private CheerTalkBotFilterEventHandler eventHandler;

    @Autowired
    private EntityUtils entityUtils;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private KorUnsmileClient korUnsmileClient;

    @Test
    void 봇이_악성으로_판정한_응원톡은_BLOCKED_BY_BOT으로_차단된다() throws Exception {
        // given
        given(korUnsmileClient.supports()).willReturn(BotType.KOR_UNSMILE);
        given(korUnsmileClient.detectAbusiveContent(any())).willReturn(objectMapper.readTree(
                "[[{\"label\":\"악플/욕설\",\"score\":0.98},{\"label\":\"clean\",\"score\":0.01}]]"));
        CheerTalk cheerTalk = entityUtils.getEntity(CHEER_TALK_ID, CheerTalk.class);

        // when
        eventHandler.handle(new CheerTalkCreateEvent(cheerTalk, GAME_ID));

        // then
        String blockStatus = jdbcTemplate.queryForObject(
                "SELECT block_status FROM cheer_talks WHERE id = ?", String.class, CHEER_TALK_ID);
        assertThat(blockStatus).isEqualTo(CheerTalkBlockStatus.BLOCKED_BY_BOT.name());
    }
}
