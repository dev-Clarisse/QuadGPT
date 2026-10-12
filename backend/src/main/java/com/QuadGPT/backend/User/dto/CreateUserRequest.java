package com.QuadGPT.backend.User.dto;

import com.QuadGPT.backend.User.entity.Role;
import com.QuadGPT.backend.department.Department;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
        @Email @NotBlank String email,
        @NotBlank @Size(min = 8) String password,
        @NotNull Department department,
        @NotNull Role role
) {}
