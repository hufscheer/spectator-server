package com.sports.server.auth.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.sports.server.auth.exception.AuthorizationErrorMessages;
import com.sports.server.command.member.domain.Member;
import com.sports.server.common.exception.UnauthorizedException;
import java.time.Duration;
import java.util.Date;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtUtil {

    @Value("${jwt.secret-key}")
    private String secretKey;

    @Value("${jwt.valid-time}")
    private long TOKEN_VALID_TIME;

    /** 남은 수명이 이보다 짧으면 새 토큰을 내려 준다. be-config 에 없으면 기본값을 쓴다 */
    @Value("${jwt.renew-before:12h}")
    private Duration renewBefore;

    /** 처음 로그인한 때부터 이 시간이 지나면 더 연장하지 않는다 */
    @Value("${jwt.max-session-age:7d}")
    private Duration maxSessionAge;

    private static final String CLAIM_ID = "id";
    private static final String CLAIM_EMAIL = "email";
    private static final String CLAIM_AUTH_TIME = "auth_time";

    public String createAccessToken(Member member) {
        Date now = new Date();
        return createAccessToken(member, now, new Date(now.getTime() + TOKEN_VALID_TIME));
    }

    public String createAccessToken(Member member, Date authTime, Date expiresAt) {
        return JWT.create()
                .withSubject(member.getEmail())
                .withExpiresAt(expiresAt)
                .withClaim(CLAIM_ID, member.getId())
                .withClaim(CLAIM_EMAIL, member.getEmail())
                .withClaim(CLAIM_AUTH_TIME, authTime)
                .sign(Algorithm.HMAC512(secretKey));
    }

    /**
     * 검증을 통과한 토큰의 재발급 여부를 정한다. 남은 수명이 넉넉하거나 처음 로그인 뒤 상한을 넘겼으면 비어 있다.
     * 새 만료 시각은 상한을 넘지 않게 자른다.
     */
    public Optional<Renewal> planRenewal(String token) {
        DecodedJWT decodedJWT = getDecodedJWT(token);
        long now = System.currentTimeMillis();
        long expiresAt = decodedJWT.getExpiresAt().getTime();

        // 임계값이 수명 이상이면 매 요청 재발급이 되므로 수명의 절반으로 묶는다
        long threshold = Math.min(renewBefore.toMillis(), TOKEN_VALID_TIME / 2);
        if (expiresAt - now >= threshold) {
            return Optional.empty();
        }

        Date authTime = getAuthTime(decodedJWT);
        long sessionDeadline = authTime.getTime() + maxSessionAge.toMillis();
        long renewedExpiresAt = Math.min(now + TOKEN_VALID_TIME, sessionDeadline);
        // JWT 의 exp 는 초 단위라 1초 미만으로 늘어나는 재발급은 의미가 없다
        if (renewedExpiresAt - expiresAt < 1000) {
            return Optional.empty();
        }

        return Optional.of(new Renewal(authTime, new Date(renewedExpiresAt)));
    }

    /** 이 클레임이 생기기 전에 발급된 토큰은 만료 시각에서 수명을 빼 로그인 시각을 구한다 */
    private Date getAuthTime(DecodedJWT decodedJWT) {
        Date authTime = decodedJWT.getClaim(CLAIM_AUTH_TIME).asDate();
        if (authTime != null) {
            return authTime;
        }
        return new Date(decodedJWT.getExpiresAt().getTime() - TOKEN_VALID_TIME);
    }

    public record Renewal(Date authTime, Date expiresAt) {
    }

    public void validateToken(String token) {
        try {
            DecodedJWT decodedJWT = getDecodedJWT(token);
            validateClaims(decodedJWT);
        } catch (TokenExpiredException e) {
            throw new UnauthorizedException(AuthorizationErrorMessages.TOKEN_EXPIRED_EXCEPTION);
        } catch (JWTVerificationException e) {
            throw new UnauthorizedException(AuthorizationErrorMessages.INVALID_TOKEN_EXCEPTION);
        }
    }

    private DecodedJWT getDecodedJWT(String token) {
        Algorithm algorithm = Algorithm.HMAC512(secretKey);
        JWTVerifier verifier = JWT.require(algorithm).build();
        return verifier.verify(token);
    }

    private void validateClaims(final DecodedJWT decodedJWT) {
        Claim idClaim = decodedJWT.getClaim(CLAIM_ID);
        Claim emailClaim = decodedJWT.getClaim(CLAIM_EMAIL);

        if (idClaim.isNull() || emailClaim.isNull()) {
            throw new UnauthorizedException(AuthorizationErrorMessages.INVALID_TOKEN_EXCEPTION);
        }

        Long id = idClaim.asLong();
        String email = emailClaim.asString();

        if (id == null || email == null) {
            throw new UnauthorizedException(AuthorizationErrorMessages.INVALID_TOKEN_EXCEPTION);
        }
    }

    public String getEmail(String token) {
        DecodedJWT decodedJWT = getDecodedJWT(token);
        Claim emailClaim = decodedJWT.getClaim(CLAIM_EMAIL);

        if (emailClaim.isNull()) {
            throw new UnauthorizedException(AuthorizationErrorMessages.INVALID_TOKEN_EXCEPTION);
        }

        return emailClaim.asString();
    }

}