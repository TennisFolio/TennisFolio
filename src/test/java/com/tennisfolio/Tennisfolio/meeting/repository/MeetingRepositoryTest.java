package com.tennisfolio.Tennisfolio.meeting.repository;

import com.tennisfolio.Tennisfolio.config.QuerydslConfig;
import com.tennisfolio.Tennisfolio.meeting.entity.Meeting;
import com.tennisfolio.Tennisfolio.meeting.entity.MeetingAttendance;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;

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
        assertThat(found.getCompetitionId()).isNull();
        assertThat(found.getStatus()).isEqualTo(Meeting.MeetingStatus.OPEN);
        assertThat(found.getDeletedAt()).isNull();
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
                MeetingAttendance.Gender.MALE,
                MeetingAttendance.AttendanceStatus.ATTENDING
        );

        MeetingAttendance saved = meetingAttendanceRepository.saveAndFlush(attendance);

        MeetingAttendance found = meetingAttendanceRepository.findById(saved.getId()).orElseThrow();
        assertThat(found.getMeeting().getId()).isEqualTo(meeting.getId());
        assertThat(found.getParticipantName()).isEqualTo("Alex Kim");
        assertThat(found.getGender()).isEqualTo(MeetingAttendance.Gender.MALE);
        assertThat(found.getAttendanceStatus()).isEqualTo(MeetingAttendance.AttendanceStatus.ATTENDING);
        assertThat(found.getDeletedAt()).isNull();
    }
}
