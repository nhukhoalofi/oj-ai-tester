package com.yourteam.ojaitester.util;

import com.yourteam.ojaitester.service.CheckerService;
import com.yourteam.ojaitester.service.CheckerType;

public class CheckerServiceTest {
    public static void main(String[] args) {
        CheckerService checker = new CheckerService();

        assertAccepted(checker.isAccepted("1 2 3", "1   2   3", CheckerType.TOKEN), "TOKEN ignores repeated spaces");
        assertAccepted(checker.isAccepted("1\n2\n3", "1 2 3", CheckerType.TOKEN), "TOKEN ignores line breaks");
        assertRejected(checker.isAccepted("1 2 4", "1 2 3", CheckerType.TOKEN), "TOKEN detects different token");
        assertAccepted(checker.isAccepted("hello\nworld", "hello\nworld", CheckerType.EXACT), "EXACT accepts identical output");
        assertRejected(checker.isAccepted("hello world", "hello\nworld", CheckerType.EXACT), "EXACT detects line difference");

        System.out.println("CheckerService tests passed.");
    }

    private static void assertAccepted(boolean value, String message) {
        if (!value) {
            throw new AssertionError(message);
        }
    }

    private static void assertRejected(boolean value, String message) {
        if (value) {
            throw new AssertionError(message);
        }
    }
}
