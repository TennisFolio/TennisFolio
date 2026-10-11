package com.tennisfolio.Tennisfolio.club.dto;

import com.tennisfolio.Tennisfolio.club.entity.ClubAccount;
import com.tennisfolio.Tennisfolio.user.domain.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ClubAccountResponse {
    private Long id;
    private Long accountId;
    private String displayName;
    private String role;

    public static ClubAccountResponse from(ClubAccount account, User user) {
        return new ClubAccountResponse(
                account.getId(),
                account.getAccountId(),
                user.getNickName(),
                account.getRole().name()
        );
    }
}
