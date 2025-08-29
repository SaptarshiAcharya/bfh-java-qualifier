package com.example.bfh;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

@Service
public class SubmissionService {
    private static final Logger log = LoggerFactory.getLogger(SubmissionService.class);

    private final ApiClient apiClient;
    private final SubmissionRepository repo;

    @Value("${app.participant.name}")
    private String name;
    @Value("${app.participant.regNo}")
    private String regNo;
    @Value("${app.participant.email}")
    private String email;

    @Value("${app.sqlPaths.odd}")
    private Resource oddSql;
    @Value("${app.sqlPaths.even}")
    private Resource evenSql;

    public SubmissionService(ApiClient apiClient, SubmissionRepository repo) {
        this.apiClient = apiClient;
        this.repo = repo;
    }

    public void runFlow() throws Exception {
        log.info("Starting flow for {}, regNo={}", name, regNo);

        // 1) Generate webhook + JWT token
        ApiClient.ParticipantInfo pi = new ApiClient.ParticipantInfo(name, regNo, email);
        ApiClient.GenerateResponse resp = apiClient.generateWebhook(pi);
        log.info("Generated webhook: {}", resp.webhookUrl());

        // 2) Decide which SQL to use based on last two digits of regNo
        int lastTwo = RegNoUtil.lastTwoDigits(regNo);
        if (lastTwo < 0) throw new IllegalArgumentException("Registration number does not contain digits: " + regNo);
        boolean isOdd = RegNoUtil.isOdd(lastTwo);
        String questionType = isOdd ? "ODD" : "EVEN";
        String finalQuery = readResource(isOdd ? oddSql : evenSql).trim();
        if (finalQuery.isBlank()) {
            throw new IllegalStateException("Your SQL file is empty. Please put your final query into " + (isOdd ? oddSql : evenSql));
        }
        log.info("Question type={}, lastTwo={}, SQL length={}", questionType, lastTwo, finalQuery.length());

        // 3) Store locally (H2)
        Submission saved = repo.save(new Submission(regNo, questionType, finalQuery, Instant.now()));
        log.info("Saved submission id={}", saved.getId());

        // 4) Submit to webhook using Authorization header with JWT token
        apiClient.submitFinalQuery(resp.webhookUrl(), resp.accessToken(), finalQuery);
        log.info("Submission sent successfully. Done.");
    }

    private static String readResource(Resource resource) throws Exception {
        try (Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
            return FileCopyUtils.copyToString(reader);
        }
    }
}
