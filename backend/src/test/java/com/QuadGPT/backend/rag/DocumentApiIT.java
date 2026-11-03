package com.QuadGPT.backend.rag;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.ResultActions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.QuadGPT.backend.User.entity.Role;
import com.QuadGPT.backend.User.entity.User;
import com.QuadGPT.backend.department.Department;
import com.QuadGPT.backend.support.IntegrationTest;

class DocumentApiIT extends IntegrationTest {

    private User admin;
    private User rhUser;
    private User itUser;

    @BeforeEach
    void setUp() {
        when(embeddingService.embed(anyString())).thenReturn(vector(1));
        admin = createUser("admin@quadria.fr", Department.IT, Role.ADMIN);
        rhUser = createUser("rh@quadria.fr", Department.RH, Role.USER);
        itUser = createUser("it@quadria.fr", Department.IT, Role.USER);
    }

    @Test
    void adminUploadsAPdfThatIsExtractedChunkedAndStored() throws Exception {
        upload(admin, "RH.pdf", "Claire Martin dirige les ressources humaines.", "RH")
                .andExpect(status().isOk())
                .andExpect(content().string("Fichier ingéré : RH.pdf"));

        String chunk = jdbcTemplate.queryForObject("SELECT content FROM document_chunk", String.class);
        assertThat(chunk).contains("Claire Martin dirige les ressources humaines.");
    }

    @Test
    void usersCannotUploadDocuments() throws Exception {
        upload(rhUser, "RH.pdf", "Texte.", "RH").andExpect(status().isForbidden());
    }

    @Test
    void uploadingTheSameFileTwiceIsAConflict() throws Exception {
        byte[] sameFile = pdf("Texte identique.");
        upload(admin, "RH.pdf", sameFile, "RH").andExpect(status().isOk());

        upload(admin, "RH-copie.pdf", sameFile, "RH").andExpect(status().isConflict());
    }

    @Test
    void usersOnlySeeAndDownloadTheirDepartmentDocuments() throws Exception {
        upload(admin, "RH.pdf", "Document RH.", "RH").andExpect(status().isOk());
        Long documentId = jdbcTemplate.queryForObject("SELECT id FROM document", Long.class);

        mockMvc.perform(get("/api/documents").header("Authorization", bearer(rhUser)))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("RH.pdf"));
        mockMvc.perform(get("/api/documents").header("Authorization", bearer(itUser)))
                .andExpect(jsonPath("$.length()").value(0));

        mockMvc.perform(get("/api/documents/{id}/file", documentId).header("Authorization", bearer(rhUser)))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/documents/{id}/file", documentId).header("Authorization", bearer(itUser)))
                .andExpect(status().isForbidden());
    }

    @Test
    void onlyAdminsCanDeleteAndDeletingRemovesTheChunks() throws Exception {
        upload(admin, "RH.pdf", "Document RH.", "RH").andExpect(status().isOk());
        Long documentId = jdbcTemplate.queryForObject("SELECT id FROM document", Long.class);

        mockMvc.perform(delete("/api/documents/{id}", documentId).header("Authorization", bearer(rhUser)))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/documents/{id}", documentId).header("Authorization", bearer(admin)))
                .andExpect(status().isNoContent());

        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM document_chunk", Integer.class)).isZero();
    }

    private ResultActions upload(
            User user, String fileName, String text, String department) throws Exception {
        return upload(user, fileName, pdf(text), department);
    }

    private ResultActions upload(
            User user, String fileName, byte[] pdf, String department) throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", fileName, "application/pdf", pdf);
        return mockMvc.perform(multipart("/api/rag/ingest-file")
                .file(file)
                .param("departments", department)
                .header("Authorization", bearer(user)));
    }

    private static byte[] pdf(String text) throws IOException {
        try (PDDocument document = new PDDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PDPage page = new PDPage();
            document.addPage(page);
            try (PDPageContentStream stream = new PDPageContentStream(document, page)) {
                stream.beginText();
                stream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                stream.newLineAtOffset(50, 700);
                stream.showText(text);
                stream.endText();
            }
            document.save(out);
            return out.toByteArray();
        }
    }
}
