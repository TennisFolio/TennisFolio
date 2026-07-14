package com.tennisfolio.Tennisfolio.meeting.domain;

public record ParticipantResolution(
        String name,
        Gender gender,
        MeetingParticipantType type,
        Long clubMemberId
) {

    public static ParticipantResolution guest(String name, Gender gender) {
        return new ParticipantResolution(name, gender, MeetingParticipantType.GUEST, null);
    }

    public static ParticipantResolution clubMember(String name, Gender gender, Long clubMemberId) {
        return new ParticipantResolution(name, gender, MeetingParticipantType.CLUB_MEMBER, clubMemberId);
    }
}
