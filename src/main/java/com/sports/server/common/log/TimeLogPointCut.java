package com.sports.server.common.log;

import org.aspectj.lang.annotation.Pointcut;

public class TimeLogPointCut {

    @Pointcut("( commandPackagePointCut() || queryPackagePointCut()) " +
            "&& ( componentPointCut() || servicePointCut() || repositoryPointCut() )")
    public void timeLogPointCut() {}

    @Pointcut("execution(* com.sports.server.command..*.*(..))")
    private void commandPackagePointCut() {}

    @Pointcut("execution(* com.sports.server.query..*.*(..))")
    private void queryPackagePointCut() {}

    @Pointcut("@target(org.springframework.stereotype.Service)")
    private void servicePointCut() {}

    @Pointcut("@target(org.springframework.stereotype.Repository)")
    private void repositoryPointCut() {}

    @Pointcut("@target(org.springframework.stereotype.Component)")
    private void componentPointCut() {}
}
