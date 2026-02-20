package com.tennisfolio.Tennisfolio.security.oauth.repository;

import com.tennisfolio.Tennisfolio.common.OAuthProvider;
import com.tennisfolio.Tennisfolio.common.OAuthStatus;
import com.tennisfolio.Tennisfolio.infrastructure.repository.OAuthAccountJpaRepository;
import com.tennisfolio.Tennisfolio.security.oauth.domain.OAuthAccount;
import com.tennisfolio.Tennisfolio.user.domain.User;
import com.tennisfolio.Tennisfolio.user.repository.UserEntity;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class OAuthAccountRepositoryImpl implements OAuthAccountRepository{
    private final OAuthAccountJpaRepository jpaRepository;

    public OAuthAccountRepositoryImpl(OAuthAccountJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<OAuthAccount> findByProviderAndProviderIdAndStatus(OAuthProvider provider, String providerId, OAuthStatus status) {
        return jpaRepository.findByProviderAndProviderIdAndStatus(provider, providerId, status).map(OAuthAccountEntity::toModel);
    }

    @Override
    public Optional<OAuthAccount> findByUserAndStatus(User user, OAuthStatus status){
        return jpaRepository.findByUserAndStatus(UserEntity.fromModel(user), status).map(OAuthAccountEntity::toModel);
    }

    @Override
    public OAuthAccount save(OAuthAccount oAuthAccount) {
        return jpaRepository.save(OAuthAccountEntity.fromModel(oAuthAccount)).toModel();
    }

    @Override
    public List<OAuthAccount> findAllWithUser() {
        return jpaRepository.findAllWithUser().stream().map(OAuthAccountEntity::toModel).toList();
    }
}
