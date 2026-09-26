package com.QuadGPT.backend.rag;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import com.QuadGPT.backend.department.Department;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "document")
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ElementCollection
    @CollectionTable(
        name = "document_department",
        joinColumns = @JoinColumn(name = "document_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "department", nullable = false)
    private Set<Department> departments = new HashSet<>();

    @Column(name = "created_at", nullable = false)
    private final LocalDateTime createdAt = LocalDateTime.now();

    public Document() {}

    public Document(String name, Set<Department> departments) {
        this.name = name;
        this.departments = departments;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Set<Department> getDepartments() {
        return departments;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}