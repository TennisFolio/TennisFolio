package com.tennisfolio.Tennisfolio.user.repository;

import com.tennisfolio.Tennisfolio.common.UserStatus;
import com.tennisfolio.Tennisfolio.infrastructure.repository.UserJpaRepository;
import com.tennisfolio.Tennisfolio.user.domain.User;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserRepositoryImpl implements UserRepository {
    private final UserJpaRepository userJpaRepository;

    public UserRepositoryImpl(UserJpaRepository userJpaRepository) {
        this.userJpaRepository = userJpaRepository;
    }

    @Override
    public Optional<User> findByEmailAndStatus(String email, UserStatus status) {
        return userJpaRepository.findByEmailAndStatus(email, status).map(UserEntity::toModel);
    }

    @Override
    public Optional<User> findByIdAndStatus(Long id, UserStatus status) {
        return userJpaRepository.findByUserIdAndStatus(id, status).map(UserEntity::toModel);
    }

    @Override
    public User save(User user) {
        return userJpaRepository.save(UserEntity.fromModel(user)).toModel();
    }
}
