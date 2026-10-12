package com.QuadGPT.backend.auth.dto;

public record LoginResponse(String token, String email, String role, String department) {}