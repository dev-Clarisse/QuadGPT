package com.QuadGPT.backend;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.web.client.ResourceAccessException;

import com.QuadGPT.backend.User.entity.Role;
import com.QuadGPT.backend.User.entity.User;
import com.QuadGPT.backend.department.Department;
import com.QuadGPT.backend.support.IntegrationTest;

/** Each error case returns the right HTTP status with a plain-text message the frontend can display. */
class ErrorHandlingIT extends IntegrationTest {

    private User admin;
    private User user;

    @BeforeEach
    void setUp() {
        admin = createUser("admin@quadria.fr", Department.IT, Role.ADMIN);
        user = createUser("rh@quadria.fr", Department.RH, Role.USER);
    }

    @Test
    void unknownDepartmentIsABadRequest() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "new@quadria.fr", "password": "motdepasse", "department": "LEGAL"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid request body"));
    }

    @Test
    void validationErrorsNameTheField() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "pas-un-email", "password": "motdepasse", "department": "RH"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.startsWith("email:")));
    }

    @Test
    void creatingAUserWithAnExistingEmailIsAConflict() throws Exception {
        mockMvc.perform(post("/api/users")
                        .header("Authorization", bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "rh@quadria.fr", "password": "motdepasse", "department": "RH", "role": "USER"}
                                """))
                .andExpect(status().isConflict());
    }

    @Test
    void unknownUserIsNotFound() throws Exception {
        mockMvc.perform(get("/api/users/999").header("Authorization", bearer(admin)))
                .andExpect(status().isNotFound())
                .andExpect(content().string("User not found"));
        mockMvc.perform(delete("/api/users/999").header("Authorization", bearer(admin)))
                .andExpect(status().isNotFound());
    }

    @Test
    void nonNumericIdIsABadRequest() throws Exception {
        mockMvc.perform(get("/api/users/abc").header("Authorization", bearer(admin)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void unknownDocumentIsNotFound() throws Exception {
        mockMvc.perform(get("/api/documents/999/file").header("Authorization", bearer(user)))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Document not found"));
    }

    @Test
    void fileThatIsNotAPdfIsABadRequest() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "notes.pdf", "application/pdf", "pas un pdf".getBytes());

        mockMvc.perform(multipart("/api/rag/ingest-file")
                        .file(file)
                        .param("departments", "RH")
                        .header("Authorization", bearer(admin)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("The file is not a valid PDF: notes.pdf"));
    }

    @Test
    void uploadWithoutDepartmentIsABadRequest() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "notes.pdf", "application/pdf", "contenu".getBytes());

        mockMvc.perform(multipart("/api/rag/ingest-file")
                        .file(file)
                        .header("Authorization", bearer(admin)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void uploadWithoutFileIsABadRequest() throws Exception {
        mockMvc.perform(multipart("/api/rag/ingest-file")
                        .param("departments", "RH")
                        .header("Authorization", bearer(admin)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void aiServiceDownIsServiceUnavailable() throws Exception {
        when(embeddingService.embed(anyString())).thenThrow(new ResourceAccessException("Connection refused"));

        mockMvc.perform(post("/api/chat")
                        .header("Authorization", bearer(user))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"message": "Bonjour"}
                                """))
                .andExpect(status().isServiceUnavailable());
    }

    @Test
    void validTokenOfADeletedAccountIsUnauthorized() throws Exception {
        String token = bearer(user);
        userRepository.delete(user);

        mockMvc.perform(post("/api/chat")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"message": "Bonjour"}
                                """))
                .andExpect(status().isUnauthorized());
    }
}
