package com.sports.server.common.log;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
@Slf4j
public class LogFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws ServletException, IOException {
        HttpServletRequest req = (HttpServletRequest) request;
        log.info(generateMessage(req));
        chain.doFilter(request, response);
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
