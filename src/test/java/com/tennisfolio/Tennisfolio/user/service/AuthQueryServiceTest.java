package com.tennisfolio.Tennisfolio.user.service;

import com.tennisfolio.Tennisfolio.common.UserStatus;
import com.tennisfolio.Tennisfolio.user.domain.Gender;
import com.tennisfolio.Tennisfolio.user.domain.User;
import com.tennisfolio.Tennisfolio.user.dto.AuthMeResponse;
import com.tennisfolio.Tennisfolio.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthQueryServiceTest {

    @Mock
    UserRepository userRepository;

    @InjectMocks
    AuthQueryService authQueryService;

    @Test
    void getCurrentUser_returnsActiveUser() {
        User user = User.builder()
                .userId(1L)
                .email("user@test.com")
                .nickName("tester")
                .status(UserStatus.ACTIVE)
                .build();
        when(userRepository.findByIdAndStatus(1L, UserStatus.ACTIVE))
                .thenReturn(Optional.of(user));

        AuthMeResponse response = authQueryService.getCurrentUser(1L);

        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getEmail()).isEqualTo("user@test.com");
        assertThat(response.getNickName()).isEqualTo("tester");
    }

    @Test
    void getCurrentUser_throwsUnauthorizedWhenUserMissing() {
        when(userRepository.findByIdAndStatus(1L, UserStatus.ACTIVE))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> authQueryService.getCurrentUser(1L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("401");
    }

    @Test
    void getCurrentUser_marksProfileSetupNeededWhenNicknameMissing() {
        User user = User.builder()
                .userId(1L)
                .email("user@test.com")
                .nickName(null)
                .gender(Gender.MALE)
                .status(UserStatus.ACTIVE)
                .build();
        when(userRepository.findByIdAndStatus(1L, UserStatus.ACTIVE))
                .thenReturn(Optional.of(user));

        AuthMeResponse response = authQueryService.getCurrentUser(1L);

        assertThat(response.isNeedsProfileSetup()).isTrue();
    }

    @Test
    void getCurrentUser_marksProfileSetupNeededWhenGenderMissing() {
        User user = User.builder()
                .userId(1L)
                .email("user@test.com")
                .nickName("tester")
                .gender(null)
                .status(UserStatus.ACTIVE)
                .build();
        when(userRepository.findByIdAndStatus(1L, UserStatus.ACTIVE))
                .thenReturn(Optional.of(user));

        AuthMeResponse response = authQueryService.getCurrentUser(1L);

        assertThat(response.isNeedsProfileSetup()).isTrue();
    }

    @Test
    void getCurrentUser_marksProfileSetupCompleteWhenNicknameAndGenderExist() {
        User user = User.builder()
                .userId(1L)
                .email("user@test.com")
                .nickName("tester")
                .gender(Gender.MALE)
                .status(UserStatus.ACTIVE)
                .build();
        when(userRepository.findByIdAndStatus(1L, UserStatus.ACTIVE))
                .thenReturn(Optional.of(user));

        AuthMeResponse response = authQueryService.getCurrentUser(1L);

        assertThat(response.getGender()).isEqualTo("MALE");
        assertThat(response.isNeedsProfileSetup()).isFalse();
    }
}
