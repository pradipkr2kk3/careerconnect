package com.careerconnect.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "experience")
@Getter @Setter @NoArgsConstructor
public class Experience {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long experienceId;
    private Long userId;
    private Long companyId;
    private String companyName;
    private String designation;
    private String roleDescription;
    private LocalDate joiningDate;
    private LocalDate endDate;
}
