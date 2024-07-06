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
import java.util.Date;
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

    private static final String CLAIM_ID = "id";
    private static final String CLAIM_EMAIL = "email";

    public String createAccessToken(Member member) {
        Date now = new Date();
        return JWT.create()
                .withSubject(member.getEmail())
                .withExpiresAt(new Date(now.getTime() + TOKEN_VALID_TIME))
                .withClaim(CLAIM_ID, member.getId())
                .withClaim(CLAIM_EMAIL, member.getEmail())
                .sign(Algorithm.HMAC512(secretKey));
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