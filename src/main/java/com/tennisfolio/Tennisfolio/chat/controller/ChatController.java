package com.tennisfolio.Tennisfolio.chat.controller;

import com.tennisfolio.Tennisfolio.chat.service.ChatService;
import com.tennisfolio.Tennisfolio.common.ChatMessage;
import com.tennisfolio.Tennisfolio.common.response.ResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController()
@RequestMapping("/api/chat")
public class ChatController {
    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/{matchId}")
    public ResponseEntity<ResponseDTO<List<ChatMessage>>> getChatList(@PathVariable("matchId") String matchId){
        List<ChatMessage> chat = chatService.getChatByMatchId(matchId);
        return new ResponseEntity(ResponseDTO.success(chat), HttpStatus.OK);
    }
}
