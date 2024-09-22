package com.sports.server.common.application;

import com.sports.server.auth.exception.AuthorizationErrorMessages;
import com.sports.server.command.member.domain.Member;
import com.sports.server.common.domain.ManagedEntity;
import com.sports.server.common.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PermissionValidator {

    private final EntityUtils entityUtils;

    public <T extends ManagedEntity> void checkPermission(final Long id, final Member manager,
                                                          final Class<T> entityType) {
        T target = entityUtils.getEntity(id, entityType);

        if (!target.isManagedBy(manager)) {
            throw new UnauthorizedException(AuthorizationErrorMessages.PERMISSION_DENIED);
        }
    }

    public <T extends ManagedEntity> T checkPermissionAndGet(final Long id, final Member manager,
                                                             final Class<T> entityType) {
        T target = entityUtils.getEntity(id, entityType);

        if (!target.isManagedBy(manager)) {
            throw new UnauthorizedException(AuthorizationErrorMessages.PERMISSION_DENIED);
        }

        return target;
    }
}
