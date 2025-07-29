package com.tennisfolio.Tennisfolio.chat.domain;

import com.tennisfolio.Tennisfolio.common.ChatMessage;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
public class Chat {

    private Long chatId;

    private String rapidMatchId;

    private String userId;

    private String nickName;

    private String message;

    private String type;

    public Chat(ChatMessage message){
        this.rapidMatchId = message.getMatchId();
        this.userId = message.getUserId();
        this.nickName = message.getSender();
        this.message = message.getMessage();
        this.type = message.getType();
    }
}
