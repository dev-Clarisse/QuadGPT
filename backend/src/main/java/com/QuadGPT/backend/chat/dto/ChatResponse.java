package com.QuadGPT.backend.chat.dto;

public class ChatResponse {

    private String response;
    private Long conversationId;
    private String title;

    public ChatResponse(String response, Long conversationId, String title) {
        this.response = response;
        this.conversationId = conversationId;
        this.title = title;
    }

    public String getResponse() {
        return response;
    }

    public Long getConversationId() {
        return conversationId;
    }

    public String getTitle() {
        return title;
    }
}
