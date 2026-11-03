package com.QuadGPT.backend.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Test;

import com.QuadGPT.backend.User.entity.Role;
import com.QuadGPT.backend.User.entity.User;
import com.QuadGPT.backend.department.Department;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;

class JwtServiceTest {

    private static final String SECRET = "une-cle-secrete-de-test-d-au-moins-32-octets";

    private final User user = new User("rh@quadria.fr", "hash", Department.RH, Role.USER);

    @Test
    void tokenCarriesEmailRoleAndDepartment() {
        JwtService jwtService = new JwtService(SECRET, 60_000);

        Claims claims = jwtService.parse(jwtService.generateToken(user));

        assertThat(claims.getSubject()).isEqualTo("rh@quadria.fr");
        assertThat(claims.get("role", String.class)).isEqualTo("USER");
        assertThat(claims.get("department", String.class)).isEqualTo("RH");
    }

    @Test
    void tokenSignedWithAnotherKeyIsRejected() {
        String token = new JwtService("une-autre-cle-secrete-d-au-moins-32-octets", 60_000).generateToken(user);

        assertThatThrownBy(() -> new JwtService(SECRET, 60_000).parse(token))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void expiredTokenIsRejected() {
        JwtService jwtService = new JwtService(SECRET, -1_000);

        String token = jwtService.generateToken(user);

        assertThatThrownBy(() -> jwtService.parse(token)).isInstanceOf(ExpiredJwtException.class);
    }
}
