package com.sports.server.auth.aop;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.sports.server.auth.exception.AuthorizationErrorMessages;
import com.sports.server.command.member.domain.Member;
import com.sports.server.command.member.domain.MemberRepository;
import com.sports.server.common.exception.UnauthorizedException;
import com.sports.server.support.TestSecurityConfig;
import com.sports.server.support.isolation.DatabaseIsolation;
import java.lang.reflect.Method;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.jdbc.Sql;

@SpringBootTest
@DatabaseIsolation
@Import(TestSecurityConfig.class)
@Sql(scripts = "/member-fixture.sql")
public class LoadMemberAspectTest {

    @Autowired
    private MemberRepository memberRepository;

    @Mock
    private ProceedingJoinPoint proceedingJoinPoint;

    @Mock
    private MethodSignature methodSignature;

    private LoadMemberAspect loadMemberAspect;
    private MockHttpServletRequest request;
    private String email;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        request = new MockHttpServletRequest();
        email = "john@example.com";
        loadMemberAspect = new LoadMemberAspect(memberRepository);

        when(proceedingJoinPoint.getSignature()).thenReturn(methodSignature);
    }

    @Test
    void 검증에_성공한다() throws Throwable {
        setSecurityContext(email);
        setupProceedingJoinPoint("testMethodWithMember", Member.class);

        Object result = loadMemberAspect.authenticate(proceedingJoinPoint, null);
        Member resultMember = (Member) result;

        assertNotNull(resultMember);
        assertEquals(email, resultMember.getEmail());
    }

    @Test
    void 존재하지_않는_회원일_경우_예외를_던진다() throws Throwable {
        String fakeEmail = "fake@gmail.com";
        setSecurityContext(fakeEmail);
        setupProceedingJoinPoint("testMethodWithMember", Member.class);

        assertThatThrownBy(
                () -> loadMemberAspect.authenticate(proceedingJoinPoint, null)
        ).isInstanceOf(UnauthorizedException.class)
                .hasMessage(AuthorizationErrorMessages.MEMBER_NOT_FOUND_EXCEPTION);

    }

    @Test
    void SecurityContextHolder에_객체가_존재하지_않는_경우_예외를_던진다() throws Throwable {
        SecurityContextHolder.clearContext();
        setupProceedingJoinPoint("testMethodWithMember", Member.class);

        assertThatThrownBy(
                () -> loadMemberAspect.authenticate(proceedingJoinPoint, null)
        ).isInstanceOf(UnauthorizedException.class)
                .hasMessage(AuthorizationErrorMessages.INVALID_COOKIE_EXCEPTION);
    }

    private void setSecurityContext(final String email) {
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(new TestingAuthenticationToken(email, null));
        SecurityContextHolder.setContext(securityContext);
    }

    private void setupProceedingJoinPoint(String methodName, Class<?>... parameterTypes) throws Throwable {
        Method testMethod = this.getClass().getDeclaredMethod(methodName, parameterTypes);
        when(methodSignature.getMethod()).thenReturn(testMethod);
        when(proceedingJoinPoint.getArgs()).thenReturn(new Object[]{null});
        when(proceedingJoinPoint.proceed(any())).thenAnswer(invocation -> {
            Object[] args = invocation.getArgument(0);
            return args[0];
        });
    }

    public void testMethodWithMember(Member member) {
        // 테스트를 위한 메서드
    }
}
