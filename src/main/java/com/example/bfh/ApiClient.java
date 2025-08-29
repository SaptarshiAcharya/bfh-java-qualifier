package com.example.bfh;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@Component
public class ApiClient {
    private static final Logger log = LoggerFactory.getLogger(ApiClient.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${app.endpoints.base}")
    private String baseUrl;

    @Value("${app.endpoints.generateWebhook}")
    private String generateWebhookPath;

    @Value("${app.endpoints.submitFallback}")
    private String submitFallbackPath;

    public ApiClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public GenerateResponse generateWebhook(ParticipantInfo participant) {
        String url = baseUrl + generateWebhookPath;
        Map<String, String> body = new HashMap<>();
        body.put("name", participant.name());
        body.put("regNo", participant.regNo());
        body.put("email", participant.email());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

        ResponseEntity<String> resp = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
        if (!resp.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("generateWebhook failed: " + resp.getStatusCode());
        }

        try {
            JsonNode node = objectMapper.readTree(resp.getBody());
            String webhook = node.path("webhook").asText(null);
            String accessToken = node.path("accessToken").asText(null);
            if (!StringUtils.hasText(accessToken)) {
                throw new IllegalStateException("accessToken missing in response");
            }
            if (!StringUtils.hasText(webhook)) {
                log.warn("Webhook missing in response; will fall back to {}", submitFallbackPath);
                webhook = baseUrl + submitFallbackPath;
            }
            return new GenerateResponse(webhook, accessToken);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse generateWebhook response", e);
        }
    }

    public void submitFinalQuery(String webhookUrl, String accessToken, String finalQuery) {
        Map<String, String> payload = Map.of("finalQuery", finalQuery);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        // Spec says: Use the JWT directly in Authorization header (no 'Bearer ' prefix)
        headers.set("Authorization", accessToken);

        HttpEntity<Map<String, String>> request = new HttpEntity<>(payload, headers);

        ResponseEntity<String> resp = restTemplate.exchange(URI.create(webhookUrl), HttpMethod.POST, request, String.class);
        if (!resp.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("submitFinalQuery failed: " + resp.getStatusCode() + " | body=" + resp.getBody());
        }
    }

    public record GenerateResponse(String webhookUrl, String accessToken) {}
    public record ParticipantInfo(String name, String regNo, String email) {}
}
