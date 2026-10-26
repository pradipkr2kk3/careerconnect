package com.careerconnect.controller;

import com.careerconnect.dao.QueryDao;
import com.careerconnect.entity.*;
import com.careerconnect.repository.*;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static com.careerconnect.security.AuthHelper.*;

/** Everything a logged-in job seeker can do: dashboard, education, skills, experience, jobs, applications. */
@RestController
@RequestMapping("/api/seeker")
@RequiredArgsConstructor
public class SeekerController {

    private final QueryDao dao;
    private final EducationRepository education;
    private final SkillRepository skills;
    private final UserSkillRepository userSkills;
    private final ExperienceRepository experience;
    private final CompanyRepository companies;
    private final JobRepository jobs;
    private final JobApplicationRepository applications;

    @GetMapping("/dashboard")
    public Map<String, Object> dashboard(HttpSession s) {
        return dao.seekerDashboard(require(s, SEEKER));
    }

    // ---------- education ----------
    @GetMapping("/education")
    public List<Education> listEducation(HttpSession s) {
        return education.findByUserIdOrderByEndYearDesc(require(s, SEEKER));
    }

    @PostMapping("/education")
    public Education addEducation(@RequestBody Education body, HttpSession s) {
        Long uid = require(s, SEEKER);
        need(body.getInstituteName(), "Institute name");
        body.setEducationId(null);
        body.setUserId(uid);
        return education.save(body);
    }

    @DeleteMapping("/education/{id}")
    public Map<String, Object> deleteEducation(@PathVariable Long id, HttpSession s) {
        Long uid = require(s, SEEKER);
        Education e = education.findById(id).orElseThrow(() -> notFound("Education not found"));
        if (!e.getUserId().equals(uid)) throw forbidden("Not your record");
        education.delete(e);
        return Map.of("message", "Deleted");
    }

    // ---------- skills ----------
    @GetMapping("/skills")
    public List<Skill> skillCatalog(HttpSession s) {
        require(s, SEEKER);
        return skills.findAll();
    }

    @PostMapping("/skills")
    public Skill addSkillToCatalog(@RequestBody Skill body, HttpSession s) {
        require(s, SEEKER);
        need(body.getSkillName(), "Skill name");
        if (skills.existsBySkillNameIgnoreCase(body.getSkillName().trim())) throw conflict("This skill already exists");
        body.setSkillId(null);
        body.setSkillName(body.getSkillName().trim());
        return skills.save(body);
    }

    @GetMapping("/my-skills")
    public List<Map<String, Object>> mySkills(HttpSession s) {
        return dao.mySkills(require(s, SEEKER));
    }

    @PostMapping("/my-skills")
    public Map<String, Object> addMySkill(@RequestBody UserSkill body, HttpSession s) {
        Long uid = require(s, SEEKER);
        if (body.getSkillId() == null || !skills.existsById(body.getSkillId())) throw bad("Choose a skill");
        body.setUserId(uid);
        userSkills.save(body);                        // same skill again = update level / years
        return Map.of("message", "Skill saved");
    }

    @DeleteMapping("/my-skills/{skillId}")
    public Map<String, Object> removeMySkill(@PathVariable Long skillId, HttpSession s) {
        Long uid = require(s, SEEKER);
        userSkills.deleteById(new Ids.UserSkillKey(uid, skillId));
        return Map.of("message", "Removed");
    }

    // ---------- experience ----------
    @GetMapping("/companies")
    public List<Map<String, Object>> companyList(HttpSession s) {
        require(s, SEEKER);
        return dao.companiesForDropdown();
    }

    @GetMapping("/experience")
    public List<Experience> listExperience(HttpSession s) {
        return experience.findByUserIdOrderByJoiningDateDesc(require(s, SEEKER));
    }

    @PostMapping("/experience")
    public Experience addExperience(@RequestBody Experience body, HttpSession s) {
        Long uid = require(s, SEEKER);
        need(body.getDesignation(), "Designation");
        body.setExperienceId(null);
        body.setUserId(uid);
        if (body.getCompanyId() != null) {
            Company c = companies.findById(body.getCompanyId()).orElse(null);
            if (c != null) body.setCompanyName(c.getCompanyName());
            else body.setCompanyId(null);
        }
        need(body.getCompanyName(), "Company");
        return experience.save(body);
    }

    @DeleteMapping("/experience/{id}")
    public Map<String, Object> deleteExperience(@PathVariable Long id, HttpSession s) {
        Long uid = require(s, SEEKER);
        Experience e = experience.findById(id).orElseThrow(() -> notFound("Experience not found"));
        if (!e.getUserId().equals(uid)) throw forbidden("Not your record");
        experience.delete(e);
        return Map.of("message", "Deleted");
    }

    // ---------- jobs & applications ----------
    @GetMapping("/jobs")
    public List<Map<String, Object>> searchJobs(@RequestParam(defaultValue = "") String q, HttpSession s) {
        return dao.searchJobs(require(s, SEEKER), q);
    }

    @PostMapping("/jobs/{jobId}/apply")
    public Map<String, Object> apply(@PathVariable Long jobId, HttpSession s) {
        Long uid = require(s, SEEKER);
        if (!jobs.existsById(jobId)) throw notFound("Job not found");
        if (applications.existsByUserIdAndJobId(uid, jobId)) throw conflict("You have already applied to this job");
        JobApplication a = new JobApplication();
        a.setUserId(uid);
        a.setJobId(jobId);
        a.setApplicationDate(LocalDate.now());
        a.setApplicationStatus("APPLIED");
        applications.save(a);
        return Map.of("message", "Application sent");
    }

    @GetMapping("/applications")
    public List<Map<String, Object>> myApplications(HttpSession s) {
        return dao.myApplications(require(s, SEEKER));
    }
}
