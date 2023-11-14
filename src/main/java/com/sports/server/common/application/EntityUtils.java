package com.sports.server.common.application;

import com.sports.server.common.exception.NotFoundException;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class EntityUtils {

    private static final String MESSAGE = "을(를) 찾을 수 없습니다";

    private final EntityManager entityManager;

    public <T> T getEntity(Long id, Class<T> entityType) {
        return Optional.ofNullable(entityManager.find(entityType, id))
                .orElseThrow(() -> new NotFoundException(entityType.getSimpleName() + MESSAGE));
    }
}
