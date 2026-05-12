package com.yourteam.ojaitester.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yourteam.ojaitester.dto.GeneratedTestCaseDto;
import com.yourteam.ojaitester.model.GeneratedTestCase;
import com.yourteam.ojaitester.model.ParsedProblem;
import com.yourteam.ojaitester.model.Problem;
import com.yourteam.ojaitester.repository.ParsedProblemRepository;
import com.yourteam.ojaitester.repository.impl.ParsedProblemRepositoryImpl;
import com.yourteam.ojaitester.service.TestcaseGenerationService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GeminiTestcaseGenerationService implements TestcaseGenerationService {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final GeminiService geminiService;
    private final ParsedProblemRepository parsedProblemRepository;

    public GeminiTestcaseGenerationService() {
        this(null, new ParsedProblemRepositoryImpl());
    }

    GeminiTestcaseGenerationService(GeminiService geminiService, ParsedProblemRepository parsedProblemRepository) {
        this.geminiService = geminiService;
        this.parsedProblemRepository = parsedProblemRepository;
    }

    @Override
    public List<GeneratedTestCase> generateTestcases(Problem problem) {
        if (problem == null || problem.getId() == null) {
            throw new IllegalArgumentException("Please select a problem first.");
        }
        if (problem.getRawText() == null || problem.getRawText().isBlank()) {
            throw new IllegalArgumentException("Problem statement is empty.");
        }

        try {
            Optional<ParsedProblem> parsedProblem = parsedProblemRepository.findByProblemId(problem.getId());
            GeminiService service = geminiService != null ? geminiService : new GeminiService();
            String response = service.generateText(buildPrompt(problem, parsedProblem.orElse(null)));
            List<GeneratedTestCaseDto> dtos = parseResponse(response);
            List<GeneratedTestCase> testCases = toGeneratedTestCases(problem.getId(), dtos);
            if (testCases.isEmpty()) {
                throw new IllegalArgumentException("No testcases generated.");
            }
            return testCases;
        } catch (IllegalArgumentException | IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage() != null ? e.getMessage() : "Gemini testcase generation failed.", e);
        }
    }

    private List<GeneratedTestCaseDto> parseResponse(String response) {
        String cleaned = cleanJsonResponse(response);
        try {
            String json = findTestcaseArrayJson(cleaned);
            if (json.isBlank()) {
                throw new IllegalArgumentException("AI response is not valid JSON array. Raw response: " + abbreviate(cleaned));
            }
            return MAPPER.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            if (e instanceof IllegalArgumentException) {
                throw (IllegalArgumentException) e;
            }
            throw new IllegalArgumentException("AI response is not valid JSON array. Raw response: " + abbreviate(cleaned), e);
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

    private String findTestcaseArrayJson(String text) throws Exception {
        if (text == null || text.isBlank()) {
            return "";
        }

        try {
            JsonNode root = MAPPER.readTree(text);
            JsonNode array = findTestcaseArray(root);
            if (array != null) {
                return MAPPER.writeValueAsString(array);
            }
        } catch (Exception ignored) {
            // Fall back to extracting a bracketed array from mixed text.
        }

        return extractJsonArray(text);
    }

    private JsonNode findTestcaseArray(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }
        if (node.isArray() && looksLikeTestcaseArray(node)) {
            return node;
        }
        if (node.isObject()) {
            String[] likelyFields = {"testcases", "test_cases", "cases", "data", "items"};
            for (String field : likelyFields) {
                JsonNode child = node.get(field);
                if (child != null && child.isArray() && looksLikeTestcaseArray(child)) {
                    return child;
                }
            }
            for (JsonNode child : node) {
                JsonNode found = findTestcaseArray(child);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    private boolean looksLikeTestcaseArray(JsonNode array) {
        if (!array.isArray() || array.isEmpty()) {
            return false;
        }
        JsonNode first = array.get(0);
        return first != null && first.isObject()
                && (first.has("input") || first.has("expected_output") || first.has("category") || first.has("purpose"));
    }

    private String extractJsonArray(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }

        int start = text.indexOf('[');
        int end = text.lastIndexOf(']');
        if (start < 0 || end <= start) {
            return "";
        }
        return text.substring(start, end + 1).trim();
    }

    private String abbreviate(String text) {
        if (text == null || text.isBlank()) {
            return "(empty)";
        }
        String compact = text.replaceAll("\\s+", " ").trim();
        return compact.length() <= 500 ? compact : compact.substring(0, 500) + "...";
    }

    private List<GeneratedTestCase> toGeneratedTestCases(Long problemId, List<GeneratedTestCaseDto> dtos) {
        List<GeneratedTestCase> result = new ArrayList<>();
        if (dtos == null) {
            return result;
        }

        for (GeneratedTestCaseDto dto : dtos) {
            if (dto == null || dto.getInput() == null || dto.getInput().isBlank()) {
                continue;
            }
            GeneratedTestCase testCase = new GeneratedTestCase(
                    problemId,
                    dto.getInput(),
                    safe(dto.getExpectedOutput()),
                    safe(dto.getCategory()),
                    safe(dto.getPurpose())
            );
            result.add(testCase);
        }
        return result;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String buildPrompt(Problem problem, ParsedProblem parsedProblem) {
        String title = firstNonBlank(parsedProblem != null ? parsedProblem.getTitle() : null, problem.getTitle());
        String statement = parsedProblem != null ? parsedProblem.getStatement() : "";
        String inputFormat = parsedProblem != null ? parsedProblem.getInputFormat() : "";
        String outputFormat = parsedProblem != null ? parsedProblem.getOutputFormat() : "";
        String constraints = parsedProblem != null ? parsedProblem.getConstraintsText() : "";
        String tags = parsedProblem != null ? parsedProblem.getTags() : "";
        String rawText = problem.getRawText();

        return """
                Bạn là một trợ lý AI chuyên sinh testcase cho đề bài lập trình thi đấu như IOI, ICPC, Codeforces, VNOI.

                Hãy đọc đề bài được cung cấp và sinh danh sách testcase phù hợp.

                Yêu cầu bắt buộc:
                - Chỉ trả về top-level JSON array hợp lệ, ký tự đầu tiên phải là [ và ký tự cuối cùng phải là ].
                - Không trả về object dạng {"testcases": [...]}.
                - Không dùng markdown.
                - Không bọc kết quả trong ```json hoặc ```.
                - Không giải thích thêm ngoài JSON.
                - Sinh 2 testcase.
                - Testcase phải đúng format input của đề.
                - Không sinh testcase vượt quá constraints.
                - Nếu không chắc expected_output thì để expected_output là "".
                - Cần có nhiều loại testcase: sample, basic, edge, random, max, tricky.
                - Mỗi testcase phải có mục đích rõ ràng trong trường purpose.

                Format JSON bắt buộc:

                [
                  {
                    "input": "",
                    "expected_output": "",
                    "category": "",
                    "purpose": ""
                  }
                ]

                Ý nghĩa:
                - input: dữ liệu đầu vào đầy đủ của testcase.
                - expected_output: output đúng nếu có thể xác định chắc chắn.
                - category: sample, basic, edge, random, max hoặc tricky.
                - purpose: giải thích ngắn testcase này dùng để kiểm tra điều gì.

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
                """.formatted(safe(title), safe(statement), safe(inputFormat), safe(outputFormat),
                safe(constraints), safe(tags), safe(rawText));
    }

    private String firstNonBlank(String first, String second) {
        if (first != null && !first.isBlank()) {
            return first;
        }
        return second != null ? second : "";
    }
}
