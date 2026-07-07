package com.tennisfolio.Tennisfolio.config;

import com.tennisfolio.Tennisfolio.club.api.ClubMeetingController;
import com.tennisfolio.Tennisfolio.club.service.ClubMeetingCommandService;
import com.tennisfolio.Tennisfolio.club.service.ClubMeetingQueryService;
import com.tennisfolio.Tennisfolio.common.monitoring.query.config.QueryCountInterceptor;
import com.tennisfolio.Tennisfolio.security.jwt.JwtAuthenticationFilter;
import com.tennisfolio.Tennisfolio.security.oauth.handler.OAuthLoginSuccessHandler;
import com.tennisfolio.Tennisfolio.security.oauth.service.CustomOAuth2UserService;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ClubMeetingController.class)
@Import(SecurityConfig.class)
class ClubMeetingSecurityConfigTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    ClubMeetingCommandService clubMeetingCommandService;

    @MockitoBean
    ClubMeetingQueryService clubMeetingQueryService;

    @MockitoBean
    OAuthLoginSuccessHandler oAuthLoginSuccessHandler;

    @MockitoBean
    CustomOAuth2UserService customOAuth2UserService;

    @MockitoBean
    JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    ClientRegistrationRepository clientRegistrationRepository;

    @MockitoBean
    JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @MockitoBean
    QueryCountInterceptor queryCountInterceptor;

    @BeforeEach
    void setUpJwtFilter() throws Exception {
        doAnswer(invocation -> {
            FilterChain filterChain = invocation.getArgument(2);
            filterChain.doFilter(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(jwtAuthenticationFilter).doFilter(any(), any(), any());
    }

    @Test
    void clubMeetingListRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/clubs/club-public-id/meetings"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    void clubMeetingCreateRequiresAuthentication() throws Exception {
        mockMvc.perform(post("/api/clubs/club-public-id/meetings")
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }
}
