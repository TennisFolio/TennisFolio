package com.tennisfolio.Tennisfolio.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AuthProfileUpdateRequest {
    private String nickName;
    private String gender;
}
