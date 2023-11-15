package com.sports.server.comment.util;

import com.vane.badwordfiltering.BadWordFiltering;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BadWordFilteringConfig {

    @Bean
    public BadWordFiltering registerBadWordFiltering() {
        return new BadWordFiltering();
    }
}
