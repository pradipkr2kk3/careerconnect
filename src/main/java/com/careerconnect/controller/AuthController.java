package com.careerconnect.controller;

import com.careerconnect.entity.Company;
import com.careerconnect.entity.User;
import com.careerconnect.repository.CompanyRepository;
import com.careerconnect.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

import static com.careerconnect.security.AuthHelper.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository users;
    private final CompanyRepository companies;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    /** Job seeker registration. */
    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody User body, HttpSession session) {
        need(body.getUserName(), "Name");
        need(body.getEmail(), "Email");
        need(body.getPassword(), "Password");
        if (body.getPassword().length() < 6) throw bad("Password must be at least 6 characters");
        String email = body.getEmail().trim().toLowerCase();
        if (users.existsByEmail(email)) throw conflict("This email is already registered");

        body.setUserId(null);
        body.setEmail(email);
        body.setRole(SEEKER);                                   // nobody can self-register as admin
        body.setPassword(encoder.encode(body.getPassword()));
        User saved = users.save(body);
        login(session, SEEKER, saved.getUserId(), saved.getUserName());
        return Map.of("role", SEEKER, "name", saved.getUserName());
    }

    /** Login for job seekers and the admin (both live in the users table). */
    @PostMapping("/login")
    public Map<String, Object> loginUser(@RequestBody Map<String, String> body, HttpSession session) {
        String email = body.getOrDefault("email", "").trim().toLowerCase();
        User u = users.findByEmail(email).orElse(null);
        if (u == null || !encoder.matches(body.getOrDefault("password", ""), u.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Wrong email or password");
        }
        login(session, u.getRole(), u.getUserId(), u.getUserName());
        return Map.of("role", u.getRole(), "name", u.getUserName());
    }

    /** Recruiter (company) registration. */
    @PostMapping("/company/register")
    public Map<String, Object> registerCompany(@RequestBody Company body, HttpSession session) {
        need(body.getCompanyName(), "Company name");
        need(body.getCompanyEmail(), "Company email");
        need(body.getPassword(), "Password");
        if (body.getPassword().length() < 6) throw bad("Password must be at least 6 characters");
        String email = body.getCompanyEmail().trim().toLowerCase();
        if (companies.existsByCompanyEmail(email)) throw conflict("This company email is already registered");

        body.setCompanyId(null);
        body.setCompanyEmail(email);
        body.setPassword(encoder.encode(body.getPassword()));
        Company saved = companies.save(body);
        login(session, RECRUITER, saved.getCompanyId(), saved.getCompanyName());
        return Map.of("role", RECRUITER, "name", saved.getCompanyName());
    }

    @PostMapping("/company/login")
    public Map<String, Object> loginCompany(@RequestBody Map<String, String> body, HttpSession session) {
        String email = body.getOrDefault("email", "").trim().toLowerCase();
        Company c = companies.findByCompanyEmail(email).orElse(null);
        if (c == null || !encoder.matches(body.getOrDefault("password", ""), c.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Wrong email or password");
        }
        login(session, RECRUITER, c.getCompanyId(), c.getCompanyName());
        return Map.of("role", RECRUITER, "name", c.getCompanyName());
    }

    @GetMapping("/me")
    public Map<String, Object> me(HttpSession session) {
        Object role = session.getAttribute("role");
        if (role == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please log in first");
        return Map.of("role", role, "name", String.valueOf(session.getAttribute("name")));
    }

    @PostMapping("/logout")
    public Map<String, Object> logout(HttpSession session) {
        session.invalidate();
        return Map.of("message", "Logged out");
    }
}
