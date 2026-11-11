package com.sports.server.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import com.sports.server.command.cheertalk.application.AiSeedScheduler;
import com.sports.server.command.cheertalk.application.AiSeedService;
import com.sports.server.command.cheertalk.infra.AiSeedMessageGenerator;
import com.sports.server.command.cheertalk.infra.CachingMaskingClient;
import com.sports.server.command.cheertalk.infra.OpenRouterMaskingClient;
import com.sports.server.command.cheertalk.presentation.AiSeedEventListener;
import com.sports.server.command.nl.infra.NlOpenRouterClient;
import com.sports.server.command.timeline.domain.TimelineCreatedEvent;
import com.sports.server.command.timeline.domain.TimelineType;
import com.sports.server.common.infra.openrouter.OpenRouterWebClientConfig;
import com.sports.server.support.ServiceTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import java.time.Instant;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.test.context.TestPropertySource;

/**
 * dev/prod(be-config)에서만 켜지는 조건부 빈은 기본 테스트 컨텍스트에서 만들어지지 않는다.
 * 같은 값으로 컨텍스트를 띄워, 기동 때 검증(예: @TransactionalEventListener 와 @Transactional 조합)에서 깨지지 않는지 본다.
 * be-config 에 조건 값이 새로 생기면 여기에도 더한다.
 */
@TestPropertySource(properties = {
        "ai-seed.provider=openrouter",
        "masking.provider=openrouter",
        "nl.provider=openrouter",
        "openrouter.api.key=test-key",
        "openrouter.api.base-url=http://127.0.0.1:9",
        "ai-seed.prompt.scheduled=p",
        "ai-seed.prompt.second-half=p",
        "ai-seed.prompt.goal=p",
        "ai-seed.prompt.own-goal=p"
})
@Sql("/timeline-fixture.sql")
class DevProdConditionalBeansContextTest extends ServiceTest {

    @Autowired
    private ApplicationContext context;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockitoBean(name = "taskScheduler")
    private TaskScheduler taskScheduler;

    @Test
    @DisplayName("dev/prod 와 같은 provider 설정으로 컨텍스트가 뜨고 조건부 빈이 모두 만들어진다")
    void dev_prod_조건부_빈으로_컨텍스트가_뜬다() {
        assertThat(context.getBeanNamesForType(AiSeedEventListener.class)).hasSize(1);
        assertThat(context.getBeanNamesForType(AiSeedScheduler.class)).hasSize(1);
        assertThat(context.getBeanNamesForType(AiSeedService.class)).hasSize(1);
        assertThat(context.getBeanNamesForType(AiSeedMessageGenerator.class)).hasSize(1);
        assertThat(context.getBeanNamesForType(OpenRouterMaskingClient.class)).hasSize(1);
        assertThat(context.getBeanNamesForType(CachingMaskingClient.class)).hasSize(1);
        assertThat(context.getBeanNamesForType(NlOpenRouterClient.class)).hasSize(1);
        assertThat(context.getBeanNamesForType(OpenRouterWebClientConfig.class)).hasSize(1);
    }

    @Test
    @DisplayName("커밋 뒤 AiSeedEventListener 가 득점 타임라인의 득점자를 지연 로딩해 응원톡을 예약한다")
    void 커밋_뒤_득점_이벤트로_응원톡을_예약한다() {
        Long scoreTimelineId = jdbcTemplate.queryForObject(
                "SELECT id FROM timelines WHERE type = 'SCORE' AND game_id = 1 ORDER BY id LIMIT 1", Long.class);

        new TransactionTemplate(transactionManager).executeWithoutResult(status ->
                eventPublisher.publishEvent(new TimelineCreatedEvent(scoreTimelineId, 1L, TimelineType.SCORE)));

        verify(taskScheduler).schedule(any(Runnable.class), any(Instant.class));
    }
}
