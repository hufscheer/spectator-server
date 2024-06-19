package com.sports.server.auth.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.sports.server.auth.details.MemberDetails;
import com.sports.server.auth.exception.AuthorizationErrorMessages;
import com.sports.server.command.member.domain.Member;
import com.sports.server.command.member.domain.MemberRepository;
import com.sports.server.common.exception.UnauthorizedException;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtProvider {

    @Value("${jwt.secret-key}")
    private String secretKey;

    @Value("${jwt.valid-time}")
    private long TOKEN_VALID_TIME;

    private final MemberRepository memberRepository;

    public String createAccessToken(Member member) {
        Date now = new Date();
        return JWT.create().withSubject(member.getEmail())
                .withExpiresAt(new Date(now.getTime() + TOKEN_VALID_TIME)).withClaim("id", member.getId())
                .withClaim("email", member.getEmail()).sign(Algorithm.HMAC512(secretKey));
    }

    public Authentication getAuthentication(final String token) {
        String email = null;

        try {
            email = JWT.require(Algorithm.HMAC512(secretKey)).build().verify(token).getClaim("email").asString();
        } catch (TokenExpiredException e) {
            throw new UnauthorizedException(AuthorizationErrorMessages.TOKEN_EXPIRED_EXCEPTION);
        } catch (JWTVerificationException e) {
            throw new UnauthorizedException(AuthorizationErrorMessages.INVALID_TOKEN_EXCEPTION);
        }

        if (email != null) {
            Member member = memberRepository.findMemberByEmail(email);
            if (member != null) {
                MemberDetails memberDetails = new MemberDetails(member);
                return new UsernamePasswordAuthenticationToken(memberDetails, null,
                        memberDetails.getAuthorities());
            }
        }
        throw new UnauthorizedException(AuthorizationErrorMessages.MEMBER_NOT_FOUND_EXCEPTION);
    }

}