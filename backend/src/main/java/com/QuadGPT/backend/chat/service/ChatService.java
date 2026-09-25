package com.QuadGPT.backend.chat.service;

import org.springframework.stereotype.Service;

@Service
public class ChatService {

    public String processMessage(String message) {
        return "Message received: " + message;
    }
}