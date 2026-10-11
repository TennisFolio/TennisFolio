package com.tennisfolio.Tennisfolio.club.service;

import com.tennisfolio.Tennisfolio.club.dto.ClubCreateRequest;
import com.tennisfolio.Tennisfolio.club.entity.Club;
import com.tennisfolio.Tennisfolio.club.entity.ClubAccount;
import com.tennisfolio.Tennisfolio.club.entity.ClubAccountRole;
import com.tennisfolio.Tennisfolio.club.repository.ClubAccountRepository;
import com.tennisfolio.Tennisfolio.club.repository.ClubRepository;
import com.tennisfolio.Tennisfolio.common.UserStatus;
import com.tennisfolio.Tennisfolio.meeting.domain.AttendanceStatus;
import com.tennisfolio.Tennisfolio.meeting.entity.Meeting;
import com.tennisfolio.Tennisfolio.meeting.entity.MeetingAttendance;
import com.tennisfolio.Tennisfolio.meeting.repository.MeetingAttendanceRepository;
import com.tennisfolio.Tennisfolio.meeting.repository.MeetingRepository;
import com.tennisfolio.Tennisfolio.user.domain.Gender;
import com.tennisfolio.Tennisfolio.user.domain.User;
import com.tennisfolio.Tennisfolio.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClubCommandServiceTest {

    @Mock ClubRepository clubRepository;
    @Mock ClubAccountRepository clubAccountRepository;
    @Mock UserRepository userRepository;
    @Mock ClubAccessService clubAccessService;
    @Mock ClubSkillTierService clubSkillTierService;
    @Mock MeetingRepository meetingRepository;
    @Mock MeetingAttendanceRepository meetingAttendanceRepository;

    ClubCommandService service;

    @BeforeEach
    void setUp() {
        service = new ClubCommandService(
                clubRepository,
                clubAccountRepository,
                userRepository,
                clubAccessService,
                clubSkillTierService,
                meetingRepository,
                meetingAttendanceRepository
        );
    }

    @Test
    void createClub_registersCreatorAsOwnerAccount() {
        Club savedClub = new Club("Morning Tennis", null, 10L);
        ReflectionTestUtils.setField(savedClub, "publicId", "club-public-id");
        User creator = User.builder().userId(10L).email("alex@example.com").nickName("Alex").gender(Gender.MALE).status(UserStatus.ACTIVE).build();
        when(userRepository.findByIdAndStatus(10L, UserStatus.ACTIVE)).thenReturn(Optional.of(creator));
        when(clubRepository.save(any(Club.class))).thenReturn(savedClub);

        service.createClub(new ClubCreateRequest("Morning Tennis", null), 10L);

        ArgumentCaptor<ClubAccount> accountCaptor = ArgumentCaptor.forClass(ClubAccount.class);
        verify(clubAccountRepository).save(accountCaptor.capture());
        assertThat(accountCaptor.getValue().getClub()).isSameAs(savedClub);
        assertThat(accountCaptor.getValue().getAccountId()).isEqualTo(10L);
        assertThat(accountCaptor.getValue().getRole()).isEqualTo(ClubAccountRole.OWNER);
    }

    @Test
    void deleteClub_softDeletesClubMeetingsAndTheirAttendances() {
        Club club = new Club("Morning Tennis", null, 10L);
        ReflectionTestUtils.setField(club, "id", 100L);
        Meeting meeting = new Meeting(10L, "Saturday", LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(2), null, null, null, null, 2, 8);
        meeting.connectClub(100L);
        MeetingAttendance attendance = new MeetingAttendance(
                meeting,
                "Jamie",
                com.tennisfolio.Tennisfolio.meeting.domain.Gender.FEMALE,
                AttendanceStatus.ATTENDING
        );
        when(clubAccessService.requireOwner("club-public-id", 10L)).thenReturn(club);
        when(meetingRepository.findByClubIdAndDeletedAtIsNullOrderByStartAtDescIdDesc(100L)).thenReturn(List.of(meeting));
        when(meetingAttendanceRepository.findByMeetingAndDeletedAtIsNullOrderByIdAsc(meeting)).thenReturn(List.of(attendance));

        service.deleteClub("club-public-id", 10L);

        assertThat(club.getDeletedAt()).isNotNull();
        assertThat(meeting.isDeleted()).isTrue();
        assertThat(attendance.isDeleted()).isTrue();
    }
}
