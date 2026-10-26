package com.careerconnect.repository;

import com.careerconnect.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CompanyRepository extends JpaRepository<Company, Long> {
    Optional<Company> findByCompanyEmail(String companyEmail);
    boolean existsByCompanyEmail(String companyEmail);
}
