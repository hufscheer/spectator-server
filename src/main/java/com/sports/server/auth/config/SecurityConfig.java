package com.sports.server.auth.config;

import com.sports.server.auth.filter.JwtAuthorizationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthorizationFilter jwtAuthorizationFilter;

    @Autowired
    @Qualifier("delegatedAuthenticationEntryPoint")
    AuthenticationEntryPoint authEntryPoint;

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http, MvcRequestMatcher.Builder mvc) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests((authorize) -> authorize
                        .requestMatchers(mvc.pattern("/manager/login")).permitAll()
                        .requestMatchers(mvc.pattern("/manager/**"),
                                mvc.pattern(HttpMethod.GET, "/members/info"),
                                mvc.pattern(HttpMethod.POST, "/leagues"),
                                mvc.pattern(HttpMethod.DELETE, "/leagues/{leagueId}"),
                                mvc.pattern(HttpMethod.POST, "/leagues/*/teams"),
                                mvc.pattern(HttpMethod.POST, "/leagues/{leagueId}/teams"),
                                mvc.pattern(HttpMethod.PUT, "/leagues/{leagueId}/teams/{teamId}"),
                                mvc.pattern(HttpMethod.DELETE, "/leagues/{leagueId}/teams/{teamId}"),
                                mvc.pattern(HttpMethod.POST, "/leagues/{leagueId}/teams/{teamId}/delete-logo"),
                                mvc.pattern(HttpMethod.POST, "/games/*/timelines/**"),
                                mvc.pattern(HttpMethod.PATCH, "/games/{gameId}/lineup-players/**"),
                                mvc.pattern(HttpMethod.PATCH, "/games/{gameId}/{gameTeamId}/lineup-players/**")
                        )
                        .authenticated()
                        .anyRequest().permitAll()
                )
                .httpBasic(basic -> basic.authenticationEntryPoint(authEntryPoint))
                .exceptionHandling(Customizer.withDefaults())
                .addFilterBefore(jwtAuthorizationFilter,
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }


    @Bean
    MvcRequestMatcher.Builder mvc(HandlerMappingIntrospector introspector) {
        return new MvcRequestMatcher.Builder(introspector);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration
    ) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

}