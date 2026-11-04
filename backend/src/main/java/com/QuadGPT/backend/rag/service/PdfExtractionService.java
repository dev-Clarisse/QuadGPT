package com.QuadGPT.backend.rag.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.QuadGPT.backend.exception.InvalidFileException;

import java.io.IOException;

@Service
public class PdfExtractionService {

    public String extractText(MultipartFile file) {
        try (PDDocument document = Loader.loadPDF(file.getBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        } catch (IOException e) {
            // The upload itself worked: the file is simply not a readable PDF.
            throw new InvalidFileException("The file is not a valid PDF: " + file.getOriginalFilename(), e);
        }
    }
}