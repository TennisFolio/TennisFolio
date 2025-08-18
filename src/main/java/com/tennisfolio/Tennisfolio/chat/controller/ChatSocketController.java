package com.tennisfolio.Tennisfolio.chat.controller;

import com.tennisfolio.Tennisfolio.chat.service.ChatService;
import com.tennisfolio.Tennisfolio.common.ChatMessage;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class ChatSocketController {

    private final ChatService chatService;
    private final SimpMessagingTemplate template;

    public ChatSocketController(ChatService chatService, SimpMessagingTemplate template) {
        this.chatService = chatService;
        this.template = template;
    }

    @MessageMapping("/chat.send/{matchId}")
    public ChatMessage send(@Payload ChatMessage message, @DestinationVariable("matchId") String matchId){
        message.mappingMatch(matchId);

        String topic = "/topic/match." + matchId;
        template.convertAndSend(topic, message);
        return chatService.saveChat(message);
    }

}
