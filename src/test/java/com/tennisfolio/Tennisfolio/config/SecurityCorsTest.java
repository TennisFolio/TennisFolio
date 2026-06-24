package com.tennisfolio.Tennisfolio.config;

import com.tennisfolio.Tennisfolio.common.monitoring.query.config.QueryCountInterceptor;
import com.tennisfolio.Tennisfolio.matching.service.CompetitionCommandService;
import com.tennisfolio.Tennisfolio.matching.service.CompetitionQueryService;
import com.tennisfolio.Tennisfolio.security.jwt.JwtAuthenticationFilter;
import com.tennisfolio.Tennisfolio.security.oauth.handler.OAuthLoginSuccessHandler;
import com.tennisfolio.Tennisfolio.security.oauth.service.CustomOAuth2UserService;
import com.tennisfolio.Tennisfolio.user.api.AuthController;
import com.tennisfolio.Tennisfolio.user.service.AuthLogoutService;
import com.tennisfolio.Tennisfolio.user.service.AuthProfileService;
import com.tennisfolio.Tennisfolio.user.service.AuthQueryService;
import com.tennisfolio.Tennisfolio.security.oauth.service.OAuthUnlinkService;
import com.tennisfolio.Tennisfolio.security.oauth.service.ReIssueService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;


import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, WebConfig.class})
class SecurityCorsTest {

    @Autowired
    MockMvc mockMvc;

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
    OAuthUnlinkService oAuthUnlinkService;

    @MockitoBean
    ReIssueService reIssueService;

    @MockitoBean
    AuthQueryService authQueryService;

    @MockitoBean
    AuthLogoutService authLogoutService;

    @MockitoBean
    AuthProfileService authProfileService;

    @MockitoBean
    CompetitionQueryService competitionQueryService;

    @MockitoBean
    CompetitionCommandService competitionCommandService;

    @MockitoBean
    QueryCountInterceptor queryCountInterceptor;

    @Test
    void profileUpdatePreflightUsesConfiguredCorsWithoutAuthentication() throws Exception {
        mockMvc.perform(options("/api/auth/profile")
                        .header(HttpHeaders.ORIGIN, "http://localhost:5173")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "PATCH"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:5173"))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true"));
    }
}
