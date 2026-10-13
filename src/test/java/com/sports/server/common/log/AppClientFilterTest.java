package com.sports.server.common.log;

import static org.assertj.core.api.Assertions.assertThat;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class AppClientFilterTest {

    private SimpleMeterRegistry registry;
    private AppClientFilter filter;

    @BeforeEach
    void setUp() {
        MDC.clear();
        registry = new SimpleMeterRegistry();
        filter = new AppClientFilter(registry);
    }

    private Map<String, String> run(String uri, String platform, String version, String build) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", uri);
        if (platform != null) request.addHeader("X-App-Platform", platform);
        if (version != null) request.addHeader("X-App-Version", version);
        if (build != null) request.addHeader("X-App-Build", build);
        Map<String, String> seen = new HashMap<>();
        MockFilterChain chain = new MockFilterChain() {
            @Override
            public void doFilter(ServletRequest req, ServletResponse res) {
                seen.put("platform", MDC.get("APP_PLATFORM"));
                seen.put("version", MDC.get("APP_VERSION"));
                seen.put("build", MDC.get("APP_BUILD"));
            }
        };
        filter.doFilter(request, new MockHttpServletResponse(), chain);
        return seen;
    }

    private double count(String platform, String build) {
        var counter = registry.find("app.client.requests").tags("platform", platform, "build", build).counter();
        return counter == null ? 0 : counter.count();
    }

    @Test
    void 유효한_헤더는_체인_안에서_MDC로_보이고_끝나면_지워진다() throws Exception {
        Map<String, String> seen = run("/games", "IOS", "0.1.0", "5");

        assertThat(seen.get("platform")).isEqualTo("ios");
        assertThat(seen.get("version")).isEqualTo("0.1.0");
        assertThat(seen.get("build")).isEqualTo("5");
        assertThat(MDC.get("APP_PLATFORM")).isNull();
        assertThat(MDC.get("APP_VERSION")).isNull();
        assertThat(MDC.get("APP_BUILD")).isNull();
        assertThat(count("ios", "5")).isEqualTo(1);
    }

    @Test
    void 잘못된_값은_무시한다() throws Exception {
        Map<String, String> seen = run("/games", "windows", "1.2", "abc");

        assertThat(seen.get("platform")).isNull();
        assertThat(seen.get("version")).isNull();
        assertThat(seen.get("build")).isNull();
        assertThat(registry.getMeters()).isEmpty();
    }

    @Test
    void 길이를_넘는_값도_무시한다() throws Exception {
        Map<String, String> seen = run("/games", "android", "1000.0.0", "1234567");

        assertThat(seen.get("platform")).isEqualTo("android");
        assertThat(seen.get("version")).isNull();
        assertThat(seen.get("build")).isNull();
        assertThat(registry.getMeters()).isEmpty();
    }

    @Test
    void 헤더가_없으면_아무것도_남기지_않는다() throws Exception {
        Map<String, String> seen = run("/games", null, null, null);

        assertThat(seen.values()).containsOnlyNulls();
        assertThat(registry.getMeters()).isEmpty();
    }

    @Test
    void 지표는_플랫폼과_빌드가_모두_유효할_때만_센다() throws Exception {
        run("/games", "android", "0.1.0", null);
        run("/games", null, "0.1.0", "7");
        assertThat(registry.getMeters()).isEmpty();

        run("/games", "android", null, "7");
        run("/games", "android", "0.1.0", "7");
        assertThat(count("android", "7")).isEqualTo(2);
    }

    @Test
    void 액추에이터_요청은_지표에서_제외한다() throws Exception {
        run("/actuator/prometheus", "ios", "0.1.0", "5");

        assertThat(registry.getMeters()).isEmpty();
    }

    @Test
    void 체인에서_예외가_나도_MDC를_지운다() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/games");
        request.addHeader("X-App-Platform", "ios");
        MockFilterChain chain = new MockFilterChain() {
            @Override
            public void doFilter(ServletRequest req, ServletResponse res) {
                throw new IllegalStateException("boom");
            }
        };

        try {
            filter.doFilter(request, new MockHttpServletResponse(), chain);
        } catch (Exception ignored) {
        }

        assertThat(MDC.get("APP_PLATFORM")).isNull();
    }
}
