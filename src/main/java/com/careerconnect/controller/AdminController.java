package com.careerconnect.controller;

import com.careerconnect.dao.QueryDao;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static com.careerconnect.security.AuthHelper.*;

/** The admin manages job seekers and recruiters. */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final QueryDao dao;

    @GetMapping("/stats")
    public Map<String, Object> stats(HttpSession s) {
        require(s, ADMIN);
        return dao.adminStats();
    }

    @GetMapping("/users")
    public List<Map<String, Object>> users(HttpSession s) {
        require(s, ADMIN);
        return dao.allSeekers();
    }

    @DeleteMapping("/users/{id}")
    public Map<String, Object> deleteUser(@PathVariable Long id, HttpSession s) {
        require(s, ADMIN);
        if (dao.deleteSeeker(id) == 0) throw notFound("Job seeker not found");
        return Map.of("message", "Job seeker removed");
    }

    @GetMapping("/companies")
    public List<Map<String, Object>> companies(HttpSession s) {
        require(s, ADMIN);
        return dao.allCompanies();
    }

    @DeleteMapping("/companies/{id}")
    public Map<String, Object> deleteCompany(@PathVariable Long id, HttpSession s) {
        require(s, ADMIN);
        if (dao.deleteCompany(id) == 0) throw notFound("Recruiter not found");
        return Map.of("message", "Recruiter removed");
    }
}
