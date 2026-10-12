package com.QuadGPT.backend.User.service;



import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;   // ⚠️ pas jakarta

import com.QuadGPT.backend.User.dto.CreateUserRequest;
import com.QuadGPT.backend.User.dto.UserResponse;
import com.QuadGPT.backend.User.entity.User;
import com.QuadGPT.backend.User.repository.UserRepository;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class UserService {                                          // ⚠️ UserService, pas userService

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserResponse createUser(CreateUserRequest r) {
        if (userRepository.existsByEmail(r.email())) {
            throw new IllegalArgumentException("Email already in use: " + r.email());
        }
        User user = new User(
                r.email(),
                passwordEncoder.encode(r.password()),
                r.department(),
                r.role()
        );
        return UserResponse.from(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public List<UserResponse> findAll() {
        return userRepository.findAll().stream().map(UserResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public UserResponse findById(Long id) {
        return userRepository.findById(id)
                .map(UserResponse::from)
                .orElseThrow(() -> new NoSuchElementException("User not found: " + id));
    }

    @Transactional
    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new NoSuchElementException("User not found: " + id);
        }
        userRepository.deleteById(id);
    }
}