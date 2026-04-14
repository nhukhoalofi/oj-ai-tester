package com.yourteam.ojaitester.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yourteam.ojaitester.config.OpenAIConfig;
import com.yourteam.ojaitester.service.ProblemExtractionService;
import okhttp3.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;

public class ProblemExtractionServiceImpl implements ProblemExtractionService {

    private static final String RESPONSES_URL = "https://api.openai.com/v1/responses";
    private static final String FILES_URL = "https://api.openai.com/v1/files";

    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public String extractTextFromImage(File imageFile) {
        try {
            String mimeType = detectMimeType(imageFile);
            String base64 = Base64.getEncoder().encodeToString(Files.readAllBytes(imageFile.toPath()));
            String dataUrl = "data:" + mimeType + ";base64," + base64;

            String payload = """
                    {
                      "model": "%s",
                      "input": [
                        {
                          "role": "user",
                          "content": [
                            {
                              "type": "input_text",
                              "text": "Extract all readable programming problem text exactly as plain text. Preserve title, statement, input, output, constraints, samples, notes, and formatting as much as possible. Do not explain anything. Return only the extracted text."
                            },
                            {
                              "type": "input_image",
                              "image_url": "%s"
                            }
                          ]
                        }
                      ]
                    }
                    """.formatted(
                    escapeJson(OpenAIConfig.getModel()),
                    escapeJson(dataUrl)
            );

            return callResponsesApi(payload);

        } catch (Exception e) {
            throw new RuntimeException("Failed to extract text from image", e);
        }
    }

    @Override
    public String extractTextFromPdf(File pdfFile) {
        try {
            String fileId = uploadFile(pdfFile);

            String payload = """
                    {
                      "model": "%s",
                      "input": [
                        {
                          "role": "user",
                          "content": [
                            {
                              "type": "input_text",
                              "text": "Extract the full programming problem statement from this PDF as plain text. Preserve title, statement, input, output, constraints, samples, and notes as much as possible. Do not summarize. Return only the extracted text."
                            },
                            {
                              "type": "input_file",
                              "file_id": "%s"
                            }
                          ]
                        }
                      ]
                    }
                    """.formatted(
                    escapeJson(OpenAIConfig.getModel()),
                    escapeJson(fileId)
            );

            return callResponsesApi(payload);

        } catch (Exception e) {
            throw new RuntimeException("Failed to extract text from PDF", e);
        }
    }

    private String uploadFile(File file) throws IOException {
        MediaType mediaType = MediaType.parse("application/octet-stream");

        RequestBody fileBody = RequestBody.create(file, mediaType);

        MultipartBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("purpose", "user_data")
                .addFormDataPart("file", file.getName(), fileBody)
                .build();

        Request request = new Request.Builder()
                .url(FILES_URL)
                .addHeader("Authorization", "Bearer " + OpenAIConfig.getApiKey())
                .post(requestBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String body = response.body() != null ? response.body().string() : "";
                throw new RuntimeException("File upload failed: " + response.code() + " - " + body);
            }

            String json = response.body() != null ? response.body().string() : "";
            JsonNode root = mapper.readTree(json);
            JsonNode idNode = root.get("id");

            if (idNode == null || idNode.asText().isBlank()) {
                throw new RuntimeException("No file id returned from OpenAI.");
            }

            return idNode.asText();
        }
    }

    private String callResponsesApi(String payload) throws IOException {
        RequestBody body = RequestBody.create(
                payload,
                MediaType.parse("application/json")
        );

        Request request = new Request.Builder()
                .url(RESPONSES_URL)
                .addHeader("Authorization", "Bearer " + OpenAIConfig.getApiKey())
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            String json = response.body() != null ? response.body().string() : "";

            if (!response.isSuccessful()) {
                throw new RuntimeException("Responses API failed: " + response.code() + " - " + json);
            }

            JsonNode root = mapper.readTree(json);

            JsonNode outputText = root.get("output_text");
            if (outputText != null && !outputText.asText().isBlank()) {
                return outputText.asText().trim();
            }

            JsonNode output = root.get("output");
            if (output != null && output.isArray()) {
                for (JsonNode item : output) {
                    JsonNode content = item.get("content");
                    if (content != null && content.isArray()) {
                        for (JsonNode c : content) {
                            if ("output_text".equals(c.path("type").asText())) {
                                String text = c.path("text").asText();
                                if (!text.isBlank()) {
                                    return text.trim();
                                }
                            }
                        }
                    }
                }
            }

            throw new RuntimeException("Could not find extracted text in OpenAI response.");
        }
    }

    private String detectMimeType(File file) throws IOException {
        String detected = Files.probeContentType(file.toPath());
        if (detected != null && !detected.isBlank()) {
            return detected;
        }

        String name = file.getName().toLowerCase();
        if (name.endsWith(".png")) return "image/png";
        if (name.endsWith(".jpg") || name.endsWith(".jpeg")) return "image/jpeg";
        if (name.endsWith(".webp")) return "image/webp";
        if (name.endsWith(".bmp")) return "image/bmp";
        return "application/octet-stream";
    }

    private String escapeJson(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}