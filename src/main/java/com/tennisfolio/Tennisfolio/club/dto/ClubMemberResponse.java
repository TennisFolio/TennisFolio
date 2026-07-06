package com.tennisfolio.Tennisfolio.club.dto;

import com.tennisfolio.Tennisfolio.club.entity.ClubMember;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ClubMemberResponse {
    private Long id;
    private Long userId;
    private String name;
    private String gender;
    private String role;
    private String skillNote;
    private String contactMemo;
    private String memo;

    public static ClubMemberResponse from(ClubMember member) {
        return new ClubMemberResponse(
                member.getId(),
                member.getUserId(),
                member.getName(),
                member.getGender().name(),
                member.getRole().name(),
                member.getSkillNote(),
                member.getContactMemo(),
                member.getMemo()
        );
    }
}
