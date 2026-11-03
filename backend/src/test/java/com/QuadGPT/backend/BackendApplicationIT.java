package com.QuadGPT.backend;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.QuadGPT.backend.support.IntegrationTest;

class BackendApplicationIT extends IntegrationTest {

    @Test
    void contextLoadsAndFlywayCreatesEveryTable() {
        Integer tables = jdbcTemplate.queryForObject(
                """
                SELECT count(*) FROM information_schema.tables
                WHERE table_schema = 'public'
                  AND table_name IN ('users', 'document', 'document_department', 'document_chunk',
                                     'document_file', 'conversation', 'chat_message')
                """,
                Integer.class);

        assertThat(tables).isEqualTo(7);
    }

    @Test
    void openApiDescriptionIsPublic() throws Exception {
        mockMvc.perform(get("/api/docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("QuadGPT API"))
                .andExpect(jsonPath("$.paths['/api/chat']").exists());
        mockMvc.perform(get("/api/swagger-ui/index.html")).andExpect(status().isOk());
    }
}
