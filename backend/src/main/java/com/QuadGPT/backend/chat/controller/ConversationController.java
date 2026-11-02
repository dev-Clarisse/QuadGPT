package com.QuadGPT.backend.chat.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.QuadGPT.backend.chat.dto.ConversationResponse;
import com.QuadGPT.backend.chat.dto.MessageResponse;
import com.QuadGPT.backend.chat.service.ChatService;

@RestController
@RequestMapping("/api/conversations")
public class ConversationController {

    private final ChatService chatService;

    public ConversationController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping
    public List<ConversationResponse> list() {
        return chatService.listConversations();
    }

    @GetMapping("/{id}/messages")
    public List<MessageResponse> messages(@PathVariable Long id) {
        return chatService.getMessages(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        chatService.deleteConversation(id);
        return ResponseEntity.noContent().build();
    }
}
