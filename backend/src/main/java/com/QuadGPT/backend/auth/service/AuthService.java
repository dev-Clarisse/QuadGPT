package com.QuadGPT.backend.auth.service;

import com.QuadGPT.backend.auth.dto.RegisterRequest;
import com.QuadGPT.backend.User.entity.Role;
import com.QuadGPT.backend.User.entity.User;
import com.QuadGPT.backend.User.repository.UserRepository;
import com.QuadGPT.backend.auth.dto.LoginRequest;
import com.QuadGPT.backend.auth.dto.LoginResponse;
import com.QuadGPT.backend.auth.exception.EmailAlreadyRegisteredException;
import com.QuadGPT.backend.auth.exception.InvalidCredentialsException;

import org.springframework.stereotype.Service;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public void register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email already in use: " + request.email());
        }
        User user = new User(
                request.email(),
                passwordEncoder.encode(request.password()),
                request.department(),
                Role.USER // 🔒 toujours USER via /register
        );
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BadCredentialsException("Invalid credentials");
        }

        String token = jwtService.generateToken(user);
        return new LoginResponse(
                token,
                user.getEmail(),
                user.getRole().name(),
                user.getDepartment().name()
        );
    }
}