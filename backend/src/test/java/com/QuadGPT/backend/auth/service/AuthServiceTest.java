package com.QuadGPT.backend.auth.service;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.ArgumentCaptor;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.QuadGPT.backend.User.entity.Role;
import com.QuadGPT.backend.User.entity.User;
import com.QuadGPT.backend.User.repository.UserRepository;
import com.QuadGPT.backend.auth.dto.LoginRequest;
import com.QuadGPT.backend.auth.dto.LoginResponse;
import com.QuadGPT.backend.auth.dto.RegisterRequest;
import com.QuadGPT.backend.auth.exception.EmailAlreadyRegisteredException;
import com.QuadGPT.backend.auth.exception.InvalidCredentialsException;
import com.QuadGPT.backend.department.Department;

class AuthServiceTest {

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private UserRepository userRepository;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        JwtService jwtService = new JwtService("une-cle-secrete-de-test-d-au-moins-32-octets", 60_000);
        authService = new AuthService(userRepository, passwordEncoder, jwtService);
    }

    @Test
    void registeredUsersAreAlwaysPlainUsersWithAHashedPassword() {
        authService.register(new RegisterRequest("rh@quadria.fr", "motdepasse", Department.RH));

        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(saved.capture());
        assertThat(saved.getValue().getRole()).isEqualTo(Role.USER);
        assertThat(saved.getValue().getPassword()).isNotEqualTo("motdepasse");
        assertThat(passwordEncoder.matches("motdepasse", saved.getValue().getPassword())).isTrue();
    }

    @Test
    void registeringAnExistingEmailFails() {
        when(userRepository.existsByEmail("rh@quadria.fr")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(
                new RegisterRequest("rh@quadria.fr", "motdepasse", Department.RH)))
                .isInstanceOf(EmailAlreadyRegisteredException.class);
        verify(userRepository, never()).save(any());
    }

    @Test
    void loginWithTheRightPasswordReturnsAToken() {
        givenUser("rh@quadria.fr", "motdepasse");

        LoginResponse response = authService.login(new LoginRequest("rh@quadria.fr", "motdepasse"));

        assertThat(response.token()).isNotBlank();
        assertThat(response.role()).isEqualTo("USER");
        assertThat(response.department()).isEqualTo("RH");
    }

    @Test
    void loginWithAWrongPasswordFails() {
        givenUser("rh@quadria.fr", "motdepasse");

        assertThatThrownBy(() -> authService.login(new LoginRequest("rh@quadria.fr", "mauvais")))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void loginWithAnUnknownEmailFails() {
        when(userRepository.findByEmail("inconnu@quadria.fr")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(new LoginRequest("inconnu@quadria.fr", "motdepasse")))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    private void givenUser(String email, String password) {
        User user = new User(email, passwordEncoder.encode(password), Department.RH, Role.USER);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
    }
}
