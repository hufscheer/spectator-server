package com.sports.server.common.application;

import com.sports.server.command.member.domain.Member;
import com.sports.server.common.domain.ManagedEntity;
import com.sports.server.auth.exception.AuthorizationErrorMessages;
import com.sports.server.common.exception.ForbiddenException;
import com.sports.server.common.exception.UnauthorizedException;

public class PermissionValidator {

    public static <T extends ManagedEntity> void checkPermission(final T entity, final Member manager) {
        if (!entity.isManagedBy(manager)) {
            throw new UnauthorizedException(
                    manager.getEmail() + " 은 " + entity.getClass() + " 에 접근할 권한이 존재하지 않습니다.");
        }
    }

    /**
     * checkPermission 과 같은 판정이지만 거부를 403 으로 낸다. 이번에 새로 거는 소유 검사는 이것을 쓴다.
     * 기존 checkPermission 의 401 은 클라이언트 계약이라 그대로 둔다.
     */
    public static <T extends ManagedEntity> void checkOwnership(final T entity, final Member manager) {
        if (!entity.isManagedBy(manager)) {
            throw new ForbiddenException(AuthorizationErrorMessages.PERMISSION_DENIED);
        }
    }
}
