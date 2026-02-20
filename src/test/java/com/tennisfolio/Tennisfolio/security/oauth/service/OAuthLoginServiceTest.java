package com.tennisfolio.Tennisfolio.security.oauth.service;

import com.tennisfolio.Tennisfolio.common.OAuthProvider;
import com.tennisfolio.Tennisfolio.security.oauth.domain.OAuthAccount;
import com.tennisfolio.Tennisfolio.security.oauth.dto.KaKaoOAuthAttributes;
import com.tennisfolio.Tennisfolio.security.oauth.dto.OAuthAttributes;
import com.tennisfolio.Tennisfolio.security.oauth.repository.OAuthAccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest
@ActiveProfiles("mysql-test")
public class OAuthLoginServiceTest {

    @Autowired
    OAuthLoginService oAuthLoginService;

    @Autowired
    OAuthAccountRepository oAuthAccountRepository;

    @Container
    static MySQLContainer<?> mySqlContainer = new MySQLContainer<>("mysql:8.0");

    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mySqlContainer::getJdbcUrl);
        registry.add("spring.datasource.username", mySqlContainer::getUsername);
        registry.add("spring.datasource.password", mySqlContainer::getPassword);
    }

    @Test
    void 동시_생성_시_한_개의_데이터만_생성() throws Exception {

        int threadCount = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        OAuthAttributes attributes =
                new KaKaoOAuthAttributes(Map.of("id", "providerId123", "kakao_account", Map.of(
                        "email", "test@test.com"
                )));

        for(int i = 0; i< threadCount; i++){
            executor.submit(() -> {
                try{
                    oAuthLoginService.findOrCreateAccount(
                            OAuthProvider.KAKAO,
                            attributes
                    );
                } catch(Exception e){
                    e.printStackTrace();
                }
                finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        List<OAuthAccount> accounts = oAuthAccountRepository.findAllWithUser();
        assertThat(accounts).hasSize(1);
        assertThat(accounts.get(0).getProviderId()).isEqualTo("providerId123");
    }
}
