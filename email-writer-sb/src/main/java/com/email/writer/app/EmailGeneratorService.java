package com.email.writer.app;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

@Service
public class EmailGeneratorService {

    private final WebClient webClient;
    private final String API_KEY = "AIzaSyDBPhnHaBdzPVZMsj92G-HIKk8I3q-YTs8"; // Gemini API key

    public EmailGeneratorService() {
        this.webClient = WebClient.builder()
                .baseUrl("https://generativelanguage.googleapis.com")
                .build();
    }

    public String generateEmailReply(String userMessage, String tone) {
        try {
            String prompt = "Generate a professional email reply for the following email content (do not include subject).";
            if (tone != null && !tone.isEmpty()) {
                prompt += " Use a " + tone + " tone.";
            }
            prompt += "\nOriginal email:\n" + userMessage;

            Map<String, Object> requestBody = Map.of(
                    "contents", List.of(
                            Map.of(
                                    "parts", List.of(
                                            Map.of("text", prompt)
                                    )
                            )
                    )
            );

            String url = "/v1beta/models/gemini-2.0-flash:generateContent?key=" + API_KEY;

            String response = webClient.post()
                    .uri(url)
                    .header("Content-Type", "application/json")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            // Parse JSON and extract the reply text
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response);
            return root.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();

        } catch (Exception e) {
            e.printStackTrace();
            return "Error generating email: " + e.toString();
        }
    }
}
