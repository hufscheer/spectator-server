package com.sports.server.auth.jwt;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.sports.server.auth.exception.AuthorizationErrorMessages;
import com.sports.server.command.member.domain.Member;
import com.sports.server.command.member.domain.MemberRepository;
import com.sports.server.common.exception.UnauthorizedException;
import com.sports.server.support.TestSecurityConfig;
import com.sports.server.support.isolation.DatabaseIsolation;
import java.util.Date;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;

@SpringBootTest
@DatabaseIsolation
@Import(TestSecurityConfig.class)
@Sql(scripts = "/member-fixture.sql")
public class JwtProviderTest {

    @Autowired
    private JwtProvider jwtProvider;

    @Autowired
    private MemberRepository memberRepository;

    private Member member;

    private final String secretKey = "secret";

    private final Long tokenValidTime = 36000L;

    private final String emailOfMember = "john@example.com";

    @BeforeEach
    public void setUp() {
        member = memberRepository.findMemberByEmail(emailOfMember).get();
    }

    @Test
    public void 올바른_엑세스_토큰을_생성한다() {
        String token = jwtProvider.createAccessToken(member);
        assertNotNull(token);

        String email = JWT.require(Algorithm.HMAC512(secretKey)).build().verify(token).getClaim("email").asString();
        assertEquals(member.getEmail(), email);
    }

    @Test
    public void 유효한_토큰을_검증한다() {
        String token = jwtProvider.createAccessToken(member);
        assertDoesNotThrow(() -> jwtProvider.validateToken(token));
    }

    @Test
    public void 만료된_토큰을_검증하면_예외가_발생한다() throws InterruptedException {
        long shortValidityTime = 1000L;

        // JWtProvider 의 createAccessToken 메서드와 동일
        Date now = new Date();
        String token = JWT.create()
                .withSubject(member.getEmail())
                .withExpiresAt(new Date(now.getTime() + shortValidityTime))
                .withClaim("id", member.getId())
                .withClaim("email", member.getEmail())
                .sign(Algorithm.HMAC512(secretKey));

        // 토큰 만료까지 대기
        Thread.sleep(2000);

        Exception exception = assertThrows(UnauthorizedException.class, () -> jwtProvider.validateToken(token));
        assertEquals(AuthorizationErrorMessages.TOKEN_EXPIRED_EXCEPTION, exception.getMessage());
    }

    @Test
    public void 클레임이_없는_토큰을_검증하면_예외가_발생한다() {
        String tokenWithoutClaims = JWT.create()
                .withExpiresAt(new Date(new Date().getTime() + tokenValidTime))
                .sign(Algorithm.HMAC512(secretKey));

        Exception exception = assertThrows(UnauthorizedException.class,
                () -> jwtProvider.validateToken(tokenWithoutClaims));
        assertEquals(AuthorizationErrorMessages.INVALID_TOKEN_EXCEPTION, exception.getMessage());
    }

    @Test
    public void 유효하지_않은_시크릿_키로_토큰을_생성한_경우_검증하면_예외가_발생한다() {
        Date now = new Date();
        String token = JWT.create()
                .withSubject(member.getEmail())
                .withExpiresAt(new Date(now.getTime() + tokenValidTime))
                .withClaim("id", member.getId())
                .withClaim("email", member.getEmail())
                .sign(Algorithm.HMAC512("invalidSecretKey"));

        Exception exception = assertThrows(UnauthorizedException.class,
                () -> jwtProvider.validateToken(token));
        assertEquals(AuthorizationErrorMessages.INVALID_TOKEN_EXCEPTION, exception.getMessage());
    }

    @Test
    public void 잘못된_토큰을_검증하면_예외가_발생한다() {
        String invalidToken = "invalid.token.here";

        Exception exception = assertThrows(UnauthorizedException.class, () -> jwtProvider.validateToken(invalidToken));
        assertEquals(AuthorizationErrorMessages.INVALID_TOKEN_EXCEPTION, exception.getMessage());
    }

//    @Test
//    public void 유효한_토큰을_통해_Authentication_객체를_올바르게_가져온다() {
//        String token = JWT.create().withSubject(this.member.getEmail())
//                .withExpiresAt(new Date(new Date().getTime() + tokenValidTime))
//                .withClaim("id", this.member.getId())
//                .withClaim("email", this.member.getEmail())
//                .sign(Algorithm.HMAC512(secretKey));
//
//        Authentication authentication = jwtProvider.getAuthentication(token);
//        assertNotNull(authentication);
//        assertEquals(emailOfMember, ((MemberDetails) authentication.getPrincipal()).getUsername());
//    }

//    @Test
//    public void 유효하지_않은_토큰인_경우_예외를_던진다() {
//        String invalidToken = "invalid.token.here";
//
//        UnauthorizedException exception = assertThrows(UnauthorizedException.class, () -> {
//            jwtProvider.getAuthentication(invalidToken);
//        });
//        assertEquals(AuthorizationErrorMessages.INVALID_TOKEN_EXCEPTION, exception.getMessage());
//    }
//
//    @Test
//    public void 존재하지_않는_회원인_경우_예외를_던진다() {
//        String token = JWT.create().withSubject("non-existing@example.com")
//                .withExpiresAt(new Date(new Date().getTime() + tokenValidTime))
//                .withClaim("email", "non-existing@example.com")
//                .sign(Algorithm.HMAC512(secretKey));
//
//        UnauthorizedException exception = assertThrows(UnauthorizedException.class, () -> {
//            jwtProvider.getAuthentication(token);
//        });
//        assertEquals(AuthorizationErrorMessages.MEMBER_NOT_FOUND_EXCEPTION, exception.getMessage());
//    }
//
//    @Test
//    public void 유효_기간이_지난_토큰인_경우_예외를_던진다() {
//        String expiredToken = JWT.create().withSubject(member.getEmail())
//                .withExpiresAt(new Date(new Date().getTime() - 1000))
//                .withClaim("id", member.getId())
//                .withClaim("email", member.getEmail())
//                .sign(Algorithm.HMAC512(secretKey));
//
//        UnauthorizedException exception = assertThrows(UnauthorizedException.class, () -> {
//            jwtProvider.getAuthentication(expiredToken);
//        });
//        assertEquals(AuthorizationErrorMessages.TOKEN_EXPIRED_EXCEPTION, exception.getMessage());
//    }
}
