package com.tennisfolio.Tennisfolio.meeting.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ManagedMeetingParticipantCreateRequest {
    private Long clubMemberId;
    private String participantName;
    private String gender;
    private String attendanceStatus;
}
