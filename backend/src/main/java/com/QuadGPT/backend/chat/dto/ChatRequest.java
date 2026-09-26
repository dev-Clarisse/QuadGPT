package com.QuadGPT.backend.chat.dto;

import jakarta.validation.constraints.NotBlank;

public class ChatRequest {

    @NotBlank
    private String message;

    @NotBlank
    private String department;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }
}