package com.tennisfolio.Tennisfolio.security.oauth.domain;

import com.tennisfolio.Tennisfolio.common.OAuthProvider;
import com.tennisfolio.Tennisfolio.common.OAuthStatus;
import com.tennisfolio.Tennisfolio.user.domain.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class OAuthAccount {
    private Long oAuthId;
    private User user;
    private OAuthProvider provider;
    private String providerId;
    private String email;
    private OAuthStatus status;

    public void unlink(){
        this.status = OAuthStatus.UNLINKED;
    }
}
