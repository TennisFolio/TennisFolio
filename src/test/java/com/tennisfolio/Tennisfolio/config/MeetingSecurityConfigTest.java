package com.tennisfolio.Tennisfolio.config;

import com.tennisfolio.Tennisfolio.meeting.api.MeetingController;
import com.tennisfolio.Tennisfolio.meeting.dto.MeetingAttendanceResponse;
import com.tennisfolio.Tennisfolio.meeting.dto.MeetingAttendanceUpsertRequest;
import com.tennisfolio.Tennisfolio.meeting.dto.MeetingDetailResponse;
import com.tennisfolio.Tennisfolio.meeting.service.MeetingAttendanceCommandService;
import com.tennisfolio.Tennisfolio.meeting.service.MeetingCommandService;
import com.tennisfolio.Tennisfolio.meeting.service.MeetingCompetitionCreateService;
import com.tennisfolio.Tennisfolio.meeting.service.MeetingQueryService;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MeetingController.class)
@Import(SecurityConfig.class)
class MeetingSecurityConfigTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    MeetingCommandService meetingCommandService;

    @MockitoBean
    MeetingQueryService meetingQueryService;

    @MockitoBean
    MeetingAttendanceCommandService attendanceCommandService;

    @MockitoBean
    MeetingCompetitionCreateService competitionCreateService;

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
    void publicMeetingDetailIsPermitAll() throws Exception {
        when(meetingQueryService.getMeeting("meeting-public-id", null))
                .thenReturn(detailResponse());

        mockMvc.perform(get("/api/meetings/meeting-public-id"))
                .andExpect(status().isOk());
    }

    @Test
    void publicAttendanceUpsertIsPermitAll() throws Exception {
        when(attendanceCommandService.upsertAttendance(
                eq("meeting-public-id"),
                any(MeetingAttendanceUpsertRequest.class)
        )).thenReturn(new MeetingAttendanceResponse(100L, "Alex Kim", "MALE", "ATTENDING"));

        mockMvc.perform(post("/api/meetings/meeting-public-id/attendances")
                        .contentType("application/json")
                        .content("""
                                {
                                  "participantName": "Alex Kim",
                                  "gender": "MALE",
                                  "attendanceStatus": "ATTENDING"
                                }
                                """))
                .andExpect(status().isOk());
    }

    @Test
    void meetingNestedCommandsRequireAuthenticationByDefault() throws Exception {
        mockMvc.perform(post("/api/meetings/meeting-public-id/competition"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void ownerMeetingPatchRequiresAuthentication() throws Exception {
        mockMvc.perform(patch("/api/meetings/meeting-public-id")
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().is3xxRedirection());
    }

    private static MeetingDetailResponse detailResponse() {
        return new MeetingDetailResponse(
                "meeting-public-id",
                10L,
                null,
                "Saturday doubles",
                LocalDateTime.of(2026, 7, 4, 10, 0),
                LocalDateTime.of(2026, 7, 4, 12, 0),
                null,
                null,
                null,
                null,
                2,
                6,
                "OPEN",
                false,
                false,
                List.of()
        );
    }
}
