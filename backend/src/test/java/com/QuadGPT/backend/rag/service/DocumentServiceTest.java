package com.QuadGPT.backend.rag.service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.test.util.ReflectionTestUtils;

import com.QuadGPT.backend.User.entity.Role;
import com.QuadGPT.backend.User.entity.User;
import com.QuadGPT.backend.User.repository.UserRepository;
import com.QuadGPT.backend.department.Department;
import com.QuadGPT.backend.exception.DocumentAccessDeniedException;
import com.QuadGPT.backend.exception.ResourceNotFoundException;
import com.QuadGPT.backend.rag.Document;
import com.QuadGPT.backend.rag.DocumentFile;
import com.QuadGPT.backend.rag.repository.DocumentFileRepository;
import com.QuadGPT.backend.rag.repository.DocumentRepository;

class DocumentServiceTest {

    private DocumentRepository documentRepository;
    private DocumentFileRepository documentFileRepository;
    private UserRepository userRepository;
    private DocumentService documentService;

    private final Document rhDocument = new Document("RH.pdf", Set.of(Department.RH));

    @BeforeEach
    void setUp() {
        documentRepository = mock(DocumentRepository.class);
        documentFileRepository = mock(DocumentFileRepository.class);
        userRepository = mock(UserRepository.class);
        documentService = new DocumentService(documentRepository, documentFileRepository, userRepository);
        ReflectionTestUtils.setField(rhDocument, "id", 1L);

        when(documentRepository.findById(1L)).thenReturn(Optional.of(rhDocument));
        when(documentFileRepository.findById(1L))
                .thenReturn(Optional.of(new DocumentFile(1L, "application/pdf", new byte[] {1, 2, 3})));
        when(documentFileRepository.findAllDocumentIds()).thenReturn(Set.of());
    }

    @Test
    void userListsOnlyTheirDepartmentDocuments() {
        givenUser("it@quadria.fr", Department.IT, Role.USER);
        when(documentRepository.findByDepartment(Department.IT)).thenReturn(List.of());

        documentService.listVisibleDocuments("it@quadria.fr");

        verify(documentRepository).findByDepartment(Department.IT);
        verify(documentRepository, never()).findAllByOrderByCreatedAtDesc();
    }

    @Test
    void adminListsEveryDocument() {
        givenUser("admin@quadria.fr", Department.IT, Role.ADMIN);
        when(documentRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(rhDocument));

        assertThat(documentService.listVisibleDocuments("admin@quadria.fr"))
                .extracting("name")
                .containsExactly("RH.pdf");
    }

    @Test
    void userOfTheDocumentDepartmentCanDownloadIt() {
        givenUser("rh@quadria.fr", Department.RH, Role.USER);

        DocumentService.DownloadableFile file = documentService.getFile(1L, "rh@quadria.fr");

        assertThat(file.name()).isEqualTo("RH.pdf");
        assertThat(file.data()).containsExactly(1, 2, 3);
    }

    @Test
    void userOfAnotherDepartmentCannotDownloadIt() {
        givenUser("it@quadria.fr", Department.IT, Role.USER);

        assertThatThrownBy(() -> documentService.getFile(1L, "it@quadria.fr"))
                .isInstanceOf(DocumentAccessDeniedException.class);
    }

    @Test
    void adminCanDownloadAnyDocument() {
        givenUser("admin@quadria.fr", Department.IT, Role.ADMIN);

        assertThat(documentService.getFile(1L, "admin@quadria.fr").name()).isEqualTo("RH.pdf");
    }

    @Test
    void deletingAnUnknownDocumentIsNotFound() {
        when(documentRepository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> documentService.deleteDocument(42L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Document not found");
    }

    private void givenUser(String email, Department department, Role role) {
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(new User(email, "hash", department, role)));
    }
}
