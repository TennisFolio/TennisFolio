package com.tennisfolio.Tennisfolio.user.service;

import com.tennisfolio.Tennisfolio.common.UserStatus;
import com.tennisfolio.Tennisfolio.user.domain.Gender;
import com.tennisfolio.Tennisfolio.user.domain.User;
import com.tennisfolio.Tennisfolio.user.dto.AuthMeResponse;
import com.tennisfolio.Tennisfolio.user.dto.AuthProfileUpdateRequest;
import com.tennisfolio.Tennisfolio.user.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthProfileService {
    private static final int MAX_NICKNAME_LENGTH = 10;

    private final UserRepository userRepository;

    public AuthProfileService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public AuthMeResponse updateProfile(Long userId, AuthProfileUpdateRequest request) {
        String nickName = validateNickName(request.getNickName());
        Gender gender = parseGender(request.getGender());
        User user = userRepository.findByIdAndStatus(userId, UserStatus.ACTIVE)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found")
                );

        User saved = userRepository.save(user.updateProfile(nickName, gender));
        return AuthMeResponse.from(saved);
    }

    private String validateNickName(String nickName) {
        String trimmed = nickName == null ? "" : nickName.trim();
        if (trimmed.isEmpty() || trimmed.length() > MAX_NICKNAME_LENGTH) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Name must be 1 to 10 characters"
            );
        }
        return trimmed;
    }

    private Gender parseGender(String gender) {
        try {
            return Gender.valueOf(gender);
        } catch (RuntimeException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Gender must be MALE or FEMALE"
            );
        }
    }
}
