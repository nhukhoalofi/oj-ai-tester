package com.yourteam.ojaitester.service.impl;

import com.yourteam.ojaitester.model.Problem;
import com.yourteam.ojaitester.repository.ProblemRepository;
import com.yourteam.ojaitester.repository.impl.ProblemRepositoryImpl;
import com.yourteam.ojaitester.service.ProblemService;

import java.util.List;
import java.util.Optional;

public class ProblemServiceImpl implements ProblemService {

    private final ProblemRepository repository = new ProblemRepositoryImpl();

    @Override
    public Problem createProblem(Problem problem) {
        return repository.save(problem);
    }

    @Override
    public List<Problem> getAllProblems() {
        return repository.findAll();
    }

    @Override
    public Optional<Problem> getProblemById(Long id) {
        return repository.findById(id);
    }

    @Override
    public void deleteProblem(Long id) {
        repository.deleteById(id);
    }
}