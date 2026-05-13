package com.yourteam.ojaitester.service;

import com.yourteam.ojaitester.dto.GeneratedCodeDto;
import com.yourteam.ojaitester.model.Problem;

public interface SampleCodeGenerationService {
    GeneratedCodeDto generateCode(Problem problem, String codeType);
}
