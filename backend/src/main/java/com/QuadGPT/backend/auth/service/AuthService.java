package com.QuadGPT.backend.auth.service;

import com.QuadGPT.backend.auth.dto.RegisterRequest;
import com.QuadGPT.backend.auth.dto.LoginRequest;
import com.QuadGPT.backend.auth.dto.LoginResponse;
import com.QuadGPT.backend.auth.entity.User;
import com.QuadGPT.backend.auth.repository.UserRepository;
import com.QuadGPT.backend.auth.exception.EmailAlreadyRegisteredException;
import com.QuadGPT.backend.auth.exception.InvalidCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public void register(RegisterRequest request) {

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new EmailAlreadyRegisteredException();
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        userRepository.save(user);
    }

    public LoginResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException();
        }

        String token = jwtService.generateToken(user.getEmail());

        return new LoginResponse(token);
    }
}