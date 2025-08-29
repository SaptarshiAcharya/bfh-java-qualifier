package com.example.bfh;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "submissions")
public class Submission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String regNo;
    private String questionType; // ODD or EVEN
    @Lob
    private String finalQuery;
    private Instant createdAt;

    protected Submission() {}

    public Submission(String regNo, String questionType, String finalQuery, Instant createdAt) {
        this.regNo = regNo;
        this.questionType = questionType;
        this.finalQuery = finalQuery;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public String getRegNo() { return regNo; }
    public String getQuestionType() { return questionType; }
    public String getFinalQuery() { return finalQuery; }
    public Instant getCreatedAt() { return createdAt; }
}
