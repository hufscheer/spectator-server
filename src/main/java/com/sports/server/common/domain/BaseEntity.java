package com.sports.server.common.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.hibernate.proxy.HibernateProxy;
import org.springframework.data.domain.AbstractAggregateRoot;

import java.util.Objects;

@MappedSuperclass
@Getter
public class BaseEntity<T extends AbstractAggregateRoot<T>> extends AbstractAggregateRoot<T> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 지연 로딩된 연관은 Hibernate 프록시다. 프록시는 클래스가 다르고(Xxx$HibernateProxy) 필드도 비어 있어서
     * getClass()·필드로 비교하면 같은 엔티티를 다르다고 본다 (#715). 프록시를 초기화하지 않고 비교한다.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BaseEntity<?> that)) return false;

        Object id = identifierOf(this);
        return id != null
                && id.equals(identifierOf(that))
                && rootEntityClass(this) == rootEntityClass(that);
    }

    private static Object identifierOf(BaseEntity<?> entity) {
        if (entity instanceof HibernateProxy proxy) {
            return proxy.getHibernateLazyInitializer().getIdentifier();
        }
        return entity.id;
    }

    // 상속 엔티티(타임라인)는 한 테이블이라 상위 타입 프록시로 올 수 있다. 최상위 엔티티 클래스로 맞춘다
    private static Class<?> rootEntityClass(Object entity) {
        Class<?> type = entity instanceof HibernateProxy proxy
                ? proxy.getHibernateLazyInitializer().getPersistentClass()
                : entity.getClass();
        while (type.getSuperclass() != null && type.getSuperclass().isAnnotationPresent(Entity.class)) {
            type = type.getSuperclass();
        }
        return type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
