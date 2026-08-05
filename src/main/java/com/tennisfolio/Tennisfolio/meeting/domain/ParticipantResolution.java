package com.tennisfolio.Tennisfolio.meeting.domain;

public record ParticipantResolution(
        String name,
        Gender gender,
        MeetingParticipantType type,
        Long clubMemberId,
        Long clubSkillTierId,
        String clubSkillTierName
) {

    public static ParticipantResolution guest(String name, Gender gender) {
        return new ParticipantResolution(name, gender, MeetingParticipantType.GUEST, null, null, null);
    }

    public static ParticipantResolution clubMember(String name, Gender gender, Long clubMemberId) {
        return clubMember(name, gender, clubMemberId, null, null);
    }

    public static ParticipantResolution clubMember(
            String name,
            Gender gender,
            Long clubMemberId,
            Long clubSkillTierId,
            String clubSkillTierName
    ) {
        return new ParticipantResolution(
                name,
                gender,
                MeetingParticipantType.CLUB_MEMBER,
                clubMemberId,
                clubSkillTierId,
                clubSkillTierName
        );
    }
}
