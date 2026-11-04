package com.QuadGPT.backend.chat.service;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import com.QuadGPT.backend.User.entity.Role;
import com.QuadGPT.backend.User.entity.User;
import com.QuadGPT.backend.User.repository.UserRepository;
import com.QuadGPT.backend.chat.dto.ChatResponse;
import com.QuadGPT.backend.chat.entity.Conversation;
import com.QuadGPT.backend.chat.repository.ChatMessageRepository;
import com.QuadGPT.backend.chat.repository.ConversationRepository;
import com.QuadGPT.backend.department.Department;
import com.QuadGPT.backend.exception.ResourceNotFoundException;
import com.QuadGPT.backend.rag.service.RagService;

class ChatServiceTest {

    private RagService ragService;
    private UserRepository userRepository;
    private ConversationRepository conversationRepository;
    private ChatMessageRepository chatMessageRepository;
    private ChatService chatService;

    @BeforeEach
    void setUp() {
        ragService = mock(RagService.class);
        userRepository = mock(UserRepository.class);
        conversationRepository = mock(ConversationRepository.class);
        chatMessageRepository = mock(ChatMessageRepository.class);
        chatService = new ChatService(ragService, userRepository, conversationRepository, chatMessageRepository);

        when(ragService.ask(anyString(), any())).thenReturn("réponse");
        // Simulates the database assigning an id on insert.
        when(conversationRepository.save(any())).thenAnswer(invocation -> {
            Conversation conversation = invocation.getArgument(0);
            ReflectionTestUtils.setField(conversation, "id", 10L);
            return conversation;
        });
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void userOnlySearchesTheirDepartment() {
        loggedInAs("rh@quadria.fr", Department.RH, Role.USER);

        chatService.processMessage("Qui dirige les RH ?", null);

        verify(ragService).ask("Qui dirige les RH ?", Department.RH);
    }

    @Test
    void adminSearchesEveryDepartment() {
        loggedInAs("admin@quadria.fr", Department.IT, Role.ADMIN);

        chatService.processMessage("Qui dirige les RH ?", null);

        verify(ragService).ask(eq("Qui dirige les RH ?"), isNull());
    }

    @Test
    void firstMessageStartsAConversationTitledAfterIt() {
        loggedInAs("rh@quadria.fr", Department.RH, Role.USER);

        ChatResponse response = chatService.processMessage("  Combien   de jours de congés ?  ", null);

        assertThat(response.getResponse()).isEqualTo("réponse");
        assertThat(response.getConversationId()).isEqualTo(10L);
        assertThat(response.getTitle()).isEqualTo("Combien de jours de congés ?");
    }

    @Test
    void longTitlesAreTruncated() {
        loggedInAs("rh@quadria.fr", Department.RH, Role.USER);

        ChatResponse response = chatService.processMessage("a".repeat(100), null);

        assertThat(response.getTitle()).hasSize(60).endsWith("…");
    }

    @Test
    void someoneElsesConversationIsNotFoundAndTheLlmIsNotCalled() {
        User user = loggedInAs("rh@quadria.fr", Department.RH, Role.USER);
        when(conversationRepository.findByIdAndUserId(99L, user.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> chatService.processMessage("question", 99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Conversation not found");
        verify(ragService, never()).ask(anyString(), any());
    }

    private User loggedInAs(String email, Department department, Role role) {
        User user = new User(email, "hash", department, role);
        ReflectionTestUtils.setField(user, "id", 1L);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken(email, null));
        return user;
    }
}
