package com.careerconnect.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "skill")
@Getter @Setter @NoArgsConstructor
public class Skill {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long skillId;
    private String skillName;
    private String skillCategory;
    private String description;
}
