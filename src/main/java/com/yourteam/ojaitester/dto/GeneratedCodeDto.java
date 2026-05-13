package com.yourteam.ojaitester.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GeneratedCodeDto {
    @JsonProperty("code_type")
    private String codeType;
    private String language;
    private String code;
    private String explanation;

    public String getCodeType() {
        return codeType;
    }

    public void setCodeType(String codeType) {
        this.codeType = codeType;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }
}
