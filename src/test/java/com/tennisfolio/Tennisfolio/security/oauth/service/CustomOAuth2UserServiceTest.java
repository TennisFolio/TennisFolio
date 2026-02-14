package com.tennisfolio.Tennisfolio.security.oauth.service;

import com.tennisfolio.Tennisfolio.common.OAuthProvider;
import com.tennisfolio.Tennisfolio.security.oauth.domain.OAuthAccount;
import com.tennisfolio.Tennisfolio.security.oauth.dto.CustomOAuth2User;
import com.tennisfolio.Tennisfolio.security.oauth.dto.OAuthAttributes;
import com.tennisfolio.Tennisfolio.user.domain.User;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CustomOAuth2UserServiceTest {

    @Mock
    OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate;

    @Mock
    OAuthLoginService oAuthLoginService;

    @InjectMocks
    CustomOAuth2UserService customOAuth2UserService;

    @Test
    void loadUser_성공(){

        OAuth2UserRequest userRequest = mockUserRequest("kakao");

        OAuth2User oauth2User = mockOAuthUser();

        when(delegate.loadUser(userRequest)).thenReturn(oauth2User);

        User user = mock(User.class);
        when(user.getUserId()).thenReturn(1L);

        OAuthAccount account = mock(OAuthAccount.class);
        when(account.getUser()).thenReturn(user);
        when(account.getEmail()).thenReturn("test@test.com");
        when(account.getProvider()).thenReturn(OAuthProvider.KAKAO);
        when(account.getProviderId()).thenReturn("123");

        when(oAuthLoginService.findOrCreateAccount(any(), any()))
                .thenReturn(account);

        CustomOAuth2User result =
                customOAuth2UserService.loadUser(userRequest);

        assertThat(result.getUserId()).isEqualTo(1L);
        assertThat(result.getEmail()).isEqualTo("test@test.com");
        verify(oAuthLoginService).findOrCreateAccount(any(), any());
    }

    @Test
    void loadUser_orphanAccount_throw(){

        OAuth2UserRequest userRequest = mockUserRequest("kakao");

        OAuth2User oauth2User = mockOAuthUser();

        when(delegate.loadUser(userRequest)).thenReturn(oauth2User);


        OAuthAccount account = mock(OAuthAccount.class);
        when(account.getUser()).thenReturn(null);


        when(oAuthLoginService.findOrCreateAccount(any(), any()))
                .thenReturn(account);


        assertThrows(IllegalStateException.class, () -> {
            customOAuth2UserService.loadUser(userRequest);
        });
    }

    @Test
    void loadUser_invalidProvider_throwException() {
        OAuth2UserRequest request = mockUserRequest("unknown");

        when(delegate.loadUser(request)).thenReturn(mockOAuthUser());

        assertThrows(IllegalArgumentException.class,
                () -> customOAuth2UserService.loadUser(request));
    }

    @Test
    void loadUser_attributesPassCorrectly(){
        OAuth2UserRequest request = mockUserRequest("kakao");

        OAuth2User oauthUser = mockOAuthUser();

        when(delegate.loadUser(request)).thenReturn(oauthUser);

        User user = mock(User.class);
        when(user.getUserId()).thenReturn(1L);

        OAuthAccount account = mock(OAuthAccount.class);

        when(account.getUser()).thenReturn(user);
        when(account.getEmail()).thenReturn("test@test.com");
        when(account.getProvider()).thenReturn(OAuthProvider.KAKAO);
        when(account.getProviderId()).thenReturn("123");

        when(oAuthLoginService.findOrCreateAccount(any(), any()))
                .thenReturn(account);

        ArgumentCaptor<OAuthAttributes> captor =
                ArgumentCaptor.forClass(OAuthAttributes.class);

        // When
        customOAuth2UserService.loadUser(request);

        // Then
        verify(oAuthLoginService).findOrCreateAccount(eq(OAuthProvider.KAKAO), captor.capture());

        OAuthAttributes captured = captor.getValue();

        assertThat(captured.getProviderId()).isEqualTo("123");
    }

    private static @NotNull OAuth2User mockOAuthUser() {
        OAuth2User oauth2User =
                 new DefaultOAuth2User(
                        List.of(new SimpleGrantedAuthority("ROLE_USER")),
                         Map.of("id", "123"),
                        "id"
                );
        return oauth2User;
    }

    private static @NotNull OAuth2UserRequest mockUserRequest(String registrationId) {
        OAuth2UserRequest userRequest = mock(OAuth2UserRequest.class);
        ClientRegistration registration = mock(ClientRegistration.class);

        when(userRequest.getClientRegistration()).thenReturn(registration);
        when(registration.getRegistrationId()).thenReturn(registrationId);
        return userRequest;
    }


}
