package com.sports.server.command.member.domain;

import com.sports.server.command.organization.domain.Organization;
import com.sports.server.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "members")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseEntity<Member> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    private Organization organization;

    @Email
    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "is_manager", nullable = false)
    private boolean isManager;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    public Member(String email, String password) {
        this.email = email;
        this.password = password;
        this.isManager = true;
    }

    public Member(String email, String password, boolean isManager) {
        this.email = email;
        this.password = password;
        this.isManager = isManager;
    }

}
