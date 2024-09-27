package com.sports.server.common.application;

import com.sports.server.command.member.domain.Member;
import com.sports.server.common.domain.ManagedEntity;
import com.sports.server.common.exception.UnauthorizedException;

public class PermissionValidator {

    public static <T extends ManagedEntity> void checkPermission(final T entity, final Member manager) {
        if (!entity.isManagedBy(manager)) {
            throw new UnauthorizedException(
                    manager.getEmail() + " 은 " + entity.getClass() + " 에 접근할 권한이 존재하지 않습니다.");
        }
    }
}
