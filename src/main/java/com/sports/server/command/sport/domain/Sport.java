package com.sports.server.command.sport.domain;

import com.sports.server.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "sports")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Sport extends BaseEntity<Sport> {

    @Column(name = "name", nullable = false)
    private String name;
}
