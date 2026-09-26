package com.QuadGPT.backend.chat.controller;

import com.QuadGPT.backend.chat.dto.ChatRequest;
import com.QuadGPT.backend.chat.dto.ChatResponse;
import com.QuadGPT.backend.chat.service.ChatService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping
    public ResponseEntity<ChatResponse> chat(
            @Valid @RequestBody ChatRequest request
    ) {
        String response = chatService.processMessage(
                request.getMessage()
        );

        return ResponseEntity.ok(
                new ChatResponse(response)
        );
    }
}
