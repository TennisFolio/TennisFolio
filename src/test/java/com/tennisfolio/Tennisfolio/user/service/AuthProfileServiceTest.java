package com.tennisfolio.Tennisfolio.user.service;

import com.tennisfolio.Tennisfolio.common.UserStatus;
import com.tennisfolio.Tennisfolio.user.domain.Gender;
import com.tennisfolio.Tennisfolio.user.domain.User;
import com.tennisfolio.Tennisfolio.user.dto.AuthMeResponse;
import com.tennisfolio.Tennisfolio.user.dto.AuthProfileUpdateRequest;
import com.tennisfolio.Tennisfolio.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthProfileServiceTest {

    @Mock
    UserRepository userRepository;

    @InjectMocks
    AuthProfileService authProfileService;

    @Test
    void updateProfile_savesTrimmedNicknameAndGender() {
        User user = User.builder()
                .userId(1L)
                .email("user@test.com")
                .status(UserStatus.ACTIVE)
                .build();
        when(userRepository.findByIdAndStatus(1L, UserStatus.ACTIVE))
                .thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AuthMeResponse response = authProfileService.updateProfile(
                1L,
                new AuthProfileUpdateRequest(" tester ", "MALE")
        );

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getNickName()).isEqualTo("tester");
        assertThat(captor.getValue().getGender()).isEqualTo(Gender.MALE);
        assertThat(response.isNeedsProfileSetup()).isFalse();
    }

    @Test
    void updateProfile_rejectsBlankName() {
        assertThatThrownBy(() -> authProfileService.updateProfile(
                1L,
                new AuthProfileUpdateRequest(" ", "MALE")
        )).isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("400");
    }

    @Test
    void updateProfile_rejectsNameLongerThanTen() {
        assertThatThrownBy(() -> authProfileService.updateProfile(
                1L,
                new AuthProfileUpdateRequest("12345678901", "MALE")
        )).isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("400");
    }

    @Test
    void updateProfile_rejectsInvalidGender() {
        assertThatThrownBy(() -> authProfileService.updateProfile(
                1L,
                new AuthProfileUpdateRequest("tester", "UNKNOWN")
        )).isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("400");
    }
}
