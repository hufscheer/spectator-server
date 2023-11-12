package com.sports.server.support;

import com.sports.server.report.infrastructure.ReportCheckClient;
import com.sports.server.support.isolation.DatabaseIsolation;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
@DatabaseIsolation
public class ServiceTest {

    @MockBean
    private ReportCheckClient reportCheckClient;
}
