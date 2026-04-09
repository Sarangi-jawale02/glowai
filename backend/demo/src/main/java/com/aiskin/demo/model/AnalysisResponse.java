package com.aiskin.demo.model;

import lombok.Data;
import lombok.AllArgsConstructor;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class AnalysisResponse {
    private List<String> diagnosis;
    private String severity;
    private int confidence;
    private Map<String, List<String>> routine;
    // routine will be: {"morning": [...], "evening": [...]}
}