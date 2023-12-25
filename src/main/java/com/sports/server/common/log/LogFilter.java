package com.sports.server.common.log;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Map;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
@Slf4j
@RequiredArgsConstructor
public class LogFilter implements Filter {

    private final TimeLogTemplate timeLogTemplate;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) {
        HttpServletRequest req = (HttpServletRequest) request;
        String requestUri = generateMessage(req);
        log.info(requestUri);
        try {
            timeLogTemplate.execute(
                    () -> chain.doFilter(request, response),
                    requestUri
            );
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private String generateMessage(HttpServletRequest req) {
        Map<String, String[]> parameterMap = req.getParameterMap();
        StringBuilder message = new StringBuilder();
        message.append("REQUEST URI : ")
                .append(req.getRequestURI())
                .append("?");
        for (String key : parameterMap.keySet()) {
            message.append(key)
                    .append("=")
                    .append(Arrays.toString(parameterMap.get(key)))
                    .append("&");
        }
        return message.toString();
    }
}