package com.sports.server.common.config;

import io.micrometer.cloudwatch2.CloudWatchConfig;
import io.micrometer.cloudwatch2.CloudWatchMeterRegistry;
import io.micrometer.core.instrument.Clock;

import java.time.Duration;

import io.micrometer.core.instrument.config.MeterFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatch.CloudWatchAsyncClient;

@Profile("prod")
@Configuration
public class CloudWatchCustomConfig {

    private static final String NAMESPACE = "HUFSCHEER/prod";
    private static final Duration STEP = Duration.ofMinutes(5);
    private static final String BATCH_SIZE = "20";

    @Bean
    public CloudWatchConfig cloudWatchConfig() {
        return new CloudWatchConfig() {

            @Override
            public String get(String key) {
                if ("cloudwatch.batchSize".equals(key)) return BATCH_SIZE;
                if ("cloudwatch.enabled".equals(key)) return "true";
                return null;
            }

            @Override
            public String namespace() {
                return NAMESPACE;
            }

            @Override
            public Duration step() {
                return STEP;
            }
        };
    }

    @Bean
    public CloudWatchAsyncClient cloudWatchAsyncClient() {
        return CloudWatchAsyncClient.builder().region(Region.AP_NORTHEAST_2).build();
    }

    @Bean
    public CloudWatchMeterRegistry cloudWatchMeterRegistry(CloudWatchConfig config, CloudWatchAsyncClient client) {
        return new CloudWatchMeterRegistry(config, Clock.SYSTEM, client);
    }

    @Bean
    public MeterFilter allowOnlySelected() {
        return MeterFilter.denyUnless(id -> id.getName().startsWith("jvm.") || id.getName().startsWith("http.server.") || id.getName().startsWith("tomcat.") || id.getName().startsWith("hikaricp."));
    }
}
