package com.tennisfolio.Tennisfolio.meeting.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ManagedMeetingParticipantUpdateRequest {
    private String participantName;
    private String gender;
    private String attendanceStatus;
    private Long clubSkillTierId;
}
