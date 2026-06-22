package com.tennisfolio.Tennisfolio.user.domain;

import com.tennisfolio.Tennisfolio.common.UserStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class User {
    private Long userId;
    private String email;
    private String nickName;
    private Gender gender;
    private UserStatus status;

    public User updateProfile(String nickName, Gender gender) {
        return User.builder()
                .userId(this.userId)
                .email(this.email)
                .nickName(nickName)
                .gender(gender)
                .status(this.status)
                .build();
    }
}
