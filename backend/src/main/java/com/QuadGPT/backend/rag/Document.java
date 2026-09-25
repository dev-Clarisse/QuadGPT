package com.QuadGPT.backend.rag;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "document")
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column
    private String department;

    @Column(name = "created_at", nullable = false)
    private final LocalDateTime createdAt = LocalDateTime.now();

    public Document() {}

    public Document(String name, String department) {
        this.name = name;
        this.department = department;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDepartment() { return department; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}