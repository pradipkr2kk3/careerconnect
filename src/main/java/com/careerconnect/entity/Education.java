package com.careerconnect.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "education")
@Getter @Setter @NoArgsConstructor
public class Education {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long educationId;
    private Long userId;
    private String instituteName;
    private String degree;
    private Integer startYear;
    private Integer endYear;
    private String grade;
}
