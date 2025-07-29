package com.tennisfolio.Tennisfolio.infrastructure.repository;

import com.tennisfolio.Tennisfolio.chat.repository.ChatEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface ChatJpaRepository extends JpaRepository<ChatEntity, Long> {
    List<ChatEntity> findByRapidMatchId(String matchId);
}
