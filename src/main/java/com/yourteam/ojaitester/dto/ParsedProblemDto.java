package com.yourteam.ojaitester.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ParsedProblemDto {
    private String title = "";
    private String statement = "";
    @JsonProperty("input_format")
    private String input_format = "";
    @JsonProperty("output_format")
    private String output_format = "";
    private String constraints = "";
    private List<String> tags = new ArrayList<>();
    private String summary = "";

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title != null ? title : "";
    }

    public String getStatement() {
        return statement;
    }

    public void setStatement(String statement) {
        this.statement = statement != null ? statement : "";
    }

    public String getInput_format() {
        return input_format;
    }

    public void setInput_format(String input_format) {
        this.input_format = input_format != null ? input_format : "";
    }

    public String getOutput_format() {
        return output_format;
    }

    public void setOutput_format(String output_format) {
        this.output_format = output_format != null ? output_format : "";
    }

    public String getConstraints() {
        return constraints;
    }

    public void setConstraints(String constraints) {
        this.constraints = constraints != null ? constraints : "";
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags != null ? tags : new ArrayList<>();
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary != null ? summary : "";
    }
}
