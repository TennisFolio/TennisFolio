package com.tennisfolio.Tennisfolio.chat.repository;

import com.tennisfolio.Tennisfolio.chat.domain.Chat;

import java.util.List;

public interface ChatRepository {
    List<Chat> findByRapidMatchId(String matchId);
    Chat save(Chat chat);
}
