package com.tennisfolio.Tennisfolio.config;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.net.http.HttpClient;

@Component
public class HttpClientConfig {
    @Bean
    public HttpClient httpClient() {
        return HttpClient.newHttpClient(); // or with builder
    }
}
