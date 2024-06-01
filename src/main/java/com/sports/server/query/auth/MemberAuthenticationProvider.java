package com.sports.server.query.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

@RequiredArgsConstructor
public class MemberAuthenticationProvider implements AuthenticationProvider {

    private final MemberDetailsService memberDetailsService;

    private final PasswordEncoder passwordEncoder;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = authentication.getCredentials().toString();

        UserDetails member = memberDetailsService.loadUserByUsername(username);
        if (passwordEncoder.matches(password, member.getPassword())) {
            return new UsernamePasswordAuthenticationToken(username, password, member.getAuthorities());
        } else {
            throw new BadCredentialsException("존재하지 않는 사용자입니다.");
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return false;
    }
}
