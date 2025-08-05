package com.tennisfolio.Tennisfolio.chat.service;

import com.tennisfolio.Tennisfolio.chat.domain.Chat;
import com.tennisfolio.Tennisfolio.chat.repository.ChatEntity;
import com.tennisfolio.Tennisfolio.chat.repository.ChatRepository;
import com.tennisfolio.Tennisfolio.infrastructure.repository.ChatJpaRepository;
import com.tennisfolio.Tennisfolio.common.ChatMessage;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatServiceImpl implements ChatService{

    private final ChatRepository chatRepository;

    public ChatServiceImpl(ChatRepository chatRepository) {
        this.chatRepository = chatRepository;
    }

    @Override
    public void saveChat(ChatMessage message) {
        Chat chat = new Chat(message);

        chatRepository.save(chat);
    }

    @Override
    public List<ChatMessage> getChatByMatchId(String matchId) {
        return chatRepository.findByRapidMatchId(matchId).stream()
                .map(chat -> new ChatMessage(ChatEntity.fromModel(chat)))
                .collect(Collectors.toList());
    }
}
