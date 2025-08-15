package com.tennisfolio.Tennisfolio.common.aop;

import org.slf4j.MDC;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;


@Configuration
public class ClientTracingConfig {

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder){
        ClientHttpRequestInterceptor tracingInterceptor = (req, body, ex) -> {
            String traceId = MDC.get("traceId");
            if(traceId != null){
                req.getHeaders().add(TraceIdFilter.TRACE_HEADER, traceId);
            }
            return ex.execute(req, body);
        };

        return builder
                .additionalInterceptors(tracingInterceptor)
                .build();
    }

    @Bean
    public WebClient webClient(WebClient.Builder builder){
        ExchangeFilterFunction tracingFilter = (request, next) -> {
            String traceId = MDC.get("traceId");
            ClientRequest mutated = ClientRequest.from(request)
                    .headers(h -> {if (traceId != null) h.add(TraceIdFilter.TRACE_HEADER, traceId); })
                    .build();
            return next.exchange(mutated);
        };
        return builder.filter(tracingFilter).build();
    }


}
