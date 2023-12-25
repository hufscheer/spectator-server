package com.sports.server.common.log;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

@Slf4j
@Component
public class TimeLogTemplate {

    public <V> V execute(ThrowableCallable<V> callable, String target) throws Throwable {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        try {
            return callable.call();
        } finally {
            logTime(target, stopWatch);
        }
    }

    private void logTime(String target, StopWatch stopWatch) {
        stopWatch.stop();
        log.info("{} 실행 시간 : {}ms", target, stopWatch.getTotalTimeMillis());
    }
}
