package com.QuadGPT.backend.chat.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.QuadGPT.backend.chat.entity.ChatMessage;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findByConversationIdOrderByIdAsc(Long conversationId);
}
