package com.tennisfolio.Tennisfolio.security.oauth.repository;

import com.tennisfolio.Tennisfolio.common.OAuthProvider;
import com.tennisfolio.Tennisfolio.common.OAuthStatus;
import com.tennisfolio.Tennisfolio.security.oauth.domain.OAuthAccount;
import com.tennisfolio.Tennisfolio.user.domain.User;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


public interface OAuthAccountRepository {
    Optional<OAuthAccount> findByProviderAndProviderIdAndStatus(OAuthProvider provider, String providerId, OAuthStatus status);
    Optional<OAuthAccount> findByUserAndStatus(User user, OAuthStatus status);
    OAuthAccount save(OAuthAccount oAuthAccount);

    List<OAuthAccount> findAllWithUser();
}
