package com.careerconnect.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "company")
@Getter @Setter @NoArgsConstructor
public class Company {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long companyId;
    private String companyName;
    private String companyType;
    private String companyLocation;
    private String companyEmail;
    private String companyContact;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;
}
