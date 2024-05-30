package com.sports.server.query.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.sports.server.command.member.domain.Member;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {
    @Value("${jwt.secret-key}")
    private String secretKey;

    @Value("${jwt.valid-time}")
    private long TOKEN_VALID_TIME;
    private final String ACCESS_TOKEN_HEADER_STRING = "Authorization";
    private final String TOKEN_PREFIX = "Bearer ";

    public String createAccessToken(Member member) {
        Date now = new Date();
        return JWT.create().withSubject(member.getEmail())
                .withExpiresAt(new Date(now.getTime() + TOKEN_VALID_TIME)).withClaim("id", member.getId())
                .withClaim("email", member.getEmail()).sign(Algorithm.HMAC512(secretKey));
    }

}
