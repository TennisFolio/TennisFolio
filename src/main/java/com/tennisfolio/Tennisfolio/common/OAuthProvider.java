package com.tennisfolio.Tennisfolio.common;

public enum OAuthProvider {
    GOOGLE("google"),
    NAVER("naver"),
    KAKAO("kakao");

    private final String name;

    OAuthProvider(String name) {
        this.name = name;
    }

    public static OAuthProvider getOAuthProvider(String name) {
        for (OAuthProvider provider : OAuthProvider.values()) {
            if (provider.name.equalsIgnoreCase(name)) {
                return provider;
            }
        }
        throw new IllegalArgumentException("No matching OAuthProvider for name: " + name);
    }
}
