package com.sports.server.command.report.infrastructure;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class ReportClientConfig {

    @Value("${report-check-origin}")
    private String reportCheckOrigin;

    @Bean
    public ReportCheckClient grammarClient() {
        WebClient client = WebClient.builder()
                .baseUrl(reportCheckOrigin)
                .build();
        return HttpServiceProxyFactory
                .builder(WebClientAdapter.forClient(client))
                .build()
                .createClient(ReportCheckClient.class);
    }
}
