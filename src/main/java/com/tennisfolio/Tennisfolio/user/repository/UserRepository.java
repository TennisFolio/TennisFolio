package com.tennisfolio.Tennisfolio.user.repository;

import com.tennisfolio.Tennisfolio.common.UserStatus;
import com.tennisfolio.Tennisfolio.user.domain.User;

import java.util.Optional;

public interface UserRepository {
    Optional<User> findByEmailAndStatus(String email, UserStatus status);
    Optional<User> findByIdAndStatus(Long id, UserStatus status);
    User save(User user);
}
