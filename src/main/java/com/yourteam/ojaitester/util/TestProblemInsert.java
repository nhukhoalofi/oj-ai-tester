package com.yourteam.ojaitester.util;

import com.yourteam.ojaitester.model.Problem;
import com.yourteam.ojaitester.service.ProblemService;
import com.yourteam.ojaitester.service.impl.ProblemServiceImpl;

public class TestProblemInsert {
    public static void main(String[] args) {
        ProblemService service = new ProblemServiceImpl();

        Problem p = new Problem();
        p.setTitle("Sample Problem");
        p.setRawText("Given n numbers, find the maximum.");
        p.setSourcePath(null);
        p.setSourceType("TEXT");
        p.setStatus("NEW");

        Problem saved = service.createProblem(p);
        System.out.println("Saved problem ID: " + saved.getId());
    }
}