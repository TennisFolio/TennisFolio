package com.tennisfolio.Tennisfolio.security.oauth.dto;

import java.util.Map;

public class NaverOAuthAttributes implements OAuthAttributes{

    private final Map<String, Object> attributes;

    public NaverOAuthAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;

        System.out.println(attributes.get("response"));
    }

    @Override
    public String getProviderId() {
        return ((Map)attributes.get("response")).get("id").toString();
    }

    @Override
    public String getEmail() {
        return ((Map)attributes.get("response")).get("email").toString();
    }
}
