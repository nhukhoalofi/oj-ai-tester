package com.yourteam.ojaitester.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yourteam.ojaitester.dto.GeneratedCodeDto;
import com.yourteam.ojaitester.model.ParsedProblem;
import com.yourteam.ojaitester.model.Problem;
import com.yourteam.ojaitester.repository.ParsedProblemRepository;
import com.yourteam.ojaitester.repository.impl.ParsedProblemRepositoryImpl;
import com.yourteam.ojaitester.service.SampleCodeGenerationService;

import java.util.List;
import java.util.Optional;

public class GeminiSampleCodeGenerationService implements SampleCodeGenerationService {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final List<String> VALID_TYPES = List.of("AC", "WA", "TLE");

    private final GeminiService geminiService;
    private final ParsedProblemRepository parsedProblemRepository;

    public GeminiSampleCodeGenerationService() {
        this(null, new ParsedProblemRepositoryImpl());
    }

    GeminiSampleCodeGenerationService(GeminiService geminiService, ParsedProblemRepository parsedProblemRepository) {
        this.geminiService = geminiService;
        this.parsedProblemRepository = parsedProblemRepository;
    }

    @Override
    public GeneratedCodeDto generateCode(Problem problem, String codeType) {
        String normalizedType = normalizeCodeType(codeType);
        validateProblem(problem);

        try {
            Optional<ParsedProblem> parsedProblem = parsedProblemRepository.findByProblemId(problem.getId());
            GeminiService service = geminiService != null ? geminiService : new GeminiService();
            GeneratedCodeDto dto = parseResponse(service.generateText(buildPrompt(problem, parsedProblem.orElse(null), normalizedType)));
            validateGeneratedCode(dto, normalizedType);
            return dto;
        } catch (IllegalArgumentException | IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage() != null ? e.getMessage() : "Gemini code generation failed.", e);
        }
    }

    private void validateProblem(Problem problem) {
        if (problem == null || problem.getId() == null) {
            throw new IllegalArgumentException("Please select a problem first.");
        }
        if (problem.getRawText() == null || problem.getRawText().isBlank()) {
            throw new IllegalArgumentException("Problem statement is empty.");
        }
    }

    private String normalizeCodeType(String codeType) {
        if (codeType == null || codeType.isBlank()) {
            throw new IllegalArgumentException("Please select code type.");
        }
        String normalized = codeType.trim().toUpperCase();
        if (!VALID_TYPES.contains(normalized)) {
            throw new IllegalArgumentException("Please select code type.");
        }
        return normalized;
    }

    private GeneratedCodeDto parseResponse(String response) {
        String json = cleanJsonResponse(response);
        if (!json.startsWith("{") || !json.endsWith("}")) {
            throw new IllegalArgumentException("AI response is not valid JSON.");
        }

        try {
            return MAPPER.readValue(json, GeneratedCodeDto.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("AI response is not valid JSON.", e);
        }
    }

    private String cleanJsonResponse(String response) {
        if (response == null) {
            return "";
        }
        return response.replace("```json", "")
                .replace("```JSON", "")
                .replace("```", "")
                .trim();
    }

    private void validateGeneratedCode(GeneratedCodeDto dto, String expectedType) {
        if (dto == null || dto.getCode() == null || dto.getCode().isBlank() || !dto.getCode().contains("main")) {
            throw new IllegalArgumentException("Generated code is invalid.");
        }
        if (dto.getCodeType() == null || !dto.getCodeType().trim().equalsIgnoreCase(expectedType)) {
            throw new IllegalArgumentException("Generated code type does not match selected type.");
        }
        if (dto.getLanguage() == null || dto.getLanguage().isBlank()) {
            dto.setLanguage("CPP");
        }
        dto.setCodeType(expectedType);
    }

    private String buildPrompt(Problem problem, ParsedProblem parsedProblem, String codeType) {
        String title = firstNonBlank(parsedProblem != null ? parsedProblem.getTitle() : null, problem.getTitle());
        String statement = parsedProblem != null ? parsedProblem.getStatement() : "";
        String inputFormat = parsedProblem != null ? parsedProblem.getInputFormat() : "";
        String outputFormat = parsedProblem != null ? parsedProblem.getOutputFormat() : "";
        String constraints = parsedProblem != null ? parsedProblem.getConstraintsText() : "";
        String tags = parsedProblem != null ? parsedProblem.getTags() : "";

        return """
                Bạn là một chuyên gia lập trình thi đấu IOI, ICPC, Codeforces, VNOI.

                Hãy đọc đề bài lập trình dưới đây và sinh đúng 1 đoạn code C++17 theo loại được yêu cầu.

                Loại code cần sinh:
                %s

                Quy tắc chung:
                - Chỉ trả về JSON hợp lệ.
                - Không dùng markdown.
                - Không bọc trong ```json.
                - Không giải thích ngoài JSON.
                - Code phải là một chuỗi JSON hợp lệ, escape ký tự xuống dòng bằng \\n.
                - Không dùng thư viện ngoài chuẩn C++.
                - Không dùng system call.
                - Không đọc/ghi file.
                - Không truy cập mạng.
                - Code phải có hàm main.
                - Code phải compile được bằng g++ C++17.

                Nếu CODE_TYPE = AC:
                - Sinh lời giải đúng hoàn toàn.
                - Đọc input đúng format.
                - In output đúng format.
                - Thuật toán phải phù hợp với constraints nếu có.

                Nếu CODE_TYPE = WA:
                - Sinh lời giải sai có chủ đích.
                - Code vẫn phải compile được.
                - Code vẫn phải đọc input và in output đúng format.
                - Lỗi sai nên là lỗi phổ biến như sai công thức, thiếu edge case, overflow, sai điều kiện biên hoặc thuật toán chưa đủ mạnh.
                - Không được chỉ in hằng số cố định cho mọi input nếu không cần thiết.
                - Phải giải thích ngắn lỗi sai trong trường explanation.

                Nếu CODE_TYPE = TLE:
                - Sinh lời giải có khả năng chạy quá thời gian.
                - Code vẫn phải compile được.
                - Code vẫn đọc input đúng format.
                - Ưu tiên dùng thuật toán chậm hơn nhiều so với lời giải đúng.
                - Nếu đề quá đơn giản, có thể dùng vòng lặp rất lớn hoặc vòng lặp vô hạn sau khi đọc input.
                - Không dùng thao tác nguy hiểm với hệ thống.
                - Phải giải thích ngắn vì sao code có thể bị TLE trong trường explanation.

                Format JSON bắt buộc:

                {
                  "code_type": "%s",
                  "language": "CPP",
                  "code": "",
                  "explanation": ""
                }

                Thông tin đề bài:

                Title:
                %s

                Statement:
                %s

                Input format:
                %s

                Output format:
                %s

                Constraints:
                %s

                Tags:
                %s

                Raw problem text:
                %s
                """.formatted(codeType, codeType, safe(title), safe(statement), safe(inputFormat),
                safe(outputFormat), safe(constraints), safe(tags), safe(problem.getRawText()));
    }

    private String firstNonBlank(String first, String second) {
        if (first != null && !first.isBlank()) {
            return first;
        }
        return second != null ? second : "";
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
