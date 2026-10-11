package com.tennisfolio.Tennisfolio.club.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ClubAccountInvitationIssueResponse {
    private String token;
    private LocalDateTime expiresAt;
}
