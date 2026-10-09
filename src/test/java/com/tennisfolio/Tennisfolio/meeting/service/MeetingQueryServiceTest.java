package com.tennisfolio.Tennisfolio.meeting.service;

import com.tennisfolio.Tennisfolio.club.repository.ClubRepository;
import com.tennisfolio.Tennisfolio.club.repository.ClubSkillTierRepository;
import com.tennisfolio.Tennisfolio.club.service.ClubAccessService;
import com.tennisfolio.Tennisfolio.matching.repository.CompetitionRepository;
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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MeetingQueryServiceTest {
    @Mock MeetingRepository meetingRepository;
    @Mock MeetingAttendanceRepository attendanceRepository;
    @Mock CompetitionRepository competitionRepository;
    @Mock UserRepository userRepository;
    @Mock ClubRepository clubRepository;
    @Mock ClubSkillTierRepository clubSkillTierRepository;
    @Mock ClubAccessService clubAccessService;
    @InjectMocks MeetingQueryService service;

    @Test
    void getMeeting_rejectsNonOperatorForClubMeeting() {
        Meeting meeting = org.mockito.Mockito.mock(Meeting.class);
        when(meetingRepository.findByPublicIdAndDeletedAtIsNull("meeting")).thenReturn(Optional.of(meeting));
        when(meeting.isClubMeeting()).thenReturn(true);
        when(meeting.getClubId()).thenReturn(100L);
        when(clubAccessService.isOperator(100L, 10L)).thenReturn(false);

        assertThatThrownBy(() -> service.getMeeting("meeting", 10L))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("statusCode")
                .isEqualTo(HttpStatus.FORBIDDEN);
    }
}
