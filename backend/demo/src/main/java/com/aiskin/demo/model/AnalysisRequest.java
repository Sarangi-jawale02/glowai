package com.aiskin.demo.model;

import lombok.Data;

@Data  // Lombok generates getters/setters automatically
public class AnalysisRequest {
    private String image;          // base64 image from frontend
    private String age;
    private String skinType;
    private String primaryConcern;
    private String waterIntake;
    private String stressLevel;
}
