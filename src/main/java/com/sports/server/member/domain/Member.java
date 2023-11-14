package com.sports.server.member.domain;

import com.sports.server.common.domain.BaseEntity;
import com.sports.server.organization.domain.Organization;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Entity
@Table(name = "members")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseEntity<Member> {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-z0-9._-]+@[a-z]+[.]+[a-z]{2,3}$");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!]).{8,}$");

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    private Organization organization;

    @Email
    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "is_administrator", nullable = false)
    private boolean isAdministrator;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    public Member(String email, String password, boolean isAdministrator) {
        validateEmail(email);
        validatePassword(password);
        this.email = email;
        this.password = password;
        this.isAdministrator = isAdministrator;
    }

    private void validateEmail(final String email) {
        Matcher matcher = EMAIL_PATTERN.matcher(email);
        if (!matcher.matches()) {
            // TODO: 예외 던지기
        }
    }

    private void validatePassword(final String password) {
        Matcher matcher = PASSWORD_PATTERN.matcher(password);
        if (!matcher.matches()) {
            // TODO: 예외 던지기
        }
    }
}
