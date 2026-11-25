package com.sports.server.query.support;

import com.sports.server.command.member.domain.Member;
import com.sports.server.common.application.OrganizationAccessValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class StudentNumberDisclosurePolicy {

    private final OrganizationAccessValidator organizationAccessValidator;

    public StudentNumberDisclosure forViewer(final Optional<Member> viewer) {
        return new StudentNumberDisclosure(viewer, organizationAccessValidator);
    }
}
