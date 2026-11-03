package com.QuadGPT.backend.chat;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.ArgumentCaptor;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.QuadGPT.backend.User.entity.Role;
import com.QuadGPT.backend.User.entity.User;
import com.QuadGPT.backend.department.Department;
import com.QuadGPT.backend.rag.service.IngestionService;
import com.QuadGPT.backend.support.IntegrationTest;

import com.jayway.jsonpath.JsonPath;

class ChatApiIT extends IntegrationTest {

    private static final String RH_TEXT = "Le département des ressources humaines est dirigé par Claire Martin.";
    private static final String IT_TEXT = "Le VPN est obligatoire en télétravail.";

    @Autowired
    private IngestionService ingestionService;

    @BeforeEach
    void ingestDocuments() {
        when(embeddingService.embed(anyString())).thenReturn(vector(1));
        when(llmClientService.chat(anyString())).thenReturn("Réponse du modèle");

        ingestionService.ingest("RH.pdf", RH_TEXT, Set.of(Department.RH));
        ingestionService.ingest("IT.pdf", IT_TEXT, Set.of(Department.IT));
    }

    @Test
    void userOnlyGetsTheirDepartmentDocumentsAsContext() throws Exception {
        User itUser = createUser("it@quadria.fr", Department.IT, Role.USER);

        ask(itUser, "Qui dirige les RH ?").andExpect(status().isOk());

        String prompt = capturedPrompt();
        assertThat(prompt).contains(IT_TEXT).doesNotContain("Claire Martin");
    }

    @Test
    void adminGetsEveryDocumentAsContext() throws Exception {
        User admin = createUser("admin@quadria.fr", Department.IT, Role.ADMIN);

        ask(admin, "Qui dirige les RH ?").andExpect(status().isOk());

        assertThat(capturedPrompt()).contains(RH_TEXT).contains(IT_TEXT);
    }

    @Test
    void conversationIsSavedAndListedForItsOwner() throws Exception {
        User rhUser = createUser("rh@quadria.fr", Department.RH, Role.USER);

        ask(rhUser, "Qui dirige les RH ?")
                .andExpect(jsonPath("$.response").value("Réponse du modèle"))
                .andExpect(jsonPath("$.title").value("Qui dirige les RH ?"));

        mockMvc.perform(get("/api/conversations").header("Authorization", bearer(rhUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void anotherUserCannotReadOrDeleteTheConversation() throws Exception {
        User owner = createUser("rh@quadria.fr", Department.RH, Role.USER);
        User other = createUser("it@quadria.fr", Department.IT, Role.USER);
        long conversationId = conversationIdOf(ask(owner, "Qui dirige les RH ?"));

        mockMvc.perform(get("/api/conversations/{id}/messages", conversationId)
                        .header("Authorization", bearer(other)))
                .andExpect(status().isNotFound());
        mockMvc.perform(delete("/api/conversations/{id}", conversationId)
                        .header("Authorization", bearer(other)))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/conversations/{id}/messages", conversationId)
                        .header("Authorization", bearer(owner)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].role").value("user"))
                .andExpect(jsonPath("$[1].role").value("assistant"));
    }

    @Test
    void emptyMessageIsRejected() throws Exception {
        User user = createUser("rh@quadria.fr", Department.RH, Role.USER);

        ask(user, " ").andExpect(status().isBadRequest());
    }

    private ResultActions ask(User user, String message) throws Exception {
        return mockMvc.perform(post("/api/chat")
                .header("Authorization", bearer(user))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"message": "%s"}
                        """.formatted(message)));
    }

    private long conversationIdOf(ResultActions result) throws Exception {
        String body = result.andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(body, "$.conversationId")).longValue();
    }

    private String capturedPrompt() {
        ArgumentCaptor<String> prompt = ArgumentCaptor.forClass(String.class);
        verify(llmClientService).chat(prompt.capture());
        return prompt.getValue();
    }
}
