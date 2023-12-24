package com.sports.server.common.log;

import org.aspectj.lang.annotation.Pointcut;

public class TimeLogPointCut {

    @Pointcut("packagePointCut() && ( servicePointCut() || repositoryPointCut() )")
    public void timeLogPointCut() {}

    @Pointcut("execution(* com.sports.server..*.*(..))")
    private void packagePointCut() {}

    @Pointcut("@target(org.springframework.stereotype.Service)")
    private void servicePointCut() {}

    @Pointcut("@target(org.springframework.stereotype.Repository)")
    private void repositoryPointCut() {}
}
