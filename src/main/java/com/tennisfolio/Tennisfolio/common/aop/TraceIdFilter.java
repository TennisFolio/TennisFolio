package com.tennisfolio.Tennisfolio.common.aop;


import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TraceIdFilter implements Filter {
    public static final String TRACE_HEADER = "X-Trace-Id";
    private static final Set<String> SKIP_PREFIXES = Set.of(
            "/actuator/health", "/actuator/prometheus",
            "/favicon", "/css/", "/js/", "/static/");

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        String uri = request.getRequestURI();
        for (String prefix : SKIP_PREFIXES) {
            if (uri.startsWith(prefix)) {
                filterChain.doFilter(servletRequest, servletResponse);
                return;
            }
        }
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        String traceId = request.getHeader(TRACE_HEADER);
        if(traceId == null || traceId.isBlank()){
            traceId = UUID.randomUUID().toString().replace("-", "");
        }

        try{
            MDC.put("traceId", traceId);
            response.setHeader(TRACE_HEADER, traceId);
            filterChain.doFilter(servletRequest, servletResponse);

        } finally {
            MDC.remove("traceId");
        }
    }
}
