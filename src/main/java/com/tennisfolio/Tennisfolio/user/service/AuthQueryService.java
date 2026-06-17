package com.tennisfolio.Tennisfolio.user.service;

import com.tennisfolio.Tennisfolio.common.UserStatus;
import com.tennisfolio.Tennisfolio.user.domain.User;
import com.tennisfolio.Tennisfolio.user.dto.AuthMeResponse;
import com.tennisfolio.Tennisfolio.user.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthQueryService {

    private final UserRepository userRepository;

    public AuthQueryService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public AuthMeResponse getCurrentUser(Long userId) {
        User user = userRepository.findByIdAndStatus(userId, UserStatus.ACTIVE)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found")
                );

        return AuthMeResponse.from(user);
    }
}
