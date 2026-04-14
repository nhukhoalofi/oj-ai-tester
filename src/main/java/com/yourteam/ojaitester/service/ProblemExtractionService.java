package com.yourteam.ojaitester.service;

import java.io.File;

public interface ProblemExtractionService {
    String extractTextFromImage(File imageFile);
    String extractTextFromPdf(File pdfFile);
}