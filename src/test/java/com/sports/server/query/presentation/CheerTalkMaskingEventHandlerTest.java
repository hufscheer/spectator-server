package com.sports.server.query.presentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.sports.server.command.cheertalk.domain.CheerTalk;
import com.sports.server.command.cheertalk.domain.CheerTalkMaskingEvent;
import com.sports.server.common.application.EntityUtils;
import com.sports.server.common.dto.PageRequestDto;
import com.sports.server.common.infra.openrouter.OpenRouterChatCaller;
import com.sports.server.common.infra.openrouter.OpenRouterChatResponse;
import com.sports.server.query.application.CheerTalkQueryService;
import com.sports.server.query.dto.response.CheerTalkResponse;
import com.sports.server.support.ServiceTest;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;

@Sql("/cheer-talk-fixture.sql")
@TestPropertySource(properties = {"masking.provider=openrouter", "openrouter.api.key=test-key"})
class CheerTalkMaskingEventHandlerTest extends ServiceTest {

    private static final Long CHEER_TALK_ID = 2L;
    private static final Long GAME_ID = 1L;

    @Autowired
    private CheerTalkMaskingEventHandler handler;

    @Autowired
    private EntityUtils entityUtils;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private CheerTalkQueryService cheerTalkQueryService;

    @MockBean
    private OpenRouterChatCaller chatCaller;

    @Test
    @DisplayName("마스킹 모델이 내용을 바꾸면 응원톡을 차단하고 이력을 남긴다")
    void 마스킹되면_차단_이력_저장() {
        // given
        String content = "씨발 잘한다";
        updateContent(content);
        given(chatCaller.call(any(), any())).willReturn(response("** 잘한다"));

        // when
        handler.handle(new CheerTalkMaskingEvent(entityUtils.getEntity(CHEER_TALK_ID, CheerTalk.class), GAME_ID));

        // then
        assertThat(blockStatus()).isEqualTo("BLOCKED_BY_BOT");
        assertThat(content()).isEqualTo("** 잘한다");
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT cheer_talk_bot_filter_result, bot_type FROM cheer_talk_bot_filter_history WHERE cheer_talk_id = ?",
                CHEER_TALK_ID);
        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).get("cheer_talk_bot_filter_result")).isEqualTo("ABUSIVE");
        assertThat(rows.get(0).get("bot_type")).isEqualTo("OPENROUTER");
    }

    @Test
    @DisplayName("마스킹 모델이 내용을 바꾸지 않으면 차단하지 않고 이력도 남기지 않는다")
    void 마스킹_안되면_그대로() {
        // given
        String content = "오늘 경기 재밌다";
        updateContent(content);
        given(chatCaller.call(any(), any())).willReturn(response(content));

        // when
        handler.handle(new CheerTalkMaskingEvent(entityUtils.getEntity(CHEER_TALK_ID, CheerTalk.class), GAME_ID));

        // then
        assertThat(blockStatus()).isEqualTo("ACTIVE");
        assertThat(content()).isEqualTo(content);
        assertThat(historyCount()).isZero();
    }

    @Test
    @DisplayName("마스킹 호출이 실패해도 예외 없이 그대로 두고 이력도 남기지 않는다")
    void 마스킹_호출_실패시_그대로() {
        // given
        String content = "이건 호출 실패 케이스다";
        updateContent(content);
        given(chatCaller.call(any(), any())).willThrow(new RuntimeException("boom"));
        CheerTalk cheerTalk = entityUtils.getEntity(CHEER_TALK_ID, CheerTalk.class);

        // when & then
        assertThatCode(() -> handler.handle(new CheerTalkMaskingEvent(cheerTalk, GAME_ID)))
                .doesNotThrowAnyException();
        assertThat(blockStatus()).isEqualTo("ACTIVE");
        assertThat(content()).isEqualTo(content);
        assertThat(historyCount()).isZero();
    }

    @Test
    @DisplayName("차단된 응원톡은 관중 조회에서 내용이 가려진다")
    void 차단된_응원톡_관중_조회에서_숨김() {
        // given
        updateContent("씨발 잘한다 진짜");
        given(chatCaller.call(any(), any())).willReturn(response("** 잘한다 진짜"));
        handler.handle(new CheerTalkMaskingEvent(entityUtils.getEntity(CHEER_TALK_ID, CheerTalk.class), GAME_ID));

        // when
        List<CheerTalkResponse.ForSpectator> responses = cheerTalkQueryService
                .getCheerTalksByGameId(GAME_ID, new PageRequestDto(null, 50)).content();

        // then
        CheerTalkResponse.ForSpectator blocked = responses.stream()
                .filter(r -> r.cheerTalkId().equals(CHEER_TALK_ID))
                .findFirst().orElseThrow();
        assertThat(blocked.isBlocked()).isTrue();
        assertThat(blocked.content()).isNull();
    }

    private void updateContent(String content) {
        jdbcTemplate.update("UPDATE cheer_talks SET content = ? WHERE id = ?", content, CHEER_TALK_ID);
    }

    private String blockStatus() {
        return jdbcTemplate.queryForObject(
                "SELECT block_status FROM cheer_talks WHERE id = ?", String.class, CHEER_TALK_ID);
    }

    private String content() {
        return jdbcTemplate.queryForObject(
                "SELECT content FROM cheer_talks WHERE id = ?", String.class, CHEER_TALK_ID);
    }

    private int historyCount() {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM cheer_talk_bot_filter_history WHERE cheer_talk_id = ?",
                Integer.class, CHEER_TALK_ID);
    }

    private OpenRouterChatResponse response(String text) {
        return new OpenRouterChatResponse(List.of(
                new OpenRouterChatResponse.Choice(new OpenRouterChatResponse.Message(text, null))));
    }
}
