package com.QuadGPT.backend.chat.dto;

import java.time.LocalDateTime;

public record ConversationResponse(
        Long id,
        String title,
        LocalDateTime updatedAt
) {}
