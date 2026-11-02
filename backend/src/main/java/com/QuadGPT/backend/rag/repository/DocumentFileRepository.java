package com.QuadGPT.backend.rag.repository;

import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.QuadGPT.backend.rag.DocumentFile;

public interface DocumentFileRepository extends JpaRepository<DocumentFile, Long> {

    @Query("SELECT f.documentId FROM DocumentFile f")
    Set<Long> findAllDocumentIds();
}
