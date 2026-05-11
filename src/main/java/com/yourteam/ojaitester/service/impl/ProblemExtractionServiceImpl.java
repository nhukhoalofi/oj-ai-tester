package com.yourteam.ojaitester.service.impl;

import com.yourteam.ojaitester.service.ProblemExtractionService;
import com.yourteam.ojaitester.util.PdfUtils;

import java.io.File;

public class ProblemExtractionServiceImpl implements ProblemExtractionService {

    private GeminiService geminiService;

    @Override
    public String extractTextFromImage(File imageFile) {
        try {
            return getGeminiService().extractTextFromImage(imageFile);
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract text from image: " + rootMessage(e), e);
        }
    }

    @Override
    public String extractTextFromPdf(File pdfFile) {
        try {
            String localText = PdfUtils.extractTextFromPdf(pdfFile);
            if (localText != null && !localText.isBlank()) {
                return localText.trim();
            }

            return getGeminiService().extractTextFromPdf(pdfFile);
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract text from PDF: " + rootMessage(e), e);
        }
    }

    private GeminiService getGeminiService() {
        if (geminiService == null) {
            geminiService = new GeminiService();
        }
        return geminiService;
    }

    private String rootMessage(Exception e) {
        Throwable root = e;
        while (root.getCause() != null) {
            root = root.getCause();
        }
        return root.getMessage() != null ? root.getMessage() : root.toString();
    }
}