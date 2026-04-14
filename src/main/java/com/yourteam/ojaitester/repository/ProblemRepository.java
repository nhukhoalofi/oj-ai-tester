package com.yourteam.ojaitester.repository;

import com.yourteam.ojaitester.model.Problem;

import java.util.List;
import java.util.Optional;

public interface ProblemRepository {
    Problem save(Problem problem);
    List<Problem> findAll();
    Optional<Problem> findById(Long id);
    void deleteById(Long id);
}