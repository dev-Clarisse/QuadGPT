package com.QuadGPT.backend.auth;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.QuadGPT.backend.User.entity.Role;
import com.QuadGPT.backend.User.entity.User;
import com.QuadGPT.backend.department.Department;
import com.QuadGPT.backend.support.IntegrationTest;

class AuthApiIT extends IntegrationTest {

    @Test
    void registerThenLoginReturnsAUserToken() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "rh@quadria.fr", "password": "motdepasse", "department": "RH"}
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "rh@quadria.fr", "password": "motdepasse"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.department").value("RH"));

        assertThat(userRepository.findByEmail("rh@quadria.fr")).get()
                .extracting(User::getRole)
                .isEqualTo(Role.USER);
    }

    @Test
    void registeringTheSameEmailTwiceIsAConflict() throws Exception {
        createUser("rh@quadria.fr", Department.RH, Role.USER);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "rh@quadria.fr", "password": "motdepasse", "department": "RH"}
                                """))
                .andExpect(status().isConflict());
    }

    @Test
    void registerRejectsAShortPassword() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "rh@quadria.fr", "password": "court", "department": "RH"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void loginWithAWrongPasswordIsUnauthorized() throws Exception {
        createUser("rh@quadria.fr", Department.RH, Role.USER);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "rh@quadria.fr", "password": "mauvais-mot-de-passe"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpointsRequireAToken() throws Exception {
        mockMvc.perform(get("/api/documents")).andExpect(status().isUnauthorized());
    }

    @Test
    void aForgedTokenIsRejected() throws Exception {
        mockMvc.perform(get("/api/documents").header("Authorization", "Bearer pas.un.jeton"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void onlyAdminsCanManageUsers() throws Exception {
        User user = createUser("rh@quadria.fr", Department.RH, Role.USER);
        User admin = createUser("admin@quadria.fr", Department.IT, Role.ADMIN);

        mockMvc.perform(get("/api/users").header("Authorization", bearer(user)))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/users").header("Authorization", bearer(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }
}
