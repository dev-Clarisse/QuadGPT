package com.QuadGPT.backend;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.QuadGPT.backend.User.entity.Role;
import com.QuadGPT.backend.User.entity.User;
import com.QuadGPT.backend.department.Department;
import com.QuadGPT.backend.support.IntegrationTest;

/** The demo users script is not loaded in tests by Flyway, so it is run here by hand. */
class SeedUsersIT extends IntegrationTest {

    @Autowired
    private DataSource dataSource;

    @Test
    void seedCreatesOneUserPerDepartmentWithoutAdmins() {
        runSeed();

        assertThat(jdbcTemplate.queryForList("SELECT DISTINCT department FROM users", String.class))
                .containsExactlyInAnyOrder("RH", "FINANCE", "IT", "MARKETING", "DIRECTION", "SALES", "SUPPORT");
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM users WHERE role = 'ADMIN'", Integer.class))
                .isZero();
    }

    @Test
    void seededUsersCanLogInWithTheDocumentedPassword() throws Exception {
        runSeed();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "claire.martin@quadria.fr", "password": "Quadria2026"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.department").value("RH"));
    }

    @Test
    void seedLeavesExistingAccountsUntouched() {
        User existing = createUser("claire.martin@quadria.fr", Department.DIRECTION, Role.ADMIN);

        runSeed();
        runSeed();

        User reloaded = userRepository.findByEmail("claire.martin@quadria.fr").orElseThrow();
        assertThat(reloaded.getRole()).isEqualTo(Role.ADMIN);
        assertThat(reloaded.getDepartment()).isEqualTo(Department.DIRECTION);
        assertThat(reloaded.getPassword()).isEqualTo(existing.getPassword());
    }

    private void runSeed() {
        new ResourceDatabasePopulator(new ClassPathResource("db/seed/R__seed_users.sql")).execute(dataSource);
    }
}
