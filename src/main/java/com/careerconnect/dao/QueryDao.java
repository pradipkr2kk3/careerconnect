package com.careerconnect.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/** Read queries that join several tables. Column aliases are camelCase so the JSON matches the frontend. */
@Repository
@RequiredArgsConstructor
public class QueryDao {

    private final JdbcTemplate jdbc;

    // ---------------- job seeker ----------------
    public Map<String, Object> seekerDashboard(Long userId) {
        return jdbc.queryForMap("""
            SELECT u.user_name AS "name", u.email AS "email", u.address AS "address",
              (SELECT COUNT(*) FROM education WHERE user_id = u.user_id)       AS "education",
              (SELECT COUNT(*) FROM user_skill WHERE user_id = u.user_id)      AS "skills",
              (SELECT COUNT(*) FROM experience WHERE user_id = u.user_id)      AS "experience",
              (SELECT COUNT(*) FROM job_application WHERE user_id = u.user_id) AS "applications",
              (SELECT COUNT(*) FROM post WHERE user_id = u.user_id)            AS "posts"
            FROM users u WHERE u.user_id = ?
            """, userId);
    }

    public List<Map<String, Object>> mySkills(Long userId) {
        return jdbc.queryForList("""
            SELECT s.skill_id AS "skillId", s.skill_name AS "skillName", s.skill_category AS "skillCategory",
                   us.proficiency_level AS "proficiencyLevel", us.year_of_experience AS "yearOfExperience"
            FROM user_skill us JOIN skill s ON s.skill_id = us.skill_id
            WHERE us.user_id = ? ORDER BY s.skill_name
            """, userId);
    }

    public List<Map<String, Object>> searchJobs(Long userId, String q) {
        String like = "%" + (q == null ? "" : q.trim().toLowerCase()) + "%";
        return jdbc.queryForList("""
            SELECT j.job_id AS "jobId", j.job_title AS "jobTitle", j.job_role AS "jobRole",
                   j.job_description AS "jobDescription", j.salary AS "salary",
                   j.job_post_date AS "jobPostDate", j.skill_required AS "skillRequired",
                   c.company_id AS "companyId", c.company_name AS "companyName",
                   c.company_location AS "companyLocation",
                   EXISTS (SELECT 1 FROM job_application a WHERE a.job_id = j.job_id AND a.user_id = ?) AS "applied"
            FROM job j JOIN company c ON c.company_id = j.company_id
            WHERE LOWER(j.job_title || ' ' || COALESCE(j.skill_required, '') || ' ' || c.company_name
                        || ' ' || COALESCE(c.company_location, '')) LIKE ?
            ORDER BY j.job_post_date DESC, j.job_id DESC
            """, userId, like);
    }

    public List<Map<String, Object>> myApplications(Long userId) {
        return jdbc.queryForList("""
            SELECT a.application_id AS "applicationId", a.application_date AS "applicationDate",
                   a.application_status AS "applicationStatus", j.job_title AS "jobTitle",
                   c.company_name AS "companyName"
            FROM job_application a
            JOIN job j ON j.job_id = a.job_id
            JOIN company c ON c.company_id = j.company_id
            WHERE a.user_id = ? ORDER BY a.application_date DESC, a.application_id DESC
            """, userId);
    }

    public List<Map<String, Object>> companiesForDropdown() {
        return jdbc.queryForList("""
            SELECT company_id AS "companyId", company_name AS "companyName" FROM company ORDER BY company_name
            """);
    }

    // ---------------- feed ----------------
    public List<Map<String, Object>> feed(Long userId) {
        return jdbc.queryForList("""
            SELECT p.post_id AS "postId", p.post_content AS "postContent", p.post_date AS "postDate",
                   p.location AS "location", u.user_name AS "userName",
                   (SELECT COUNT(*) FROM likes l WHERE l.post_id = p.post_id AND l.like_status)  AS "likes",
                   (SELECT COUNT(*) FROM comments c WHERE c.post_id = p.post_id)                 AS "comments",
                   (SELECT COUNT(*) FROM share s WHERE s.post_id = p.post_id)                    AS "shares",
                   EXISTS (SELECT 1 FROM likes l WHERE l.post_id = p.post_id AND l.user_id = ? AND l.like_status) AS "likedByMe"
            FROM post p JOIN users u ON u.user_id = p.user_id
            ORDER BY p.post_date DESC LIMIT 50
            """, userId);
    }

    public List<Map<String, Object>> comments(Long postId) {
        return jdbc.queryForList("""
            SELECT c.comment_id AS "commentId", c.comment_content AS "commentContent",
                   c.comment_time AS "commentTime", u.user_name AS "userName"
            FROM comments c JOIN users u ON u.user_id = c.user_id
            WHERE c.post_id = ? ORDER BY c.comment_time
            """, postId);
    }

    public long likeCount(Long postId) {
        Long n = jdbc.queryForObject("SELECT COUNT(*) FROM likes WHERE post_id = ? AND like_status", Long.class, postId);
        return n == null ? 0 : n;
    }

    // ---------------- recruiter ----------------
    public Map<String, Object> recruiterDashboard(Long companyId) {
        return jdbc.queryForMap("""
            SELECT c.company_name AS "name", c.company_location AS "location", c.company_email AS "email",
              (SELECT COUNT(*) FROM job WHERE company_id = c.company_id) AS "jobs",
              (SELECT COUNT(*) FROM job_application a JOIN job j ON j.job_id = a.job_id
                 WHERE j.company_id = c.company_id) AS "applications",
              (SELECT COUNT(*) FROM job_application a JOIN job j ON j.job_id = a.job_id
                 WHERE j.company_id = c.company_id AND a.application_status = 'SHORTLISTED') AS "shortlisted",
              (SELECT COUNT(*) FROM job_application a JOIN job j ON j.job_id = a.job_id
                 WHERE j.company_id = c.company_id AND a.application_status = 'HIRED') AS "hired"
            FROM company c WHERE c.company_id = ?
            """, companyId);
    }

    public List<Map<String, Object>> companyJobs(Long companyId) {
        return jdbc.queryForList("""
            SELECT j.job_id AS "jobId", j.job_title AS "jobTitle", j.job_role AS "jobRole", j.salary AS "salary",
                   j.job_post_date AS "jobPostDate", j.skill_required AS "skillRequired",
                   (SELECT COUNT(*) FROM job_application a WHERE a.job_id = j.job_id) AS "applicants"
            FROM job j WHERE j.company_id = ? ORDER BY j.job_post_date DESC, j.job_id DESC
            """, companyId);
    }

    public List<Map<String, Object>> companyApplications(Long companyId) {
        return jdbc.queryForList("""
            SELECT a.application_id AS "applicationId", a.application_date AS "applicationDate",
                   a.application_status AS "applicationStatus", j.job_title AS "jobTitle",
                   u.user_name AS "userName", u.email AS "email", u.address AS "address",
                   (SELECT STRING_AGG(s.skill_name, ', ' ORDER BY s.skill_name)
                      FROM user_skill us JOIN skill s ON s.skill_id = us.skill_id
                     WHERE us.user_id = u.user_id) AS "skills"
            FROM job_application a
            JOIN job j ON j.job_id = a.job_id
            JOIN users u ON u.user_id = a.user_id
            WHERE j.company_id = ?
            ORDER BY a.application_date DESC, a.application_id DESC
            """, companyId);
    }

    public int updateApplicationStatus(Long companyId, Long applicationId, String status) {
        return jdbc.update("""
            UPDATE job_application SET application_status = ?
            WHERE application_id = ? AND job_id IN (SELECT job_id FROM job WHERE company_id = ?)
            """, status, applicationId, companyId);
    }

    // ---------------- admin ----------------
    public Map<String, Object> adminStats() {
        return jdbc.queryForMap("""
            SELECT (SELECT COUNT(*) FROM users WHERE role = 'JOB_SEEKER') AS "jobSeekers",
                   (SELECT COUNT(*) FROM company)         AS "recruiters",
                   (SELECT COUNT(*) FROM job)             AS "jobs",
                   (SELECT COUNT(*) FROM job_application) AS "applications",
                   (SELECT COUNT(*) FROM post)            AS "posts"
            """);
    }

    public List<Map<String, Object>> allSeekers() {
        return jdbc.queryForList("""
            SELECT user_id AS "userId", user_name AS "userName", email AS "email", address AS "address"
            FROM users WHERE role = 'JOB_SEEKER' ORDER BY user_id DESC
            """);
    }

    public List<Map<String, Object>> allCompanies() {
        return jdbc.queryForList("""
            SELECT c.company_id AS "companyId", c.company_name AS "companyName", c.company_type AS "companyType",
                   c.company_location AS "companyLocation", c.company_email AS "companyEmail",
                   c.company_contact AS "companyContact",
                   (SELECT COUNT(*) FROM job j WHERE j.company_id = c.company_id) AS "jobs"
            FROM company c ORDER BY c.company_id DESC
            """);
    }

    public int deleteSeeker(Long userId) {
        return jdbc.update("DELETE FROM users WHERE user_id = ? AND role = 'JOB_SEEKER'", userId);
    }

    public int deleteCompany(Long companyId) {
        return jdbc.update("DELETE FROM company WHERE company_id = ?", companyId);
    }
}
