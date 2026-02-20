package com.tennisfolio.Tennisfolio.security.oauth.dto;

import com.tennisfolio.Tennisfolio.common.OAuthProvider;

import java.util.Map;

public class OAuthAttributesFactory {

    public static OAuthAttributes of(OAuthProvider provider, Map<String, Object> attributes) {
        switch (provider) {
            case KAKAO:
               return new KaKaoOAuthAttributes(attributes);
            // Add other providers here
            case NAVER:
                return new NaverOAuthAttributes(attributes);
            default:
                throw new IllegalArgumentException("Unsupported OAuth provider: " + provider);
        }
    }
}
