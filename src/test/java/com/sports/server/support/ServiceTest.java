package com.sports.server.support;

import com.sports.server.report.infrastructure.ReportCheckClient;
import com.sports.server.support.isolation.DatabaseIsolation;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@SpringBootTest
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
