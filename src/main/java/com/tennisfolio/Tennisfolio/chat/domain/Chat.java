package com.tennisfolio.Tennisfolio.chat.domain;

import com.tennisfolio.Tennisfolio.common.ChatMessage;
import com.tennisfolio.Tennisfolio.common.Entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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

    private LocalDateTime timeStamp;

    public Chat(ChatMessage message){
        this.rapidMatchId = message.getMatchId();
        this.userId = message.getUserId();
        this.nickName = message.getSender();
        this.message = message.getMessage();
        this.type = message.getType();
    }
}
