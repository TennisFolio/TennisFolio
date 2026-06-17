package com.tennisfolio.Tennisfolio.user.dto;

import com.tennisfolio.Tennisfolio.user.domain.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthMeResponse {
    private Long userId;
    private String email;
    private String nickName;

    public static AuthMeResponse from(User user) {
        return new AuthMeResponse(
                user.getUserId(),
                user.getEmail(),
                user.getNickName()
        );
    }
}
