package com.sports.server.common.domain;

import com.sports.server.command.member.domain.Member;

public interface ManagedEntity {
    boolean isManagedBy(Member manager);
}
