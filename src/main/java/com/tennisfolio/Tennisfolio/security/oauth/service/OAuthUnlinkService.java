package com.tennisfolio.Tennisfolio.security.oauth.service;

import com.tennisfolio.Tennisfolio.common.OAuthStatus;
import com.tennisfolio.Tennisfolio.common.UserStatus;
import com.tennisfolio.Tennisfolio.security.oauth.client.KakaoOAuthClient;
import com.tennisfolio.Tennisfolio.security.oauth.domain.OAuthAccount;
import com.tennisfolio.Tennisfolio.security.oauth.repository.OAuthAccountRepository;
import com.tennisfolio.Tennisfolio.user.domain.User;
import com.tennisfolio.Tennisfolio.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class OAuthUnlinkService {

    private final KakaoOAuthClient kakaoOAuthClient;
    private final OAuthAccountRepository oAuthAccountRepository;
    private final UserRepository userRepository;

    public OAuthUnlinkService(KakaoOAuthClient kakaoOAuthClient, OAuthAccountRepository oAuthAccountRepository, UserRepository userRepository) {
        this.kakaoOAuthClient = kakaoOAuthClient;
        this.oAuthAccountRepository = oAuthAccountRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void unlinkKakao(Long userId){

        User findUser = userRepository.findByIdAndStatus(userId, UserStatus.ACTIVE)
                .orElseThrow(() -> new IllegalStateException("User not found with ID: " + userId));

        OAuthAccount oAuthAccount = oAuthAccountRepository.findByUserAndStatus(findUser, OAuthStatus.LINKED)
                        .orElseThrow(() -> new IllegalStateException("No linked OAuth account found for user ID: " + userId));

        oAuthAccount.unlink();
        kakaoOAuthClient.unlink(oAuthAccount.getProviderId());


    }
}
