package com.QuadGPT.backend.chat.service;

import com.QuadGPT.backend.auth.entity.User;
import com.QuadGPT.backend.auth.repository.UserRepository;
import com.QuadGPT.backend.rag.RagService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class ChatService {

    private final RagService ragService;
    private final UserRepository userRepository;

    public ChatService(
            RagService ragService,
            UserRepository userRepository
    ) {
        this.ragService = ragService;
        this.userRepository = userRepository;
    }

    public String processMessage(String message) {

        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));

        return ragService.ask(
                message,
                user.getDepartment()
        );
    }
}
