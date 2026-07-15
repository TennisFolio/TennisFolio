package com.tennisfolio.Tennisfolio.meeting.repository;

import com.tennisfolio.Tennisfolio.config.QuerydslConfig;
import com.tennisfolio.Tennisfolio.meeting.domain.AttendanceStatus;
import com.tennisfolio.Tennisfolio.meeting.domain.Gender;
import com.tennisfolio.Tennisfolio.meeting.domain.MeetingStatus;
import com.tennisfolio.Tennisfolio.meeting.entity.Meeting;
import com.tennisfolio.Tennisfolio.meeting.entity.MeetingAttendance;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(QuerydslConfig.class)
class MeetingRepositoryTest {

    @Autowired
    private MeetingRepository meetingRepository;

    @Autowired
    private MeetingAttendanceRepository meetingAttendanceRepository;

    @Test
    void saveMeeting_persistsPublicIdOwnerAndOpenStatus() {
        Meeting meeting = new Meeting(
                10L,
                "Saturday doubles",
                LocalDateTime.of(2026, 7, 4, 10, 0),
                LocalDateTime.of(2026, 7, 4, 12, 0),
                "Indoor court",
                12,
                8,
                4,
                2,
                6
        );

        Meeting saved = meetingRepository.saveAndFlush(meeting);

        Meeting found = meetingRepository.findById(saved.getId()).orElseThrow();
        assertThat(found.getPublicId()).isNotBlank();
        assertThat(found.getOwnerUserId()).isEqualTo(10L);
        assertThat(found.getClubId()).isNull();
        assertThat(found.getCompetitionId()).isNull();
        assertThat(found.getStatus()).isEqualTo(MeetingStatus.OPEN);
        assertThat(found.getDeletedAt()).isNull();
    }

    @Test
    void findByClubId_returnsOnlyActiveClubMeetingsInLatestOrder() {
        Meeting olderClubMeeting = clubMeeting(100L, LocalDateTime.of(2026, 7, 4, 10, 0), "older");
        Meeting newerClubMeeting = clubMeeting(100L, LocalDateTime.of(2026, 7, 5, 10, 0), "newer");
        Meeting otherClubMeeting = clubMeeting(200L, LocalDateTime.of(2026, 7, 6, 10, 0), "other");
        Meeting independentMeeting = meeting("independent", LocalDateTime.of(2026, 7, 7, 10, 0));
        Meeting deletedClubMeeting = clubMeeting(100L, LocalDateTime.of(2026, 7, 8, 10, 0), "deleted");
        deletedClubMeeting.delete(LocalDateTime.of(2026, 7, 1, 0, 0));
        meetingRepository.saveAllAndFlush(List.of(
                olderClubMeeting,
                newerClubMeeting,
                otherClubMeeting,
                independentMeeting,
                deletedClubMeeting
        ));

        List<Meeting> found = meetingRepository.findByClubIdAndDeletedAtIsNullOrderByStartAtDescIdDesc(100L);

        assertThat(found)
                .extracting(Meeting::getTitle)
                .containsExactly("newer", "older");
    }

    @Test
    void saveMeetingAttendance_persistsMeetingNameGenderAndStatus() {
        Meeting meeting = meetingRepository.saveAndFlush(new Meeting(
                10L,
                "Saturday doubles",
                LocalDateTime.of(2026, 7, 4, 10, 0),
                LocalDateTime.of(2026, 7, 4, 12, 0),
                null,
                null,
                null,
                null,
                2,
                6
        ));
        MeetingAttendance attendance = new MeetingAttendance(
                meeting,
                "Alex Kim",
                Gender.MALE,
                AttendanceStatus.ATTENDING
        );

        MeetingAttendance saved = meetingAttendanceRepository.saveAndFlush(attendance);

        MeetingAttendance found = meetingAttendanceRepository.findById(saved.getId()).orElseThrow();
        assertThat(found.getMeeting().getId()).isEqualTo(meeting.getId());
        assertThat(found.getParticipantName()).isEqualTo("Alex Kim");
        assertThat(found.getGender()).isEqualTo(Gender.MALE);
        assertThat(found.getAttendanceStatus()).isEqualTo(AttendanceStatus.ATTENDING);
        assertThat(found.getDeletedAt()).isNull();
    }

    @Test
    void findByMeetingAndUserId_returnsOnlyActiveAttendanceForUser() {
        Meeting meeting = meetingRepository.saveAndFlush(meeting(
                "Saturday doubles",
                LocalDateTime.of(2026, 7, 4, 10, 0)
        ));
        MeetingAttendance activeAttendance = new MeetingAttendance(
                meeting,
                "Alex Kim",
                Gender.MALE,
                AttendanceStatus.ATTENDING
        );
        activeAttendance.assignUser(10L);
        MeetingAttendance deletedAttendance = new MeetingAttendance(
                meeting,
                "Alex Kim old response",
                Gender.MALE,
                AttendanceStatus.NOT_ATTENDING
        );
        deletedAttendance.assignUser(10L);
        deletedAttendance.delete(LocalDateTime.of(2026, 7, 1, 0, 0));
        meetingAttendanceRepository.saveAllAndFlush(List.of(activeAttendance, deletedAttendance));

        Optional<MeetingAttendance> found =
                meetingAttendanceRepository.findByMeetingAndUserIdAndDeletedAtIsNull(meeting, 10L);

        assertThat(found).contains(activeAttendance);
    }

    private static Meeting clubMeeting(Long clubId, LocalDateTime startAt, String title) {
        Meeting meeting = meeting(title, startAt);
        meeting.connectClub(clubId);
        return meeting;
    }

    private static Meeting meeting(String title, LocalDateTime startAt) {
        return new Meeting(
                10L,
                title,
                startAt,
                startAt.plusHours(2),
                null,
                null,
                null,
                null,
                2,
                6
        );
    }
}
