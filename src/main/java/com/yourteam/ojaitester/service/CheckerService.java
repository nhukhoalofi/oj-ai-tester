package com.yourteam.ojaitester.service;

import java.util.Arrays;

public class CheckerService {

    public boolean isAccepted(String actualOutput, String expectedOutput, CheckerType checkerType) {
        CheckerType type = checkerType != null ? checkerType : CheckerType.TOKEN;
        String actual = actualOutput != null ? actualOutput : "";
        String expected = expectedOutput != null ? expectedOutput : "";

        return switch (type) {
            case EXACT -> normalizeExact(actual).equals(normalizeExact(expected));
            case TOKEN -> tokensEqual(actual, expected);
        };
    }

    private String normalizeExact(String output) {
        return Arrays.stream(normalizeLineEndings(output).trim().split("\n", -1))
                .map(String::trim)
                .reduce((left, right) -> left + "\n" + right)
                .orElse("");
    }

    private boolean tokensEqual(String actualOutput, String expectedOutput) {
        String actual = normalizeLineEndings(actualOutput).trim();
        String expected = normalizeLineEndings(expectedOutput).trim();
        if (actual.isEmpty() && expected.isEmpty()) {
            return true;
        }
        if (actual.isEmpty() || expected.isEmpty()) {
            return false;
        }

        String[] actualTokens = actual.split("\\s+");
        String[] expectedTokens = expected.split("\\s+");
        return Arrays.equals(actualTokens, expectedTokens);
    }

    private String normalizeLineEndings(String output) {
        return output.replace("\r\n", "\n").replace('\r', '\n');
    }
}
