package com.rahul.resumematcher.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class PdfParserService {

    /**
     * Extracts plain text from an uploaded PDF resume.
     * Throws IOException if the file cannot be read or parsed.
     */
    public String extractText(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file is empty");
        }
        if (!"application/pdf".equalsIgnoreCase(file.getContentType())) {
            throw new IllegalArgumentException("Only PDF files are supported");
        }

        try (PDDocument document = PDDocument.load(file.getInputStream())) {
            if (document.isEncrypted()) {
                throw new IllegalArgumentException("Encrypted PDFs are not supported");
            }
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }
}
