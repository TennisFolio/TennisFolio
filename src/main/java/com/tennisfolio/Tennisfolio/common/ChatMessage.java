package com.tennisfolio.Tennisfolio.common;

import com.tennisfolio.Tennisfolio.chat.domain.Chat;
import com.tennisfolio.Tennisfolio.util.ConversionUtil;
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
    private String timestamp;
    private String type;

    public ChatMessage(String matchId, String sender, String message, String userId, String timestamp, String type){
        this.matchId = matchId;
        this.sender = sender;
        this.message =  message;
        this.userId = userId;
        this.timestamp = timestamp;
        this.type = type;
    }

    public ChatMessage(Chat chat){
        this.matchId = chat.getRapidMatchId();
        this.message = chat.getMessage();
        this.sender = chat.getNickName();
        this.userId = chat.getUserId();
        this.timestamp = ConversionUtil.timestampToHHmm(chat.getCreateDt());
        this.type = chat.getType();
    }
}
