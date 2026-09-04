package com.sports.server.command.organization.domain;

import com.sports.server.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "organizations")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Organization extends BaseEntity<Organization> {

    @Column(name = "name", nullable = false)
    private String name;

    /**
     * 이 조직 선수의 학번 자리수. {@code null} 은 자리수를 하나로 정할 수 없다는 뜻이다.
     * 여러 학교가 한 조직으로 묶이는 대회가 그렇다.
     */
    @Column(name = "student_number_digits")
    private Integer studentNumberDigits = 9;

    @Column(name = "logo_image_url")
    private String logoImageUrl;
}
