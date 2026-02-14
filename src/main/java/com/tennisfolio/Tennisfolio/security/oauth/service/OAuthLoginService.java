package com.tennisfolio.Tennisfolio.security.oauth.service;

import com.tennisfolio.Tennisfolio.common.OAuthProvider;
import com.tennisfolio.Tennisfolio.common.OAuthStatus;
import com.tennisfolio.Tennisfolio.common.UserStatus;
import com.tennisfolio.Tennisfolio.security.oauth.domain.OAuthAccount;
import com.tennisfolio.Tennisfolio.security.oauth.dto.OAuthAttributes;
import com.tennisfolio.Tennisfolio.security.oauth.repository.OAuthAccountRepository;
import com.tennisfolio.Tennisfolio.user.domain.User;
import com.tennisfolio.Tennisfolio.user.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Optional;

@Service
public class OAuthLoginService {

    private final OAuthAccountRepository oAuthAccountRepository;
    private final UserRepository userRepository;
    private final TransactionTemplate requiresNewTx;

    public OAuthLoginService(OAuthAccountRepository oAuthAccountRepository, UserRepository userRepository, PlatformTransactionManager txManager) {
        this.oAuthAccountRepository = oAuthAccountRepository;
        this.userRepository = userRepository;

        TransactionTemplate template = new TransactionTemplate(txManager);
        template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        this.requiresNewTx = template;
    }

    public OAuthAccount findOrCreateAccount(OAuthProvider provider,
                                            OAuthAttributes attributes) {

        return findExistingAccount(provider, attributes)
                .orElseGet(() -> createAccountWithRetry(provider, attributes));
    }

    private Optional<OAuthAccount> findExistingAccount(OAuthProvider provider, OAuthAttributes attributes){
        return oAuthAccountRepository
                .findByProviderAndProviderIdAndStatus(
                        provider,
                        attributes.getProviderId(),
                        OAuthStatus.LINKED
                );
    }

    // ✅ 3. 생성 + retry 전략
    private OAuthAccount createAccountWithRetry(OAuthProvider provider,
                                                OAuthAttributes attributes) {

        try {
            return tryCreateAccountInNewTx(provider, attributes);
        } catch (DataIntegrityViolationException e) {
            return findExistingAccountWithRetry(provider, attributes);
        }
    }


    // ✅ 4. 실제 생성 시도 (트랜잭션 경계)
    private OAuthAccount tryCreateAccountInNewTx(OAuthProvider provider,
                                                 OAuthAttributes attributes) {

        return requiresNewTx.execute(status -> {

            User user = findOrCreateUser(attributes.getEmail());

            return oAuthAccountRepository.save(
                    OAuthAccount.builder()
                            .user(user)
                            .status(OAuthStatus.LINKED)
                            .provider(provider)
                            .providerId(attributes.getProviderId())
                            .email(attributes.getEmail())
                            .build()
            );
        });
    }

    // ✅ 5. retry 조회 전략
    private OAuthAccount findExistingAccountWithRetry(OAuthProvider provider,
                                                      OAuthAttributes attributes) {

        for (int i = 0; i < 5; i++) {

            Optional<OAuthAccount> account =
                    findExistingAccount(provider, attributes);

            if (account.isPresent()) {
                return account.get();
            }

            sleepBriefly();
        }

        throw new IllegalStateException("Account not found after retry");
    }

    // ✅ 6. User 생성 전략
    private User findOrCreateUser(String email) {

        return userRepository.findByEmailAndStatus(email, UserStatus.ACTIVE)
                .orElseGet(() -> userRepository.save(
                        User.builder()
                                .email(email)
                                .status(UserStatus.ACTIVE)
                                .build()
                ));
    }

    private void sleepBriefly() {
        try {
            Thread.sleep(10);
        } catch (InterruptedException ignored) {
        }
    }

}
