package com.tennisfolio.Tennisfolio.meeting.entity;

import com.tennisfolio.Tennisfolio.meeting.domain.AttendanceStatus;
import com.tennisfolio.Tennisfolio.meeting.domain.Gender;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class MeetingAttendanceTest {

    @Test
    void assignUser_storesAuthenticatedUserId() {
        MeetingAttendance attendance = new MeetingAttendance(
                meeting(),
                "Alex Kim",
                Gender.MALE,
                AttendanceStatus.ATTENDING
        );

        attendance.assignUser(10L);

        assertThat(attendance.getUserId()).isEqualTo(10L);
    }

    private Meeting meeting() {
        return new Meeting(
                1L,
                "Saturday doubles",
                LocalDateTime.of(2026, 7, 18, 10, 0),
                LocalDateTime.of(2026, 7, 18, 12, 0),
                null,
                null,
                null,
                null,
                2,
                6
        );
    }
}
