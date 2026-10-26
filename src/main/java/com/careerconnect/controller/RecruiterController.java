package com.careerconnect.controller;

import com.careerconnect.dao.QueryDao;
import com.careerconnect.entity.Job;
import com.careerconnect.repository.JobRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.careerconnect.security.AuthHelper.*;

/** Everything a logged-in recruiter (company) can do. */
@RestController
@RequestMapping("/api/recruiter")
@RequiredArgsConstructor
public class RecruiterController {

    private static final Set<String> STATUSES = Set.of("APPLIED", "SHORTLISTED", "REJECTED", "HIRED");

    private final QueryDao dao;
    private final JobRepository jobs;

    @GetMapping("/dashboard")
    public Map<String, Object> dashboard(HttpSession s) {
        return dao.recruiterDashboard(require(s, RECRUITER));
    }

    @GetMapping("/jobs")
    public List<Map<String, Object>> myJobs(HttpSession s) {
        return dao.companyJobs(require(s, RECRUITER));
    }

    @PostMapping("/jobs")
    public Map<String, Object> postJob(@RequestBody Job body, HttpSession s) {
        Long cid = require(s, RECRUITER);
        need(body.getJobTitle(), "Job title");
        need(body.getJobDescription(), "Job description");
        body.setJobId(null);
        body.setCompanyId(cid);
        body.setJobPostDate(LocalDate.now());
        jobs.save(body);
        return Map.of("message", "Job posted");
    }

    @DeleteMapping("/jobs/{id}")
    public Map<String, Object> deleteJob(@PathVariable Long id, HttpSession s) {
        Long cid = require(s, RECRUITER);
        Job j = jobs.findById(id).orElseThrow(() -> notFound("Job not found"));
        if (!j.getCompanyId().equals(cid)) throw forbidden("Not your job");
        jobs.delete(j);
        return Map.of("message", "Job deleted");
    }

    @GetMapping("/applications")
    public List<Map<String, Object>> applications(HttpSession s) {
        return dao.companyApplications(require(s, RECRUITER));
    }

    @PutMapping("/applications/{id}/status")
    public Map<String, Object> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body, HttpSession s) {
        Long cid = require(s, RECRUITER);
        String status = body.getOrDefault("status", "").toUpperCase();
        if (!STATUSES.contains(status)) throw bad("Invalid status");
        if (dao.updateApplicationStatus(cid, id, status) == 0) throw notFound("Application not found");
        return Map.of("message", "Status updated");
    }
}
