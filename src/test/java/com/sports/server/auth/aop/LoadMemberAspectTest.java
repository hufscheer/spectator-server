package com.sports.server.auth.aop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.sports.server.auth.exception.AuthorizationErrorMessages;
import com.sports.server.auth.utils.JwtUtil;
import com.sports.server.command.member.domain.Member;
import com.sports.server.command.member.domain.MemberRepository;
import com.sports.server.common.exception.UnauthorizedException;
import com.sports.server.support.TestSecurityConfig;
import com.sports.server.support.isolation.DatabaseIsolation;
import jakarta.servlet.http.Cookie;
import java.util.Date;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@SpringBootTest
@DatabaseIsolation
@Import(TestSecurityConfig.class)
@Sql(scripts = "/member-fixture.sql")
public class LoadMemberAspectTest {

    @Value("${cookie.name}")
    private String COOKIE_NAME;

    @Value("${jwt.secret-key}")
    private String secretKey;

    @Value("${jwt.valid-time}")
    private long TOKEN_VALID_TIME;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Mock
    private ProceedingJoinPoint proceedingJoinPoint;

    @Mock
    private MethodSignature methodSignature;

    private LoadMemberAspect loadMemberAspect;
    private MockHttpServletRequest request;
    private Member member;
    private String email;
    private Cookie cookie;
    private String accessToken;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        request = new MockHttpServletRequest();
        email = "john@example.com";
        member = memberRepository.findMemberByEmail(email).get();
        accessToken = jwtUtil.createAccessToken(member);
        cookie = new Cookie(COOKIE_NAME, accessToken);
        request.setCookies(cookie);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request)); // RequestContextHolder에 설정
        loadMemberAspect = new LoadMemberAspect(memberRepository);

        when(proceedingJoinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(this.getClass().getDeclaredMethods()[0]);
    }

    @Test
    void 검증에_성공한다() throws Throwable {
        when(proceedingJoinPoint.proceed(any())).thenReturn(member);
        Member resultMember = (Member) loadMemberAspect.authenticate(proceedingJoinPoint, null);

        assertNotNull(resultMember);
        assertEquals(email, resultMember.getEmail());
    }

    @Test
    void 존재하지_않는_회원일_경우_예외를_던진다() {
        String fakeToken = JWT.create()
                .withSubject("fake@gmail.com")
                .withExpiresAt(new Date(new Date().getTime() + TOKEN_VALID_TIME))
                .withClaim("id", 1L)
                .withClaim("email", "fake@gmail.com")
                .sign(Algorithm.HMAC512(secretKey));

        Cookie fakeCookie = new Cookie(COOKIE_NAME, fakeToken);
        request.setCookies(fakeCookie);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        UnauthorizedException exception = assertThrows(UnauthorizedException.class, () -> {
            loadMemberAspect.authenticate(proceedingJoinPoint, null);
        });
        assertEquals(AuthorizationErrorMessages.MEMBER_NOT_FOUND_EXCEPTION, exception.getMessage());
    }

    @Test
    void 유효하지_않은_토큰일_경우_예외를_던진다() {
        String invalidToken = accessToken + "invalid";
        Cookie invalidCookie = new Cookie(COOKIE_NAME, invalidToken);
        request.setCookies(invalidCookie);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        UnauthorizedException exception = assertThrows(UnauthorizedException.class, () -> {
            loadMemberAspect.authenticate(proceedingJoinPoint, null);
        });
        assertEquals(AuthorizationErrorMessages.INVALID_TOKEN_EXCEPTION, exception.getMessage());
    }

}
