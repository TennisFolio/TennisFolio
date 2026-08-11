package com.tennisfolio.Tennisfolio.club.repository;

import com.tennisfolio.Tennisfolio.club.entity.Club;
import com.tennisfolio.Tennisfolio.club.entity.ClubMember;
import com.tennisfolio.Tennisfolio.club.entity.ClubMemberRole;
import com.tennisfolio.Tennisfolio.club.entity.ClubSkillTier;
import com.tennisfolio.Tennisfolio.config.QuerydslConfig;
import com.tennisfolio.Tennisfolio.meeting.domain.AttendanceStatus;
import com.tennisfolio.Tennisfolio.meeting.domain.Gender;
import com.tennisfolio.Tennisfolio.meeting.domain.MeetingParticipantType;
import com.tennisfolio.Tennisfolio.meeting.domain.MeetingStatus;
import com.tennisfolio.Tennisfolio.meeting.entity.Meeting;
import com.tennisfolio.Tennisfolio.meeting.entity.MeetingAttendance;
import com.tennisfolio.Tennisfolio.meeting.repository.MeetingAttendanceRepository;
import com.tennisfolio.Tennisfolio.meeting.repository.MeetingRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({QuerydslConfig.class, ClubDashboardQueryRepository.class})
class ClubDashboardQueryRepositoryTest {

    private static final LocalDateTime FROM = LocalDateTime.of(2026, 7, 13, 0, 0);
    private static final LocalDateTime TO = LocalDateTime.of(2026, 8, 11, 23, 59, 59);

    @Autowired
    private ClubRepository clubRepository;

    @Autowired
    private ClubMemberRepository clubMemberRepository;

    @Autowired
    private ClubSkillTierRepository clubSkillTierRepository;

    @Autowired
    private MeetingRepository meetingRepository;

    @Autowired
    private MeetingAttendanceRepository meetingAttendanceRepository;

    @Autowired
    private ClubDashboardQueryRepository dashboardQueryRepository;

    @Test
    void findDashboardData_aggregatesActiveMembersAndEligibleMeetingAttendance() {
        Club club = clubRepository.saveAndFlush(new Club("Dashboard club", "desc", 1L));
        ClubSkillTier beginner = clubSkillTierRepository.saveAndFlush(new ClubSkillTier(club, "Beginner", 1));
        ClubSkillTier advanced = clubSkillTierRepository.saveAndFlush(new ClubSkillTier(club, "Advanced", 2));

        ClubMember attendingMan = member(club, "Alex", Gender.MALE, beginner);
        ClubMember attendingWoman = member(club, "Jamie", Gender.FEMALE, advanced);
        ClubMember unclassifiedMan = member(club, "Robin", Gender.MALE, null);
        ClubMember inactiveWoman = member(club, "Former", Gender.FEMALE, beginner);
        inactiveWoman.deactivate();
        clubMemberRepository.saveAllAndFlush(List.of(attendingMan, attendingWoman, unclassifiedMan, inactiveWoman));

        Meeting firstMeeting = meeting(club.getId(), "First", LocalDateTime.of(2026, 7, 20, 10, 0));
        Meeting secondMeeting = meeting(club.getId(), "Second", LocalDateTime.of(2026, 8, 10, 10, 0));
        Meeting cancelledMeeting = meeting(club.getId(), "Cancelled", LocalDateTime.of(2026, 8, 5, 10, 0));
        cancelledMeeting.updateStatus(MeetingStatus.CANCELLED);
        Meeting outsideMeeting = meeting(club.getId(), "Outside", LocalDateTime.of(2026, 7, 12, 10, 0));
        Meeting deletedMeeting = meeting(club.getId(), "Deleted", LocalDateTime.of(2026, 8, 9, 10, 0));
        deletedMeeting.delete(LocalDateTime.of(2026, 8, 1, 0, 0));
        meetingRepository.saveAllAndFlush(List.of(firstMeeting, secondMeeting, cancelledMeeting, outsideMeeting, deletedMeeting));

        meetingAttendanceRepository.saveAllAndFlush(List.of(
                memberAttendance(firstMeeting, "Alex", Gender.MALE, AttendanceStatus.ATTENDING, attendingMan.getId()),
                attendance(firstMeeting, "Guest", Gender.FEMALE, AttendanceStatus.ATTENDING),
                memberAttendance(firstMeeting, "Jamie", Gender.FEMALE, AttendanceStatus.WAITING, attendingWoman.getId()),
                memberAttendance(secondMeeting, "Robin", Gender.MALE, AttendanceStatus.ATTENDING, unclassifiedMan.getId()),
                attendance(cancelledMeeting, "Cancelled guest", Gender.MALE, AttendanceStatus.ATTENDING),
                attendance(outsideMeeting, "Outside guest", Gender.MALE, AttendanceStatus.ATTENDING),
                attendance(deletedMeeting, "Deleted guest", Gender.MALE, AttendanceStatus.ATTENDING)
        ));

        ClubDashboardQueryRepository.DashboardData result =
                dashboardQueryRepository.findDashboardData(club.getId(), FROM, TO);

        assertThat(result.getActiveMemberCount()).isEqualTo(3);
        assertThat(result.getGenderCounts())
                .extracting(ClubDashboardQueryRepository.GenderCount::getCount)
                .containsExactly(2L, 1L);
        assertThat(result.getSkillTierCounts())
                .extracting(ClubDashboardQueryRepository.SkillTierCount::getName)
                .containsExactly("Beginner", "Advanced");
        assertThat(result.getUnclassifiedSkillMemberCount()).isEqualTo(1);
        assertThat(result.getMeetingCount()).isEqualTo(2);
        assertThat(result.getCancelledMeetingCount()).isEqualTo(1);
        assertThat(result.getMemberAttendanceCount()).isEqualTo(2);
        assertThat(result.getGuestAttendanceCount()).isEqualTo(1);
        assertThat(result.getParticipantCount()).isEqualTo(2);
        assertThat(result.getRecentMeetings())
                .extracting(ClubDashboardQueryRepository.RecentMeeting::getTitle)
                .containsExactly("Second", "First");
        assertThat(result.getRecentMeetings().get(0).getMemberAttendanceCount()).isEqualTo(1);
        assertThat(result.getRecentMeetings().get(0).getGuestAttendanceCount()).isZero();
    }

    private static ClubMember member(Club club, String name, Gender gender, ClubSkillTier skillTier) {
        return new ClubMember(club, null, name, gender, ClubMemberRole.MEMBER, skillTier, null, null);
    }

    private static Meeting meeting(Long clubId, String title, LocalDateTime startAt) {
        Meeting meeting = new Meeting(1L, title, startAt, startAt.plusHours(2), null, null, null, null, 2, 6);
        meeting.connectClub(clubId);
        return meeting;
    }

    private static MeetingAttendance attendance(
            Meeting meeting,
            String name,
            Gender gender,
            AttendanceStatus attendanceStatus
    ) {
        return new MeetingAttendance(meeting, name, gender, attendanceStatus);
    }

    private static MeetingAttendance memberAttendance(
            Meeting meeting,
            String name,
            Gender gender,
            AttendanceStatus attendanceStatus,
            Long clubMemberId
    ) {
        MeetingAttendance attendance = attendance(meeting, name, gender, attendanceStatus);
        attendance.assignParticipant(MeetingParticipantType.CLUB_MEMBER, clubMemberId);
        return attendance;
    }
}
