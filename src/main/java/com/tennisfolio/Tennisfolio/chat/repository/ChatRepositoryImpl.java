package com.tennisfolio.Tennisfolio.chat.repository;

import com.tennisfolio.Tennisfolio.chat.domain.Chat;
import com.tennisfolio.Tennisfolio.infrastructure.repository.ChatJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ChatRepositoryImpl implements ChatRepository{
    private final ChatJpaRepository chatJpaRepository;

    public ChatRepositoryImpl(ChatJpaRepository chatJpaRepository) {
        this.chatJpaRepository = chatJpaRepository;
    }

    @Override
    public List<Chat> findByRapidMatchId(String matchId) {
        return chatJpaRepository.findByRapidMatchId(matchId).stream().map(ChatEntity::toModel).toList();
    }

    @Override
    public Chat save(Chat chat) {
        return chatJpaRepository.save(chat);
    }
}
