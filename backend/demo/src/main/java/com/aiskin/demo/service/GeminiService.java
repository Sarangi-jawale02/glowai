//package com.aiskin.demo.service;
//
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//
//import java.net.URI;
//import java.net.http.HttpClient;
//import java.net.http.HttpRequest;
//import java.net.http.HttpResponse;
//import java.util.Map;
//
//@Service
//public class GeminiService {
//
//    @Value("${gemini.api.key}")
//    private String apiKey = "";  // reads from application.properties
//
//    private final HttpClient httpClient = HttpClient.newHttpClient();
//    private final ObjectMapper objectMapper = new ObjectMapper();
//
//    public String analyzeImage(String base64Image, String age, String skinType,
//                               String primaryConcern, String waterIntake, String stressLevel) {
//
//        try {
//            // Remove the "data:image/jpeg;base64," prefix if present
//            String imageData = base64Image;
//            if (base64Image.contains(",")) {
//                imageData = base64Image.split(",")[1];
//            }
//
//            // Build the prompt
//            String prompt = String.format(
//                    "You are a dermatology assistant. Analyze this facial skin image.\n\n" +
//                            "Patient info:\n" +
//                            "- Age: %s\n" +
//                            "- Skin type: %s\n" +
//                            "- Primary concern: %s\n" +
//                            "- Daily water intake: %s glasses\n" +
//                            "- Stress level: %s out of 5\n\n" +
//                            "Respond ONLY in this exact JSON format, nothing else:\n" +
//                            "{\n" +
//                            "  \"diagnosis\": [\"issue1\", \"issue2\", \"issue3\"],\n" +
//                            "  \"severity\": \"Mild or Moderate or Severe\",\n" +
//                            "  \"confidence\": 85,\n" +
//                            "  \"routine\": {\n" +
//                            "    \"morning\": [\"Step 1\", \"Step 2\", \"Step 3\"],\n" +
//                            "    \"evening\": [\"Step 1\", \"Step 2\", \"Step 3\"]\n" +
//                            "  }\n" +
//                            "}",
//                    age, skinType, primaryConcern, waterIntake, stressLevel
//            );
//
//            // Build the request body for Gemini
//            String requestBody = objectMapper.writeValueAsString(Map.of(
//                    "contents", new Object[]{
//                            Map.of("parts", new Object[]{
//                                    Map.of("text", prompt),
//                                    Map.of("inline_data", Map.of(
//                                            "mime_type", "image/jpeg",
//                                            "data", imageData
//                                    ))
//                            })
//                    }
//            ));
//
//            // Send request to Gemini
//            HttpRequest request = HttpRequest.newBuilder()
//                    .uri(URI.create("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-latest:generateContent?key=" + apiKey))
//                    .header("Content-Type", "application/json")
//                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
//                    .build();
//
//            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
//            System.out.println("=== GEMINI RAW RESPONSE ===");
//            System.out.println(response.body());
//
//            // Extract the text from Gemini's response
//            JsonNode root = objectMapper.readTree(response.body());
//           // Safely extract text from Gemini response
//            JsonNode candidates = root.path("candidates");
//            if (candidates.isEmpty()) {
//                throw new RuntimeException("Gemini returned no candidates. Check your API key.");
//            }
//
//            String aiText = candidates
//                    .get(0)
//                    .path("content")
//                    .path("parts")
//                    .get(0)
//                    .path("text")
//                    .asText();
//                    System.out.println("=== EXTRACTED TEXT ===");
//                    System.out.println(aiText);
//
//
//
//            // Clean up in case Gemini wraps it in markdown code blocks
//            aiText = aiText.replace("```json", "").replace("```", "").trim();
//
//            return aiText;
//
//        } catch (Exception e) {
//            throw new RuntimeException("Gemini API call failed: " + e.getMessage());
//        }
//    }
//}


package com.aiskin.demo.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

@Service
public class GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey = "";

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String analyzeImage(String base64Image, String age, String skinType,
                               String primaryConcern, String waterIntake,
                               String stressLevel) {
        try {
            // Remove base64 prefix to get pure data
            String imageData = base64Image.contains(",")
                    ? base64Image.split(",")[1]
                    : base64Image;

            String prompt =
                    "You are a dermatology assistant. Look carefully at this face photo.\n\n" +
                            "IMPORTANT: Actually analyze what you visually see in the image.\n" +
                            "Look for: acne, pimples, blackheads, whiteheads, dark spots, " +
                            "redness, dryness, oiliness, dark circles, enlarged pores, " +
                            "uneven skin tone, fine lines.\n\n" +
                            "Also consider this patient info:\n" +
                            "- Age: " + age + "\n" +
                            "- Skin type: " + skinType + "\n" +
                            "- Primary concern: " + primaryConcern + "\n" +
                            "- Daily water intake: " + waterIntake + " glasses\n" +
                            "- Stress level: " + stressLevel + " out of 5\n\n" +
                            "Respond ONLY in this exact JSON format, nothing else:\n" +
                            "{\n" +
                            "  \"diagnosis\": [\"Condition 1 seen in photo\", " +
                            "\"Condition 2 seen in photo\", \"Condition 3\"],\n" +
                            "  \"severity\": \"Mild\",\n" +
                            "  \"confidence\": 85,\n" +
                            "  \"routine\": {\n" +
                            "    \"morning\": [\"Step 1\", \"Step 2\", \"Step 3\", \"Step 4\"],\n" +
                            "    \"evening\": [\"Step 1\", \"Step 2\", \"Step 3\", \"Step 4\"]\n" +
                            "  }\n" +
                            "}";

            // Build request WITH image — Gemini Vision
            String requestBody = objectMapper.writeValueAsString(Map.of(
                    "contents", new Object[]{
                            Map.of("parts", new Object[]{
                                    // PART 1: The actual image
                                    Map.of(
                                            "inline_data", Map.of(
                                                    "mime_type", "image/jpeg",
                                                    "data", imageData
                                            )
                                    ),
                                    // PART 2: The text prompt
                                    Map.of("text", prompt)
                            })
                    }
            ));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(
                            "https://generativelanguage.googleapis.com/v1beta/models/" +
                                    "gemini-2.5-flash:generateContent?key=" + apiKey
                    ))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request, HttpResponse.BodyHandlers.ofString()
            );

            System.out.println("=== GEMINI VISION RESPONSE ===");
            System.out.println(response.body());

            JsonNode root = objectMapper.readTree(response.body());

            if (root.has("error")) {
                String errorMsg = root.path("error").path("message").asText();
                throw new RuntimeException("Gemini error: " + errorMsg);
            }

            JsonNode candidates = root.path("candidates");
            if (candidates.isEmpty()) {
                throw new RuntimeException("No response from Gemini");
            }

            String aiText = candidates.get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text")
                    .asText();

            System.out.println("=== EXTRACTED TEXT ===");
            System.out.println(aiText);

            // Clean markdown if Gemini wraps in code blocks
            aiText = aiText.replace("```json", "")
                    .replace("```", "")
                    .trim();

            return aiText;

        } catch (Exception e) {
            throw new RuntimeException("Gemini Vision failed: " + e.getMessage());
        }
    }

    public String chatResponse(String userMessage) {
        try {
            String prompt =
                    "You are GlowBot, a friendly and knowledgeable skincare assistant. " +
                            "Answer the following skincare question in a helpful, friendly way. " +
                            "Keep your answer concise (2-4 sentences). " +
                            "If the question is not about skincare, politely say you only help with skincare topics. " +
                            "Add one relevant emoji at the end.\n\n" +
                            "User question: " + userMessage;

            String requestBody = objectMapper.writeValueAsString(Map.of(
                    "contents", new Object[]{
                            Map.of("parts", new Object[]{
                                    Map.of("text", prompt)
                            })
                    }
            ));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(
                            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + apiKey
                    ))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request, HttpResponse.BodyHandlers.ofString()
            );

            JsonNode root = objectMapper.readTree(response.body());

            if (root.has("error")) {
                return "Sorry, I'm having trouble right now. Please try again! 😊";
            }

            return root.path("candidates")
                    .get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text")
                    .asText();

        } catch (Exception e) {
            return "Sorry, something went wrong. Please try again! 😊";
        }
    }
    public List<String> analyzeWithHuggingFace(String base64Image) {
        try {
            String imageData = base64Image.contains(",")
                    ? base64Image.split(",")[1]
                    : base64Image;

            String requestBody = objectMapper.writeValueAsString(Map.of(
                    "data", new Object[]{ "data:image/jpeg;base64," + imageData }
            ));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(
                            "https://pratikskarnik-face-problems-analyzer.hf.space/api/predict"
                    ))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request, HttpResponse.BodyHandlers.ofString()
            );

            System.out.println("=== HUGGINGFACE RESPONSE ===");
            System.out.println(response.body());

            JsonNode root = objectMapper.readTree(response.body());
            JsonNode data = root.path("data");

            List<String> conditions = new ArrayList<>();

            if (data.size() > 0) {
                String label = data.get(0).asText();
                if (!label.isEmpty() && !label.equals("null")) {
                    conditions.add(label);
                }
            }

            if (data.size() > 1 && data.get(1).isObject()) {
                data.get(1).fields().forEachRemaining(entry -> {
                    double confidence = entry.getValue().asDouble();
                    if (confidence > 0.3) {
                        conditions.add(entry.getKey() + " ("
                                + Math.round(confidence * 100) + "%)");
                    }
                });
            }

            return conditions.isEmpty() ? null : conditions;

        } catch (Exception e) {
            System.out.println("HuggingFace failed, falling back to Gemini: "
                    + e.getMessage());
            return null;
        }
    }
}