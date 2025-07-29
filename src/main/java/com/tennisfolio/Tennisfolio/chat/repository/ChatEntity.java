package com.tennisfolio.Tennisfolio.chat.repository;

import com.tennisfolio.Tennisfolio.chat.domain.Chat;
import com.tennisfolio.Tennisfolio.common.ChatMessage;
import com.tennisfolio.Tennisfolio.common.Entity.BaseTimeEntity;
import com.tennisfolio.Tennisfolio.player.domain.Country;
import com.tennisfolio.Tennisfolio.player.infrastructure.CountryEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name="tb_chat")
@NoArgsConstructor
public class ChatEntity extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="CHAT_ID")
    private Long chatId;
    @Column(name="RAPID_MATCH_ID")
    private String rapidMatchId;
    @Column(name="USER_ID")
    private String userId;
    @Column(name="NICKNAME")
    private String nickName;
    @Column(name="MESSAGE")
    private String message;
    @Column(name="TYPE")
    private String type;

    public static ChatEntity fromModel(Chat chat) {
        ChatEntity chatEntity = new ChatEntity();
        chatEntity.chatId = chat.getChatId();
        chatEntity.rapidMatchId = chat.getRapidMatchId();
        chatEntity.userId = chat.getUserId();
        chatEntity.nickName = chat.getNickName();
        chatEntity.message = chat.getMessage();
        chatEntity.type = chat.getType();
        return chatEntity;
    }

    public Chat toModel(){
        return Chat.builder()
                .chatId(chatId)
                .rapidMatchId(rapidMatchId)
                .userId(userId)
                .nickName(nickName)
                .message(message)
                .type(type)
                .build();
    }
}
