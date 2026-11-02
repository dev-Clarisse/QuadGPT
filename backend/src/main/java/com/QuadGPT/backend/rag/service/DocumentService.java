package com.QuadGPT.backend.rag.service;

import java.util.List;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.QuadGPT.backend.User.entity.Role;
import com.QuadGPT.backend.User.entity.User;
import com.QuadGPT.backend.User.repository.UserRepository;
import com.QuadGPT.backend.rag.Document;
import com.QuadGPT.backend.rag.DocumentFile;
import com.QuadGPT.backend.rag.dto.DocumentResponse;
import com.QuadGPT.backend.rag.repository.DocumentFileRepository;
import com.QuadGPT.backend.rag.repository.DocumentRepository;

@Service
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final DocumentFileRepository documentFileRepository;
    private final UserRepository userRepository;

    public DocumentService(
            DocumentRepository documentRepository,
            DocumentFileRepository documentFileRepository,
            UserRepository userRepository
    ) {
        this.documentRepository = documentRepository;
        this.documentFileRepository = documentFileRepository;
        this.userRepository = userRepository;
    }

    public record DownloadableFile(String name, String contentType, byte[] data) {}

    // ADMIN sees every document, USER only the documents of their department.
    @Transactional(readOnly = true)
    public List<DocumentResponse> listVisibleDocuments(String email) {
        User user = findUser(email);

        List<Document> documents = user.getRole() == Role.ADMIN
                ? documentRepository.findAllByOrderByCreatedAtDesc()
                : documentRepository.findByDepartment(user.getDepartment());

        Set<Long> idsWithFile = documentFileRepository.findAllDocumentIds();

        return documents.stream()
                .map(document -> new DocumentResponse(
                        document.getId(),
                        document.getName(),
                        document.getDepartments() == null ? Set.of() : Set.copyOf(document.getDepartments()),
                        document.getCreatedAt(),
                        idsWithFile.contains(document.getId())
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public DownloadableFile getFile(Long documentId, String email) {
        User user = findUser(email);

        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Document not found"));

        boolean allowed = user.getRole() == Role.ADMIN
                || document.getDepartments().contains(user.getDepartment());
        if (!allowed) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot access this document");
        }

        DocumentFile file = documentFileRepository.findById(documentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "The original file is not available for this document"));

        return new DownloadableFile(document.getName(), file.getContentType(), file.getData());
    }

    // Removes the document everywhere: its departments, chunks and stored file
    // go with it through the ON DELETE CASCADE foreign keys.
    @Transactional
    public void deleteDocument(Long documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Document not found"));
        documentRepository.delete(document);
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unknown user"));
    }
}
