package com.tennisfolio.Tennisfolio.common.monitoring.query.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.messaging.handler.HandlerMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;

@Component
@AllArgsConstructor
public class QueryCountInterceptor implements HandlerInterceptor {
    public static final String UNKNOWN_PATH = "UNKNOWN_PATH";

    private final MeterRegistry meterRegistry;

    /**
     * 컨트롤러 실행 전: RequestContext 생성 후 ThreadLocal 에 등록
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {

        String httpMethod = request.getMethod();

        RequestContext ctx = RequestContext.builder()
                .httpMethod(httpMethod)
                .build();

        RequestContextHolder.initContext(ctx);
        return true;
    }

    /**
     * 요청 처리 완료 시점: 누적된 쿼리 횟수를 꺼내어 MeterRegistry 에 기록하고 ThreadLocal 정리
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        RequestContext ctx = RequestContextHolder.getContext();

        if (ctx != null) {

            // ★ 여기서 path template 추출됨
            String pathPattern =
                    (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);

            if (pathPattern == null) {
                pathPattern = request.getRequestURI(); // fallback
            }

            ctx.setBestMatchPath(pathPattern);

            // 모든 QueryType SQL 개수를 합산 → "요청당 총 SQL 실행 횟수"
            int totalCount = ctx.getQueryCountByType()
                    .values()
                    .stream()
                    .mapToInt(Integer::intValue)
                    .sum();

            System.out.println("Metric submit: path=" + ctx.getBestMatchPath() + ", count=" + totalCount);

            // Grafana/N+1 측정용 Counter 기록(요청당 1회)
            Counter.builder("sql_queries_per_request_total")
                    .tag("path", ctx.getBestMatchPath())
                    .tag("http_method", ctx.getHttpMethod())
                    .register(meterRegistry)
                    .increment(totalCount);

            DistributionSummary summary = DistributionSummary.builder("sql_queries_per_request")
                    .description("SQL count per request")
                    .publishPercentileHistogram()
                    .tag("path", ctx.getBestMatchPath())
                    .tag("http_method", ctx.getHttpMethod())
                    .register(meterRegistry);

            summary.record(totalCount);

        }



        RequestContextHolder.clear();
    }

}
