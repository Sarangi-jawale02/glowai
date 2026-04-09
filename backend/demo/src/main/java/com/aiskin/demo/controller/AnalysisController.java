package com.aiskin.demo.controller;

import com.aiskin.demo.model.AnalysisRequest;
import com.aiskin.demo.model.AnalysisResponse;
import com.aiskin.demo.service.AnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")  // Allows your frontend to call this
public class AnalysisController {

    @Autowired
    private AnalysisService analysisService;

    @PostMapping("/analyze")
    public ResponseEntity<AnalysisResponse> analyze(@RequestBody AnalysisRequest request) {
        AnalysisResponse response = analysisService.analyze(request);
        return ResponseEntity.ok(response);
    }
    @PostMapping("/chat")
    public ResponseEntity<Map<String, String>> chat(@RequestBody Map<String, String> body) {
        String userMessage = body.get("message");
        String botReply = analysisService.chat(userMessage);
        return ResponseEntity.ok(Map.of("reply", botReply));
    }

    // Health check — open browser and go to localhost:8080/api/health to test
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("GlowAI backend is running!");
    }

}