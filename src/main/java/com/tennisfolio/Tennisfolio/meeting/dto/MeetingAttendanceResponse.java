package com.tennisfolio.Tennisfolio.meeting.dto;

import com.tennisfolio.Tennisfolio.meeting.entity.MeetingAttendance;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

@Getter
@AllArgsConstructor
public class MeetingAttendanceResponse {
    private Long id;
    private String participantName;
    private String gender;
    private String attendanceStatus;
    private String participantType;
    private Long clubMemberId;
    private Long clubSkillTierId;
    private String clubSkillTierName;
    private String badgeLabel;

    public MeetingAttendanceResponse(
            Long id,
            String participantName,
            String gender,
            String attendanceStatus
    ) {
        this(id, participantName, gender, attendanceStatus, "GUEST", null, null, null, "게스트");
    }

    public static MeetingAttendanceResponse from(MeetingAttendance attendance) {
        return from(attendance, Map.of());
    }

    public static MeetingAttendanceResponse from(
            MeetingAttendance attendance,
            Map<Long, String> skillTierNames
    ) {
        Long clubSkillTierId = attendance.getClubSkillTierId();
        return new MeetingAttendanceResponse(
                attendance.getId(),
                attendance.getParticipantName(),
                attendance.getGender().name(),
                attendance.getAttendanceStatus().name(),
                attendance.getParticipantType().name(),
                attendance.getClubMemberId(),
                clubSkillTierId,
                clubSkillTierId == null ? null : skillTierNames.get(clubSkillTierId),
                attendance.getParticipantType().name().equals("CLUB_MEMBER") ? "클럽원" : "게스트"
        );
    }
}
