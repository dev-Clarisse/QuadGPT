package com.QuadGPT.backend.auth.controller;

import com.QuadGPT.backend.auth.dto.RegisterRequest;
import com.QuadGPT.backend.auth.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(@RequestBody RegisterRequest request) {

        authService.register(request);

        return ResponseEntity.ok().build();
    }
}
