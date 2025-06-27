package com.tennisfolio.Tennisfolio.chat.controller;

import com.tennisfolio.Tennisfolio.chat.service.ChatService;
import com.tennisfolio.Tennisfolio.common.ChatMessage;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class ChatSocketController {

    private final ChatService chatService;

    public ChatSocketController(ChatService chatService) {
        this.chatService = chatService;
    }

    @MessageMapping("/chat.send/{matchId}")
    @SendTo("/topic/match.{matchId}")
    public ChatMessage send(@Payload ChatMessage message, @DestinationVariable("matchId") String matchId){
        message.mappingMatch(matchId);
        chatService.saveChat(message);
        return message;
    }

}
