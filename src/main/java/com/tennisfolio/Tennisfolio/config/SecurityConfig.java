package com.tennisfolio.Tennisfolio.config;

import com.tennisfolio.Tennisfolio.security.jwt.JwtAuthenticationFilter;
import com.tennisfolio.Tennisfolio.security.jwt.JwtAuthenticationToken;
import com.tennisfolio.Tennisfolio.security.oauth.handler.OAuthLoginSuccessHandler;
import com.tennisfolio.Tennisfolio.security.oauth.service.CustomOAuth2UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final OAuthLoginSuccessHandler oAuthLoginSuccessHandler;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(OAuthLoginSuccessHandler oAuthLoginSuccessHandler, CustomOAuth2UserService customOAuth2UserService, JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.oAuthLoginSuccessHandler = oAuthLoginSuccessHandler;
        this.customOAuth2UserService = customOAuth2UserService;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/logout").authenticated()
                        .anyRequest().permitAll()
                )
                .oauth2Login(oauth -> oauth
                        .userInfoEndpoint(userInfo ->
                                userInfo.userService(customOAuth2UserService)
                        )
                        .successHandler(oAuthLoginSuccessHandler)
                )
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}
