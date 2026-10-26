package com.careerconnect.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "job")
@Getter @Setter @NoArgsConstructor
public class Job {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long jobId;
    private Long companyId;
    private String jobTitle;
    private String jobRole;
    private String jobDescription;
    private BigDecimal salary;
    private LocalDate jobPostDate;
    private String skillRequired;
}
