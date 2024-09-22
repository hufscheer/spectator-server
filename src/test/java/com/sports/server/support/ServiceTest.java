package com.sports.server.support;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.sports.server.command.report.application.ReportProcessor;
import com.sports.server.command.report.infrastructure.ReportCheckClient;
import com.sports.server.support.config.AsyncTestConfig;
import com.sports.server.support.isolation.DatabaseIsolation;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;

@SpringBootTest
@Import(AsyncTestConfig.class)
@DatabaseIsolation
public class ServiceTest {

    @MockBean
    protected ReportCheckClient reportCheckClient;

    @BeforeEach
    void init() {
        given(reportCheckClient.check(any()))
                .willReturn(ResponseEntity.ok().build());
    }
}
