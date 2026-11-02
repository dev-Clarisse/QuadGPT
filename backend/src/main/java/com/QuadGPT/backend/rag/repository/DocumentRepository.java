package com.QuadGPT.backend.rag.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.QuadGPT.backend.department.Department;
import com.QuadGPT.backend.rag.Document;

public interface DocumentRepository extends JpaRepository<Document, Long> {

    List<Document> findAllByOrderByCreatedAtDesc();

    Optional<Document> findByFileHash(String fileHash);

    @Query("""
            SELECT d FROM Document d
            JOIN d.departments dep
            WHERE dep = :department
            ORDER BY d.createdAt DESC
            """)
    List<Document> findByDepartment(Department department);
}
