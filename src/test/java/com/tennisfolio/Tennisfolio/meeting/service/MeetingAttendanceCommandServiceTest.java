package com.tennisfolio.Tennisfolio.meeting.service;

import com.tennisfolio.Tennisfolio.club.repository.ClubMemberRepository;
import com.tennisfolio.Tennisfolio.club.repository.ClubRepository;
import com.tennisfolio.Tennisfolio.club.repository.ClubSkillTierRepository;
import com.tennisfolio.Tennisfolio.club.service.ClubAccessService;
import com.tennisfolio.Tennisfolio.meeting.dto.MeetingAttendanceUpsertRequest;
import com.tennisfolio.Tennisfolio.meeting.entity.Meeting;
import com.tennisfolio.Tennisfolio.meeting.repository.MeetingAttendanceRepository;
import com.tennisfolio.Tennisfolio.meeting.repository.MeetingRepository;
import com.tennisfolio.Tennisfolio.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MeetingAttendanceCommandServiceTest {
    @Mock MeetingRepository meetingRepository;
    @Mock MeetingAttendanceRepository attendanceRepository;
    @Mock UserRepository userRepository;
    @Mock ClubRepository clubRepository;
    @Mock ClubMemberRepository clubMemberRepository;
    @Mock ClubSkillTierRepository clubSkillTierRepository;
    @Mock ClubAccessService clubAccessService;
    @InjectMocks MeetingAttendanceCommandService service;

    @Test
    void upsertAttendance_rejectsNonOperatorForClubMeeting() {
        Meeting meeting = new Meeting(10L, "클럽 모임", LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(2), null, null, null, null, 2, 8);
        ReflectionTestUtils.setField(meeting, "clubId", 100L);
        when(meetingRepository.findByPublicIdAndDeletedAtIsNullForUpdate("meeting"))
                .thenReturn(Optional.of(meeting));
        when(clubAccessService.isOperator(100L, 20L)).thenReturn(false);

        assertThatThrownBy(() -> service.upsertAttendance(
                "meeting",
                new MeetingAttendanceUpsertRequest(null, "게스트", "MALE", "ATTENDING"),
                20L
        )).isInstanceOfSatisfying(ResponseStatusException.class, error ->
                org.assertj.core.api.Assertions.assertThat(error.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN)
        );
    }
}
