package com.tennisfolio.Tennisfolio.config;

import com.tennisfolio.Tennisfolio.club.api.ClubController;
import com.tennisfolio.Tennisfolio.club.service.ClubCommandService;
import com.tennisfolio.Tennisfolio.club.service.ClubMemberCommandService;
import com.tennisfolio.Tennisfolio.club.service.ClubQueryService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ClubController.class)
@Import(SecurityConfig.class)
class ClubSecurityConfigTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    ClubCommandService clubCommandService;

    @MockitoBean
    ClubQueryService clubQueryService;

    @MockitoBean
    ClubMemberCommandService clubMemberCommandService;

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
    void clubListRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/clubs"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    void clubCreateRequiresAuthentication() throws Exception {
        mockMvc.perform(post("/api/clubs")
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    void clubDetailRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/clubs/club-public-id"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    void clubMemberCommandsRequireAuthentication() throws Exception {
        mockMvc.perform(patch("/api/clubs/club-public-id/members/100")
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }
}
