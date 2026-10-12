package com.QuadGPT.backend.User.dto;

import com.QuadGPT.backend.User.entity.Role;
import com.QuadGPT.backend.User.entity.User;
import com.QuadGPT.backend.department.Department;

public record UserResponse(Long id, String email, Department department, Role role) {
    public static UserResponse from(User u) {
        return new UserResponse(u.getId(), u.getEmail(), u.getDepartment(), u.getRole());
    }
}
