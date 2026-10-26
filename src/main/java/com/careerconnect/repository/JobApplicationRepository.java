package com.careerconnect.repository;

import com.careerconnect.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {
    boolean existsByUserIdAndJobId(Long userId, Long jobId);
}
