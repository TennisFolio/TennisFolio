package com.tennisfolio.Tennisfolio.club.dto;

import com.tennisfolio.Tennisfolio.club.entity.ClubAccountInvitation;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ClubAccountInvitationStatusResponse {
    private boolean active;
    private LocalDateTime expiresAt;

    public static ClubAccountInvitationStatusResponse from(ClubAccountInvitation invitation, LocalDateTime now) {
        return new ClubAccountInvitationStatusResponse(invitation.isActiveAt(now), invitation.getExpiresAt());
    }
}
