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
    private String participantType;
    private Long clubMemberId;
    private String badgeLabel;

    public MeetingAttendanceResponse(
            Long id,
            String participantName,
            String gender,
            String attendanceStatus
    ) {
        this(id, participantName, gender, attendanceStatus, "GUEST", null, "게스트");
    }

    public static MeetingAttendanceResponse from(MeetingAttendance attendance) {
        return new MeetingAttendanceResponse(
                attendance.getId(),
                attendance.getParticipantName(),
                attendance.getGender().name(),
                attendance.getAttendanceStatus().name(),
                attendance.getParticipantType().name(),
                attendance.getClubMemberId(),
                attendance.getParticipantType().name().equals("CLUB_MEMBER") ? "클럽원" : "게스트"
        );
    }
}
