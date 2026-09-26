package com.QuadGPT.backend.chat.service;

import com.QuadGPT.backend.rag.RagService;
import org.springframework.stereotype.Service;

@Service
public class ChatService {

    private final RagService ragService;

    public ChatService(RagService ragService) {
        this.ragService = ragService;
    }

    public String processMessage(String message, String department) {
        return ragService.ask(message, department);
    }
}