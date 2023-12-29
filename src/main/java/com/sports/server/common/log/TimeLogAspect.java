package com.sports.server.common.log;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class TimeLogAspect {

    private final TimeLogTemplate timeLogTemplate;

    @Around("com.sports.server.common.log.TimeLogPointCut.timeLogPointCut()")
    public Object execute(ProceedingJoinPoint joinPoint) throws Throwable {
        return timeLogTemplate.executeWithResult(
                joinPoint::proceed,
                generateTargetName(joinPoint)
        );
    }

    private String generateTargetName(ProceedingJoinPoint joinPoint) {
        String targetClassName = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        return targetClassName + "." + methodName;
    }
}
