package com.QuadGPT.backend.rag.dto;

import java.time.LocalDateTime;
import java.util.Set;

import com.QuadGPT.backend.department.Department;

public record DocumentResponse(
        Long id,
        String name,
        Set<Department> departments,
        LocalDateTime createdAt,
        boolean downloadable
) {}
