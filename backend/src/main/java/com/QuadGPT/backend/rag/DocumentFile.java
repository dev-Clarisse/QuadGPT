package com.QuadGPT.backend.rag;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "document_file")
public class DocumentFile {

    @Id
    @Column(name = "document_id")
    private Long documentId;

    @Column(name = "content_type", nullable = false)
    private String contentType;

    @Column(name = "data", nullable = false)
    private byte[] data;

    public DocumentFile() {}

    public DocumentFile(Long documentId, String contentType, byte[] data) {
        this.documentId = documentId;
        this.contentType = contentType;
        this.data = data;
    }

    public Long getDocumentId() {
        return documentId;
    }

    public String getContentType() {
        return contentType;
    }

    public byte[] getData() {
        return data;
    }
}
