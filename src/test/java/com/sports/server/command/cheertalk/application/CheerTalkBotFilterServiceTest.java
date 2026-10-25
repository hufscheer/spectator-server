package com.sports.server.command.cheertalk.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sports.server.command.cheertalk.domain.BotType;
import com.sports.server.command.cheertalk.domain.CheerTalkBotFilterResult;
import com.sports.server.command.cheertalk.infra.KorUnsmileClient;
import com.sports.server.support.ServiceTest;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;

@Sql("/cheer-talk-fixture.sql")
public class CheerTalkBotFilterServiceTest extends ServiceTest {

    private static final Long CHEER_TALK_ID = 2L;

    @Autowired
    private CheerTalkBotFilterService cheerTalkBotFilterService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private KorUnsmileClient korUnsmileClient;

    @Test
    void 악성_응원톡을_필터링하면_ABUSIVE를_반환하고_필터링_이력을_저장한다() throws Exception {
        // given
        stubBot("[[{\"label\":\"악플/욕설\",\"score\":0.98},{\"label\":\"clean\",\"score\":0.01}]]");

        // when
        CheerTalkBotFilterResult result = cheerTalkBotFilterService.filterByBot("나쁜말", CHEER_TALK_ID);

        // then
        assertThat(result).isEqualTo(CheerTalkBotFilterResult.ABUSIVE);
        assertHistory(CheerTalkBotFilterResult.ABUSIVE);
    }

    @Test
    void 정상_응원톡을_필터링하면_CLEAN을_반환하고_필터링_이력을_저장한다() throws Exception {
        // given
        stubBot("[[{\"label\":\"clean\",\"score\":0.97},{\"label\":\"악플/욕설\",\"score\":0.01}]]");

        // when
        CheerTalkBotFilterResult result = cheerTalkBotFilterService.filterByBot("화이팅", CHEER_TALK_ID);

        // then
        assertThat(result).isEqualTo(CheerTalkBotFilterResult.CLEAN);
        assertHistory(CheerTalkBotFilterResult.CLEAN);
    }

    private void stubBot(String json) throws Exception {
        JsonNode response = objectMapper.readTree(json);
        given(korUnsmileClient.supports()).willReturn(BotType.KOR_UNSMILE);
        given(korUnsmileClient.detectAbusiveContent(any())).willReturn(response);
    }

    private void assertHistory(CheerTalkBotFilterResult expected) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT cheer_talk_bot_filter_result, bot_type FROM cheer_talk_bot_filter_history WHERE cheer_talk_id = ?",
                CHEER_TALK_ID);

        assertThat(rows).hasSize(1);
        assertThat(rows.get(0))
                .containsEntry("cheer_talk_bot_filter_result", expected.name())
                .containsEntry("bot_type", BotType.KOR_UNSMILE.name());
    }
}
