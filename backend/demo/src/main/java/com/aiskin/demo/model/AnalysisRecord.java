package com.aiskin.demo.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "analysis_records")
public class AnalysisRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String age;
    private String skinType;
    private String primaryConcern;
    private String waterIntake;
    private String stressLevel;

    @Column(columnDefinition = "TEXT")
    private String diagnosisJson;    // we'll store the list as JSON string

    private String severity;
    private int confidence;

    @Column(columnDefinition = "TEXT")
    private String routineJson;      // store routine as JSON string

    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }
}