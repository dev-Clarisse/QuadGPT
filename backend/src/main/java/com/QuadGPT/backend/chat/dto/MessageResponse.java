package com.QuadGPT.backend.chat.dto;

import java.time.LocalDateTime;

public record MessageResponse(
        String role,
        String content,
        LocalDateTime createdAt
) {}
