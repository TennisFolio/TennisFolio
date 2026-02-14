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
    private UserStatus status;
}
