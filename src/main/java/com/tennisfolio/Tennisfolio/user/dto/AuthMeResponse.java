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
    private String gender;
    private boolean needsProfileSetup;

    public static AuthMeResponse from(User user) {
        String gender = user.getGender() == null ? null : user.getGender().name();
        boolean needsProfileSetup = user.getNickName() == null
                || user.getNickName().trim().isEmpty()
                || user.getGender() == null;

        return new AuthMeResponse(
                user.getUserId(),
                user.getEmail(),
                user.getNickName(),
                gender,
                needsProfileSetup
        );
    }
}
