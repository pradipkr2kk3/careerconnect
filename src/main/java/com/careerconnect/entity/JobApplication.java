package com.careerconnect.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "job_application")
@Getter @Setter @NoArgsConstructor
public class JobApplication {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long applicationId;
    private Long userId;
    private Long jobId;
    private LocalDate applicationDate;
    private String applicationStatus;
}
