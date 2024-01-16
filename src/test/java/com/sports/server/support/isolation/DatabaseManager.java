package com.sports.server.support.isolation;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.metamodel.EntityType;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@Transactional
public class DatabaseManager {

    private final EntityManager entityManager;
    private final List<String> tableNames;

    public DatabaseManager(EntityManager entityManager) {
        this.entityManager = entityManager;
        this.tableNames = extractTableNames();
    }

    private List<String> extractTableNames() {
        return entityManager.getMetamodel().getEntities().stream()
                .filter(this::isEntity)
                .map(this::convertCamelToSnake)
                .toList();
    }

    private boolean isEntity(EntityType<?> entityType) {
        return entityType.getJavaType().getAnnotation(Entity.class) != null;
    }

    private String convertCamelToSnake(EntityType<?> entityType) {
        String regex = "([a-z])([A-Z]+)";
        String replacement = "$1_$2";
        return entityType.getName()
                .replaceAll(regex, replacement)
                .toLowerCase() + "s";
    }

    public void truncateTables() {
        entityManager.createNativeQuery("SET REFERENTIAL_INTEGRITY FALSE").executeUpdate();
        for (String tableName : tableNames) {
            entityManager.createNativeQuery("TRUNCATE TABLE " + tableName).executeUpdate();
        }
        entityManager.createNativeQuery("SET REFERENTIAL_INTEGRITY TRUE").executeUpdate();
    }
}
