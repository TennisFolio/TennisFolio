package com.tennisfolio.Tennisfolio.club.dto;

import com.tennisfolio.Tennisfolio.club.entity.ClubAccount;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ClubAccountResponse {
    private Long id;
    private Long accountId;
    private String displayName;
    private String role;

    public static ClubAccountResponse from(ClubAccount account) {
        return new ClubAccountResponse(
                account.getId(),
                account.getAccountId(),
                account.getDisplayName(),
                account.getRole().name()
        );
    }
}
