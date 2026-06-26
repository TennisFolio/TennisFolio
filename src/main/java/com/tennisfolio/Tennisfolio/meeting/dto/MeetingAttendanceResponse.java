package com.tennisfolio.Tennisfolio.meeting.dto;

import com.tennisfolio.Tennisfolio.meeting.entity.MeetingAttendance;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MeetingAttendanceResponse {
    private Long id;
    private String participantName;
    private String gender;
    private String attendanceStatus;

    public static MeetingAttendanceResponse from(MeetingAttendance attendance) {
        return new MeetingAttendanceResponse(
                attendance.getId(),
                attendance.getParticipantName(),
                attendance.getGender().name(),
                attendance.getAttendanceStatus().name()
        );
    }
}
