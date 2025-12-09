package com.sports.server.common.log;

import com.sports.server.common.exception.ExceptionMessages;
import com.sports.server.common.exception.InternalServerException;
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
        try {
            if (isActuatorRequest(req)) {
                chain.doFilter(request, response);
                return;
            }
            String requestUri = generateMessage(req);
            log.info(requestUri);
            timeLogTemplate.execute(
                    () -> chain.doFilter(request, response),
                    requestUri
            );
        } catch (Throwable e) {
            throw new InternalServerException(ExceptionMessages.LOG_FILTER_ERROR, e);
        }
    }

    private boolean isActuatorRequest(HttpServletRequest req) {
        return req.getRequestURI().contains("actuator");
    }

    private String generateMessage(HttpServletRequest req) {
        Map<String, String[]> parameterMap = req.getParameterMap();
        StringBuilder message = new StringBuilder();
        message.append("[REQUEST] ")
                .append(req.getMethod())
                .append(" ")
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
