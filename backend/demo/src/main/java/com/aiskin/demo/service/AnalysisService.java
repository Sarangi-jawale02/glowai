package com.aiskin.demo.service;

import com.aiskin.demo.model.AnalysisRecord;
import com.aiskin.demo.model.AnalysisRequest;
import com.aiskin.demo.model.AnalysisResponse;
import com.aiskin.demo.repository.AnalysisRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class AnalysisService {

    private final GeminiService geminiService;
    private final AnalysisRepository analysisRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AnalysisService(GeminiService geminiService,
                           AnalysisRepository analysisRepository) {
        this.geminiService = geminiService;
        this.analysisRepository = analysisRepository;
    }


    @SuppressWarnings("unchecked")
    public AnalysisResponse analyze(AnalysisRequest request) {
        try {
            // Gemini Vision — analyzes BOTH the photo AND form answers
            System.out.println(">>> Sending image to Gemini Vision...");
            String aiJsonResponse = geminiService.analyzeImage(
                    request.getImage(),
                    request.getAge(),
                    request.getSkinType(),
                    request.getPrimaryConcern(),
                    request.getWaterIntake(),
                    request.getStressLevel()
            );

            // Parse response
            Map<String, Object> parsed = objectMapper
                    .readValue(aiJsonResponse, Map.class);

            List<String> diagnosis = (List<String>) parsed.get("diagnosis");
            String severity = (String) parsed.get("severity");
            int confidence = (int) parsed.get("confidence");
            Map<String, List<String>> routine =
                    (Map<String, List<String>>) parsed.get("routine");

            // Save to database
            AnalysisRecord record = new AnalysisRecord();
            record.setAge(request.getAge());
            record.setSkinType(request.getSkinType());
            record.setPrimaryConcern(request.getPrimaryConcern());
            record.setWaterIntake(request.getWaterIntake());
            record.setStressLevel(request.getStressLevel());
            record.setDiagnosisJson(
                    objectMapper.writeValueAsString(diagnosis));
            record.setSeverity(severity);
            record.setConfidence(confidence);
            record.setRoutineJson(
                    objectMapper.writeValueAsString(routine));
            analysisRepository.save(record);

            return new AnalysisResponse(diagnosis, severity, confidence, routine);

        } catch (Exception e) {
            throw new RuntimeException("Analysis failed: " + e.getMessage());
        }
    }

    public String chat(String userMessage) {
        try {
            return geminiService.chatResponse(userMessage);
        } catch (Exception e) {
            return "Sorry, I couldn't process that. Please try again!";
        }
    }
}