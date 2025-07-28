package com.tennisfolio.Tennisfolio.chat.service;

import com.tennisfolio.Tennisfolio.common.ChatMessage;

import java.util.List;

public interface ChatService {
    void saveChat(ChatMessage message);
    List<ChatMessage> getChatByMatchId(String matchId);
}
