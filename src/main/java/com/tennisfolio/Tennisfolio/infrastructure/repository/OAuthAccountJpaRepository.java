package com.tennisfolio.Tennisfolio.infrastructure.repository;

import com.tennisfolio.Tennisfolio.common.OAuthProvider;
import com.tennisfolio.Tennisfolio.common.OAuthStatus;
import com.tennisfolio.Tennisfolio.security.oauth.repository.OAuthAccountEntity;
import com.tennisfolio.Tennisfolio.user.domain.User;
import com.tennisfolio.Tennisfolio.user.repository.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OAuthAccountJpaRepository extends JpaRepository<OAuthAccountEntity, Long> {
    @Query("""
    SELECT oa
    FROM OAuthAccountEntity oa
    JOIN FETCH oa.user u
    WHERE oa.provider = :provider
      AND oa.providerId = :providerId
      AND oa.status = :status
            """)
    Optional<OAuthAccountEntity> findByProviderAndProviderIdAndStatus(@Param("provider") OAuthProvider provider,
                                                                      @Param("providerId")String providerId,
                                                                      @Param("status")OAuthStatus status);

    Optional<OAuthAccountEntity> findByUserAndStatus(UserEntity user, OAuthStatus status);

    @Query("""
            SELECT oa FROM OAuthAccountEntity oa
            JOIN FETCH oa.user u
            """)
    List<OAuthAccountEntity> findAllWithUser();
}
