package com.sports.server.common.log;

import io.micrometer.core.instrument.MeterRegistry;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Locale;
import java.util.regex.Pattern;

// 앱이 보내는 X-App-* 헤더를 로그(MDC)와 지표로 남긴다. 웹 클라이언트는 헤더가 없으므로 아무 일도 하지 않는다.
// CorrelationIdFilter(MDC.clear 호출) 뒤, LogFilter 앞에서 돌아야 한다.
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
@RequiredArgsConstructor
public class AppClientFilter implements Filter {

    static final String PLATFORM_KEY = "APP_PLATFORM";
    static final String VERSION_KEY = "APP_VERSION";
    static final String BUILD_KEY = "APP_BUILD";
    static final String COUNTER_NAME = "app.client.requests";

    private static final Pattern VERSION_PATTERN = Pattern.compile("^\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}$");
    private static final Pattern BUILD_PATTERN = Pattern.compile("^\\d{1,6}$");

    private final MeterRegistry meterRegistry;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        String platform = platform(req.getHeader("X-App-Platform"));
        String version = matching(req.getHeader("X-App-Version"), VERSION_PATTERN);
        String build = matching(req.getHeader("X-App-Build"), BUILD_PATTERN);

        putIfPresent(PLATFORM_KEY, platform);
        putIfPresent(VERSION_KEY, version);
        putIfPresent(BUILD_KEY, build);
        try {
            if (platform != null && build != null && !isActuatorRequest(req)) {
                meterRegistry.counter(COUNTER_NAME, "platform", platform, "build", build).increment();
            }
            chain.doFilter(request, response);
        } finally {
            MDC.remove(PLATFORM_KEY);
            MDC.remove(VERSION_KEY);
            MDC.remove(BUILD_KEY);
        }
    }

    private String platform(String value) {
        if (value == null) {
            return null;
        }
        String lower = value.toLowerCase(Locale.ROOT);
        return lower.equals("ios") || lower.equals("android") ? lower : null;
    }

    private String matching(String value, Pattern pattern) {
        return value != null && pattern.matcher(value).matches() ? value : null;
    }

    private void putIfPresent(String key, String value) {
        if (value != null) {
            MDC.put(key, value);
        }
    }

    private boolean isActuatorRequest(HttpServletRequest req) {
        return req.getRequestURI().contains("actuator");
    }
}
