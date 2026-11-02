package com.QuadGPT.backend.chat.service;

import java.util.List;

import com.QuadGPT.backend.User.entity.User;
import com.QuadGPT.backend.User.repository.UserRepository;
import com.QuadGPT.backend.chat.dto.ChatResponse;
import com.QuadGPT.backend.chat.dto.ConversationResponse;
import com.QuadGPT.backend.chat.dto.MessageResponse;
import com.QuadGPT.backend.chat.entity.ChatMessage;
import com.QuadGPT.backend.chat.entity.Conversation;
import com.QuadGPT.backend.chat.repository.ChatMessageRepository;
import com.QuadGPT.backend.chat.repository.ConversationRepository;
import com.QuadGPT.backend.rag.service.RagService;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ChatService {

    private static final int TITLE_MAX_LENGTH = 60;

    private final RagService ragService;
    private final UserRepository userRepository;
    private final ConversationRepository conversationRepository;
    private final ChatMessageRepository chatMessageRepository;

    public ChatService(
            RagService ragService,
            UserRepository userRepository,
            ConversationRepository conversationRepository,
            ChatMessageRepository chatMessageRepository
    ) {
        this.ragService = ragService;
        this.userRepository = userRepository;
        this.conversationRepository = conversationRepository;
        this.chatMessageRepository = chatMessageRepository;
    }

    public ChatResponse processMessage(String message, Long conversationId) {
        User user = currentUser();

        // Check ownership before calling the LLM, so a wrong id fails fast.
        Conversation conversation = conversationId == null
                ? null
                : findOwnedConversation(conversationId, user);

        String answer = ragService.ask(
                message,
                user.getDepartment()
        );

        // Only persist once the answer is available, so a failed call leaves no half-saved exchange.
        if (conversation == null) {
            conversation = new Conversation(user.getId(), buildTitle(message));
        }
        conversation.touch();
        conversation = conversationRepository.save(conversation);

        chatMessageRepository.save(new ChatMessage(conversation.getId(), ChatMessage.ROLE_USER, message));
        chatMessageRepository.save(new ChatMessage(conversation.getId(), ChatMessage.ROLE_ASSISTANT, answer));

        return new ChatResponse(answer, conversation.getId(), conversation.getTitle());
    }

    public List<ConversationResponse> listConversations() {
        return conversationRepository.findByUserIdOrderByUpdatedAtDesc(currentUser().getId())
                .stream()
                .map(conversation -> new ConversationResponse(
                        conversation.getId(),
                        conversation.getTitle(),
                        conversation.getUpdatedAt()
                ))
                .toList();
    }

    public List<MessageResponse> getMessages(Long conversationId) {
        Conversation conversation = findOwnedConversation(conversationId, currentUser());

        return chatMessageRepository.findByConversationIdOrderByIdAsc(conversation.getId())
                .stream()
                .map(chatMessage -> new MessageResponse(
                        chatMessage.getRole(),
                        chatMessage.getContent(),
                        chatMessage.getCreatedAt()
                ))
                .toList();
    }

    @Transactional
    public void deleteConversation(Long conversationId) {
        Conversation conversation = findOwnedConversation(conversationId, currentUser());
        // Messages are removed by the ON DELETE CASCADE foreign key.
        conversationRepository.delete(conversation);
    }

    private Conversation findOwnedConversation(Long conversationId, User user) {
        // 404 rather than 403 so other users' conversation ids are not revealed.
        return conversationRepository.findByIdAndUserId(conversationId, user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation not found"));
    }

    private String buildTitle(String message) {
        String title = message.strip().replaceAll("\\s+", " ");
        return title.length() > TITLE_MAX_LENGTH
                ? title.substring(0, TITLE_MAX_LENGTH - 1) + "…"
                : title;
    }

    private User currentUser() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));
    }
}
