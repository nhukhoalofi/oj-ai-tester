package com.yourteam.ojaitester.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yourteam.ojaitester.config.AppConfig;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

public class GeminiService {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final OkHttpClient CLIENT = new OkHttpClient();

    private final String apiKey;
    private final String model;

    public GeminiService() {
        this.apiKey = AppConfig.getProperty("ai.apiKey");
        this.model = AppConfig.getProperty("ai.model");

        if (apiKey == null || apiKey.isBlank()) {
            throw new RuntimeException("Gemini API key is missing. Please check application.properties");
        }

        if (model == null || model.isBlank()) {
            throw new RuntimeException("Gemini model is missing. Please check application.properties");
        }
    }

    public String extractTextFromImage(File imageFile) {
        try {
            String mimeType = detectMimeType(imageFile);
            byte[] fileBytes = Files.readAllBytes(imageFile.toPath());
            String base64Data = Base64.getEncoder().encodeToString(fileBytes);

            String url = "https://generativelanguage.googleapis.com/v1beta/models/"
                    + model + ":generateContent?key=" + apiKey;

            String payload = """
                    {
                      "contents": [
                        {
                          "parts": [
                            {
                              "text": "Extract all readable text from this programming problem image exactly as plain text. Preserve title, statement, input, output, constraints, samples, and notes. Return only the extracted text."
                            },
                            {
                              "inline_data": {
                                "mime_type": "%s",
                                "data": "%s"
                              }
                            }
                          ]
                        }
                      ]
                    }
                    """.formatted(mimeType, base64Data);

            return callGeminiApiWithRetry(url, payload);

        } catch (Exception e) {
            throw new RuntimeException("Failed to extract text from image", e);
        }
    }

    public String extractTextFromPdf(File pdfFile) {
        try {
            String mimeType = "application/pdf";
            byte[] fileBytes = Files.readAllBytes(pdfFile.toPath());
            String base64Data = Base64.getEncoder().encodeToString(fileBytes);

            String url = "https://generativelanguage.googleapis.com/v1beta/models/"
                    + model + ":generateContent?key=" + apiKey;

            String payload = """
                    {
                      "contents": [
                        {
                          "parts": [
                            {
                              "text": "Extract the full programming problem statement from this PDF as plain text. Preserve title, statement, input, output, constraints, samples, and notes as much as possible. Return only the extracted text."
                            },
                            {
                              "inline_data": {
                                "mime_type": "%s",
                                "data": "%s"
                              }
                            }
                          ]
                        }
                      ]
                    }
                    """.formatted(mimeType, base64Data);

            return callGeminiApiWithRetry(url, payload);

        } catch (Exception e) {
            throw new RuntimeException("Failed to extract text from PDF", e);
        }
    }

    private String callGeminiApiWithRetry(String url, String payload) throws Exception {
        int attempts = 3;
        long delayMs = 800L;
        Exception lastError = null;

        for (int attempt = 1; attempt <= attempts; attempt++) {
            try {
                return callGeminiApi(url, payload);
            } catch (RuntimeException ex) {
                lastError = ex;
                if (!isTransientGeminiError(ex) || attempt == attempts) {
                    throw ex;
                }
                TimeUnit.MILLISECONDS.sleep(delayMs);
                delayMs *= 2;
            }
        }

        throw lastError;
    }

    private String callGeminiApi(String url, String payload) throws Exception {
        RequestBody body = RequestBody.create(payload, MediaType.parse("application/json"));

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        try (Response response = CLIENT.newCall(request).execute()) {
            String responseBody = response.body() != null ? response.body().string() : "";

            if (!response.isSuccessful()) {
                throw new RuntimeException("Gemini API failed: " + response.code() + " - " + responseBody);
            }

            JsonNode root = MAPPER.readTree(responseBody);
            JsonNode textNode = root.path("candidates")
                    .path(0)
                    .path("content")
                    .path("parts")
                    .path(0)
                    .path("text");

            if (textNode.isMissingNode() || textNode.asText().isBlank()) {
                throw new RuntimeException("Gemini returned empty text: " + responseBody);
            }

            return textNode.asText().trim();
        }
    }

    private String detectMimeType(File file) throws IOException {
        String name = file.getName().toLowerCase();
        if (name.endsWith(".png")) return "image/png";
        if (name.endsWith(".jpg") || name.endsWith(".jpeg")) return "image/jpeg";
        if (name.endsWith(".webp")) return "image/webp";
        if (name.endsWith(".bmp")) return "image/bmp";
        String detected = Files.probeContentType(file.toPath());
        if (detected != null && !detected.isBlank()) {
            return detected;
        }

        return "application/octet-stream";
    }

    private boolean isTransientGeminiError(RuntimeException ex) {
        String message = ex.getMessage() != null ? ex.getMessage() : "";
        return message.contains("503") || message.contains("429") || message.contains("UNAVAILABLE")
                || message.contains("high demand") || message.contains("temporarily");
    }
}