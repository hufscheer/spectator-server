package com.sports.server.command.sport.domain;

import com.sports.server.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Entity
@Table(name = "sports")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Sport extends BaseEntity<Sport> {

    private static Integer AFTER_START_QUARTER_ORDER = 2;

    @Column(name = "name", nullable = false)
    private String name;

    @OneToMany(mappedBy = "sport")
    private List<Quarter> quarters = new ArrayList<>();

    public Quarter getAfterStartQuarter() {
        return quarters.stream()
                .filter(quarter -> quarter.isOrder(AFTER_START_QUARTER_ORDER))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("경기 시작 후의 쿼터가 존재하지 않습니다."));
    }

    public Quarter getEndQuarter() {
        return quarters.stream()
                .max(Comparator.comparing(Quarter::getOrder))
                .orElseThrow(() -> new IllegalArgumentException("최종 쿼터가 존재하지 않습니다."));
    }
}
