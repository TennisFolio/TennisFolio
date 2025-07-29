package com.tennisfolio.Tennisfolio.chat.repository;

import com.tennisfolio.Tennisfolio.common.ChatMessage;
import com.tennisfolio.Tennisfolio.common.Entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name="tb_chat")
@NoArgsConstructor
public class Chat extends BaseTimeEntity {
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

    public Chat(ChatMessage message){
        this.rapidMatchId = message.getMatchId();
        this.userId = message.getUserId();
        this.nickName = message.getSender();
        this.message = message.getMessage();
        this.type = message.getType();
    }
}
