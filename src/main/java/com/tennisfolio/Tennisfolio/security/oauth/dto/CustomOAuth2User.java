package com.tennisfolio.Tennisfolio.security.oauth.dto;

import com.tennisfolio.Tennisfolio.common.OAuthProvider;
import com.tennisfolio.Tennisfolio.security.oauth.service.CustomOAuth2UserService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Getter
@AllArgsConstructor
@Builder
public class CustomOAuth2User implements OAuth2User {
    private Long userId;
    private String email;
    private OAuthProvider provider;
    private String providerId;
    private Map<String, Object> attributes;
    private Collection<? extends GrantedAuthority> authorities;

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getName() {
        return String.valueOf(userId);
    }
}
