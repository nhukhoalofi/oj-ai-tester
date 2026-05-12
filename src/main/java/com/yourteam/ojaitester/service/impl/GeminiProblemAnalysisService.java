package com.yourteam.ojaitester.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yourteam.ojaitester.dto.ParsedProblemDto;
import com.yourteam.ojaitester.model.ParsedProblem;
import com.yourteam.ojaitester.model.Problem;
import com.yourteam.ojaitester.repository.ParsedProblemRepository;
import com.yourteam.ojaitester.repository.impl.ParsedProblemRepositoryImpl;
import com.yourteam.ojaitester.service.ProblemAnalysisService;

import java.util.Optional;

public class GeminiProblemAnalysisService implements ProblemAnalysisService {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final GeminiService geminiService;
    private final ParsedProblemRepository parsedProblemRepository;

    public GeminiProblemAnalysisService() {
        this(null, new ParsedProblemRepositoryImpl());
    }

    GeminiProblemAnalysisService(GeminiService geminiService, ParsedProblemRepository parsedProblemRepository) {
        this.geminiService = geminiService;
        this.parsedProblemRepository = parsedProblemRepository;
    }

    @Override
    public ParsedProblem analyzeAndSave(Problem problem) {
        if (problem == null || problem.getId() == null) {
            throw new IllegalArgumentException("Please select a problem first.");
        }
        if (problem.getRawText() == null || problem.getRawText().isBlank()) {
            throw new IllegalArgumentException("Problem statement is empty.");
        }

        try {
            GeminiService service = geminiService != null ? geminiService : new GeminiService();
            String response = service.generateText(buildPrompt(problem.getRawText()));
            ParsedProblemDto dto = parseResponse(response);
            ParsedProblem parsedProblem = toParsedProblem(problem.getId(), dto);
            return parsedProblemRepository.saveOrUpdate(parsedProblem);
        } catch (IllegalArgumentException | IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage() != null ? e.getMessage() : "Gemini analysis failed.", e);
        }
    }

    @Override
    public Optional<ParsedProblem> getParsedProblem(Long problemId) {
        if (problemId == null) {
            return Optional.empty();
        }
        return parsedProblemRepository.findByProblemId(problemId);
    }

    private ParsedProblemDto parseResponse(String response) {
        String cleaned = cleanJsonResponse(response);
        String json = extractJsonObject(cleaned);
        if (json.isBlank()) {
            throw new IllegalArgumentException("AI response is not valid JSON.");
        }

        try {
            return MAPPER.readValue(json, ParsedProblemDto.class);
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

    private String extractJsonObject(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }

        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start < 0 || end <= start) {
            return "";
        }
        return text.substring(start, end + 1).trim();
    }

    private ParsedProblem toParsedProblem(Long problemId, ParsedProblemDto dto) throws Exception {
        ParsedProblem parsedProblem = new ParsedProblem();
        parsedProblem.setProblemId(problemId);
        parsedProblem.setTitle(dto.getTitle());
        parsedProblem.setStatement(dto.getStatement());
        parsedProblem.setInputFormat(dto.getInput_format());
        parsedProblem.setOutputFormat(dto.getOutput_format());
        parsedProblem.setConstraintsText(dto.getConstraints());
        parsedProblem.setTags(MAPPER.writeValueAsString(dto.getTags()));
        parsedProblem.setSummary(dto.getSummary());
        return parsedProblem;
    }

    private String buildPrompt(String problemText) {
        return """
                Bạn là một trợ lý AI chuyên phân tích đề bài lập trình thi đấu như IOI, ICPC, Codeforces, VNOI.

                Hãy đọc nội dung đề bài bên dưới và trích xuất thông tin thành JSON hợp lệ.

                Yêu cầu bắt buộc:
                - Chỉ trả về JSON, không giải thích thêm.
                - Không dùng markdown, không bọc trong ```json.
                - Nếu thiếu thông tin, dùng chuỗi rỗng "" hoặc mảng rỗng [].
                - Không tự bịa thêm dữ liệu nếu đề không cung cấp.
                - Giữ nguyên ngôn ngữ của đề bài.
                - Tags chỉ gồm các chủ đề thuật toán phù hợp như: implementation, math, greedy, dynamic programming, graph, shortest path, binary search, string, data structures, sorting, number theory, brute force.

                Format JSON bắt buộc:

                {
                  "title": "",
                  "statement": "",
                  "input_format": "",
                  "output_format": "",
                  "constraints": "",
                  "tags": [],
                  "summary": ""
                }

                Ý nghĩa:
                - title: tên bài toán.
                - statement: mô tả bài toán.
                - input_format: định dạng input.
                - output_format: định dạng output.
                - constraints: ràng buộc bài toán.
                - tags: dạng bài hoặc thuật toán liên quan.
                - summary: tóm tắt bài toán trong 1-3 câu.

                Nội dung đề bài cần phân tích:

                %s
                """.formatted(problemText);
    }
}
