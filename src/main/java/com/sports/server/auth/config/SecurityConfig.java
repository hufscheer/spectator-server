package com.sports.server.auth.config;

import com.sports.server.auth.filter.JwtAuthorizationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthorizationFilter jwtAuthorizationFilter;

    @Autowired
    @Qualifier("delegatedAuthenticationEntryPoint")
    AuthenticationEntryPoint authEntryPoint;

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http, PathPatternRequestMatcher.Builder paths) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests((authorize) -> authorize
                        .requestMatchers(paths.matcher("/manager/login")).permitAll()
                        .requestMatchers(paths.matcher("/manager/**"),
                                paths.matcher(HttpMethod.GET, "/members/info"),
                                paths.matcher(HttpMethod.POST, "/leagues"),
                                paths.matcher(HttpMethod.PUT, "/leagues/{leagueId}"),
                                paths.matcher(HttpMethod.PUT, "/leagues/{leagueId}/bracket"),
                                paths.matcher(HttpMethod.PUT, "/leagues/{leagueId}/{gameId}"),
                                paths.matcher(HttpMethod.POST, "/leagues/*/teams"),
                                paths.matcher(HttpMethod.DELETE, "/leagues/*/teams"),
                                paths.matcher(HttpMethod.POST, "/leagues/{leagueId}/teams"),
                                paths.matcher(HttpMethod.PUT, "/leagues/{leagueId}/teams/{teamId}"),
                                paths.matcher(HttpMethod.DELETE, "/leagues/{leagueId}/teams/{teamId}"),
                                paths.matcher(HttpMethod.POST, "/leagues/{leagueId}/teams/{teamId}/delete-logo"),
                                paths.matcher(HttpMethod.POST, "/leagues/{leagueId}/games"),
                                paths.matcher(HttpMethod.DELETE, "/leagues/{leagueId}/{gameId}"),
                                paths.matcher(HttpMethod.GET, "/cheer-talks/**"),
                                paths.matcher(HttpMethod.POST, "/games/*/timelines/**"),
                                paths.matcher(HttpMethod.POST, "/games/{gameId}/{gameTeamId}/lineup-players/{lineupPlayerId}/**"),
                                paths.matcher(HttpMethod.POST, "/teams/**"),
                                paths.matcher(HttpMethod.PUT, "/teams/{teamId}"),
                                paths.matcher(HttpMethod.DELETE, "/teams/**"),
                                paths.matcher(HttpMethod.GET, "/players"),
                                paths.matcher(HttpMethod.POST, "/players"),
                                paths.matcher(HttpMethod.PATCH, "/players/{playerId}"),
                                paths.matcher(HttpMethod.DELETE, "/players/{playerId}"),
                                paths.matcher(HttpMethod.PATCH, "/cheer-talks/**"),
                                paths.matcher(HttpMethod.POST, "/nl/**"),
                                paths.matcher(HttpMethod.DELETE, "/game-teams/{gameTeamId}"),
                                paths.matcher(HttpMethod.POST, "/game-teams/{gameTeamId}/lineup-players"),
                                paths.matcher(HttpMethod.DELETE, "/game-teams/{gameTeamId}/lineup-players/{lineupPlayerId}"),
                                paths.matcher(HttpMethod.PATCH, "/games/{gameId}/lineup-players/{lineupPlayerId}/starter"),
                                paths.matcher(HttpMethod.PATCH, "/games/{gameId}/lineup-players/{lineupPlayerId}/candidate"),
                                paths.matcher(HttpMethod.PATCH, "/games/{gameId}/lineup-players/{lineupPlayerId}/position"),
                                paths.matcher(HttpMethod.PUT, "/games/{gameId}/lineup-players/{lineupPlayerId}/captain/register"),
                                paths.matcher(HttpMethod.PATCH, "/games/{gameId}/lineup-players/{lineupPlayerId}/captain/revoke"),
                                paths.matcher(HttpMethod.POST, "/admin/**"),
                                paths.matcher(HttpMethod.PATCH, "/reports/{leagueId}/{cheerTalkId}/cancel")
                        )
                        .authenticated()
                        .anyRequest().permitAll()
                )
                .exceptionHandling(eh -> eh.authenticationEntryPoint(authEntryPoint))
                .addFilterBefore(jwtAuthorizationFilter,
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }


    @Bean
    PathPatternRequestMatcher.Builder paths() {
        return PathPatternRequestMatcher.withDefaults();
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