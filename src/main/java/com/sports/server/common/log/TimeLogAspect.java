package com.sports.server.common.log;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

@Aspect
@Component
@Slf4j
public class TimeLogAspect {

    @Around("com.sports.server.common.log.TimeLogPointCut.timeLogPointCut()")
    public Object execute(ProceedingJoinPoint joinPoint) throws Throwable {
        StopWatch stopWatch = new StopWatch();
        return proceedWithTimeCheck(joinPoint, stopWatch);
    }

    private Object proceedWithTimeCheck(ProceedingJoinPoint joinPoint, StopWatch stopWatch) throws Throwable {
        stopWatch.start();
        Object result;
        try {
            result = joinPoint.proceed();
            return result;
        } finally {
            logTime(joinPoint, stopWatch);
        }
    }

    private void logTime(ProceedingJoinPoint joinPoint, StopWatch stopWatch) {
        stopWatch.stop();
        log.info("{} 실행 시간 : {}ms", generateTargetName(joinPoint), stopWatch.getTotalTimeMillis());
    }

    private String generateTargetName(ProceedingJoinPoint joinPoint) {
        String targetClassName = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        return targetClassName + "." + methodName;
    }
}
