package com.tennisfolio.Tennisfolio.security.oauth.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
public class KaKaoOAuthAttributes implements OAuthAttributes {
    private final Map<String, Object> attributes;

    public KaKaoOAuthAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String getProviderId() {
        return String.valueOf(attributes.get("id"));
    }

    @Override
    public String getEmail() {
        Map<String, Object> account =
                (Map<String, Object>) attributes.get("kakao_account");
        return (String) account.get("email");
    }
}
