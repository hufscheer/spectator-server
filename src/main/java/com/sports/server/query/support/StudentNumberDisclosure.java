package com.sports.server.query.support;

import com.sports.server.command.member.domain.Member;
import com.sports.server.command.player.domain.Player;
import com.sports.server.common.application.OrganizationAccessValidator;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 한 요청 안에서 선수의 전체 학번을 보여줄지 판정한다. 학교별 판정을 캐시해 목록에서 선수마다 쿼리가 나가지 않게 한다.
 * 한 번이라도 전체 학번을 보여줬다면 {@link #revealedAny()} 가 참이다(응답을 중간 캐시에 남기지 않기 위한 표시).
 */
public class StudentNumberDisclosure {

    private static final Long NO_ORGANIZATION = -1L;

    private final Optional<Member> viewer;
    private final OrganizationAccessValidator validator;
    private final Map<Long, Boolean> byOrganization = new HashMap<>();
    private boolean revealedAny = false;

    StudentNumberDisclosure(final Optional<Member> viewer, final OrganizationAccessValidator validator) {
        this.viewer = viewer;
        this.validator = validator;
    }

    public boolean canReveal(final Player player) {
        if (viewer.isEmpty()) {
            return false;
        }
        Long organizationId = player.getOrganization() == null ? NO_ORGANIZATION : player.getOrganization().getId();
        boolean reveal = byOrganization.computeIfAbsent(organizationId,
                id -> validator.canManage(viewer.get(), player));
        if (reveal && player.getStudentNumber() != null) {
            revealedAny = true;
        }
        return reveal;
    }

    /** 전체 학번이 담긴 응답은 중간 캐시가 비로그인 요청에 재사용하지 못하게 한다. 그 밖의 응답 헤더는 건드리지 않는다. */
    public static <T> ResponseEntity<T> respond(final T body, final StudentNumberDisclosure disclosure) {
        if (!disclosure.revealedAny()) {
            return ResponseEntity.ok(body);
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, "private")
                .header(HttpHeaders.VARY, "Cookie")
                .body(body);
    }

    public boolean revealedAny() {
        return revealedAny;
    }
}
