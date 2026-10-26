package com.sports.server.common.config;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionHandler;

@Slf4j
@Configuration
@EnableAsync
public class AsyncConfig {

    private static final int CORE_POOL_SIZE = 5;
    private static final int MAX_POOL_SIZE = 10;
    private static final int QUEUE_CAPACITY = 10;
    private static final String CUSTOM_THREAD_NAME_PREFIX = "ASYNC_THREAD-";
    private static final int PUSH_CORE_POOL_SIZE = 2;
    private static final int PUSH_MAX_POOL_SIZE = 4;
    // 대기 작업은 아이디만 들고 있어 가볍다
    private static final int PUSH_QUEUE_CAPACITY = 200;
    private static final String PUSH_THREAD_NAME_PREFIX = "PUSH_THREAD-";
    private static final TaskDecorator MDC_TASK_DECORATOR = runnable -> {
        Map<String, String> contextMap = MDC.getCopyOfContextMap();
        return () -> {
            MDC.setContextMap(contextMap);
            runnable.run();
        };
    };

    @Bean(name = "asyncThreadPool")
    public Executor asyncThreadPool() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(CORE_POOL_SIZE);
        taskExecutor.setMaxPoolSize(MAX_POOL_SIZE);
        taskExecutor.setQueueCapacity(QUEUE_CAPACITY);
        taskExecutor.setThreadNamePrefix(CUSTOM_THREAD_NAME_PREFIX);
        taskExecutor.setTaskDecorator(MDC_TASK_DECORATOR);
        taskExecutor.initialize();
        return taskExecutor;
    }

    // 푸시 전송은 한 건에 최대 2분까지 스레드를 잡으므로 다른 비동기 핸들러와 풀을 나눈다
    @Bean(name = "pushThreadPool")
    public Executor pushThreadPool() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(PUSH_CORE_POOL_SIZE);
        taskExecutor.setMaxPoolSize(PUSH_MAX_POOL_SIZE);
        taskExecutor.setQueueCapacity(PUSH_QUEUE_CAPACITY);
        taskExecutor.setThreadNamePrefix(PUSH_THREAD_NAME_PREFIX);
        taskExecutor.setTaskDecorator(MDC_TASK_DECORATOR);
        taskExecutor.setRejectedExecutionHandler(discardWithWarn());
        taskExecutor.initialize();
        return taskExecutor;
    }

    // 호출 스레드는 커밋 직후의 요청 스레드라서, 막지 않고 알림을 버린다
    private RejectedExecutionHandler discardWithWarn() {
        return (runnable, executor) ->
                log.warn("푸시 대기열이 가득 차 알림을 버립니다: active={}, queued={}",
                        executor.getActiveCount(), executor.getQueue().size());
    }
}
