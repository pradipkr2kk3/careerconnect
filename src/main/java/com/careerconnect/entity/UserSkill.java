package com.careerconnect.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_skill")
@IdClass(Ids.UserSkillKey.class)
@Getter @Setter @NoArgsConstructor
public class UserSkill {
    @Id private Long userId;
    @Id private Long skillId;
    private String proficiencyLevel;
    private Integer yearOfExperience;
}
