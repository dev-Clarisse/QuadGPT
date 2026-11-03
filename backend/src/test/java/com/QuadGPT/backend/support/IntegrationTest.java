package com.QuadGPT.backend.support;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.QuadGPT.backend.User.entity.Role;
import com.QuadGPT.backend.User.entity.User;
import com.QuadGPT.backend.User.repository.UserRepository;
import com.QuadGPT.backend.auth.service.JwtService;
import com.QuadGPT.backend.department.Department;
import com.QuadGPT.backend.llm.LlmClientService;
import com.QuadGPT.backend.rag.service.EmbeddingService;

/**
 * Base class for integration tests: the whole Spring application runs against a real
 * PostgreSQL + pgvector database. Only Ollama (embeddings and chat) is mocked, since a
 * model cannot run in CI.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfig.class)
public abstract class IntegrationTest {

    protected static final String PASSWORD = "motdepasse";

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    @Autowired
    protected JwtService jwtService;

    @MockitoBean
    protected EmbeddingService embeddingService;

    @MockitoBean
    protected LlmClientService llmClientService;

    // Each test starts from an empty database; CASCADE also empties chunks, files and conversations.
    @BeforeEach
    void cleanDatabase() {
        jdbcTemplate.execute("TRUNCATE document, users RESTART IDENTITY CASCADE");
    }

    protected User createUser(String email, Department department, Role role) {
        return userRepository.save(new User(email, passwordEncoder.encode(PASSWORD), department, role));
    }

    protected String bearer(User user) {
        return "Bearer " + jwtService.generateToken(user);
    }

    // A 768-dimension vector (the size of the embedding column) starting with the given values.
    protected static float[] vector(float... firstValues) {
        float[] vector = new float[768];
        System.arraycopy(firstValues, 0, vector, 0, firstValues.length);
        return vector;
    }
}
