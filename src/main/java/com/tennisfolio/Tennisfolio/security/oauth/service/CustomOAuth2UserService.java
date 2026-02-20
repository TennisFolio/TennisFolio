package com.tennisfolio.Tennisfolio.security.oauth.service;

import com.tennisfolio.Tennisfolio.common.OAuthProvider;
import com.tennisfolio.Tennisfolio.common.OAuthStatus;
import com.tennisfolio.Tennisfolio.common.UserStatus;
import com.tennisfolio.Tennisfolio.security.oauth.domain.OAuthAccount;
import com.tennisfolio.Tennisfolio.security.oauth.dto.CustomOAuth2User;
import com.tennisfolio.Tennisfolio.security.oauth.dto.OAuthAttributes;
import com.tennisfolio.Tennisfolio.security.oauth.dto.OAuthAttributesFactory;
import com.tennisfolio.Tennisfolio.security.oauth.repository.OAuthAccountRepository;
import com.tennisfolio.Tennisfolio.user.domain.User;
import com.tennisfolio.Tennisfolio.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
public class CustomOAuth2UserService implements OAuth2UserService {
    private final OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate;

    private final OAuthLoginService oAuthLoginService;

    public CustomOAuth2UserService(@Qualifier("defaultOAuth2UserService")
                                   OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate, OAuthLoginService oAuthLoginService) {
        this.delegate = delegate;
        this.oAuthLoginService = oAuthLoginService;
    }

    @Override
    @Transactional
    public CustomOAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2User oauth2User = loadOAuthUser(userRequest);

        OAuthProvider provider = resolveProvider(userRequest);

        OAuthAttributes oAuthAttributes = extractAttributes(provider, oauth2User);

        OAuthAccount account = oAuthLoginService.findOrCreateAccount(provider, oAuthAttributes);

        User user = validateUser(account);

        return buildPrincipal(oauth2User, account, user);
    }



    private OAuthAttributes extractAttributes(OAuthProvider provider, OAuth2User oauth2User) {
        return OAuthAttributesFactory.of(provider, oauth2User.getAttributes());
    }

    private OAuthProvider resolveProvider(OAuth2UserRequest userRequest) {
        OAuthProvider provider =
                OAuthProvider.getOAuthProvider(userRequest.getClientRegistration().getRegistrationId());
        return provider;
    }

    private OAuth2User loadOAuthUser(OAuth2UserRequest userRequest) {
        OAuth2User oauth2User = delegate.loadUser(userRequest);
        return oauth2User;
    }

    private User validateUser(OAuthAccount account) {

        User user = account.getUser();

        if (user == null) {
            throw new IllegalStateException("Orphan OAuthAccount exists.");
        }

        return user;
    }

    private CustomOAuth2User buildPrincipal(OAuth2User oauth2User,
                                            OAuthAccount account,
                                            User user) {

        return CustomOAuth2User.builder()
                .attributes(oauth2User.getAttributes())
                .authorities(oauth2User.getAuthorities())
                .userId(user.getUserId())
                .email(account.getEmail())
                .provider(account.getProvider())
                .providerId(account.getProviderId())
                .build();
    }
}
