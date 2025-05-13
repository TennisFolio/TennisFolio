package com.tennisfolio.Tennisfolio.common;

import com.tennisfolio.Tennisfolio.chat.domain.Chat;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class ChatMessage {
    private String matchId;
    private String sender;
    private String message;
    private String userId;
    private String timeStamp;
    private String type;

    public ChatMessage(String matchId, String sender, String message, String userId, String timeStamp, String type){
        this.matchId = matchId;
        this.sender = sender;
        this.message =  message;
        this.userId = userId;
        this.timeStamp = timeStamp;
        this.type = type;
    }

    public ChatMessage(Chat chat){
        this.matchId = chat.getRapidMatchId();
        this.message = chat.getMessage();
        this.sender = chat.getNickName();
        this.userId = chat.getUserId();
        this.timeStamp = chat.getCreateDt().toString();
        this.type = chat.getType();
    }
}
