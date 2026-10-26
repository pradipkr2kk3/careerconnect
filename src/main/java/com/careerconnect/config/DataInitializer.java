package com.careerconnect.config;

import com.careerconnect.entity.Skill;
import com.careerconnect.entity.User;
import com.careerconnect.repository.SkillRepository;
import com.careerconnect.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/** Runs on startup: creates the first admin account and a starter list of skills. */
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    public static final String ADMIN_EMAIL = "admin@careerconnect.com";
    public static final String ADMIN_PASSWORD = "Admin@123";

    private final UserRepository users;
    private final SkillRepository skills;

    @Override
    public void run(String... args) {
        if (!users.existsByEmail(ADMIN_EMAIL)) {
            User admin = new User();
            admin.setUserName("Administrator");
            admin.setEmail(ADMIN_EMAIL);
            admin.setPassword(new BCryptPasswordEncoder().encode(ADMIN_PASSWORD));
            admin.setAddress("Head office");
            admin.setRole("ADMIN");
            users.save(admin);
            System.out.println(">>> Admin created: " + ADMIN_EMAIL + " / " + ADMIN_PASSWORD);
        }
        if (skills.count() == 0) {
            String[][] seed = {
                {"Java", "Programming"}, {"Spring Boot", "Programming"}, {"Python", "Programming"},
                {"JavaScript", "Programming"}, {"SQL", "Database"}, {"PostgreSQL", "Database"},
                {"HTML & CSS", "Web"}, {"React", "Web"}, {"Data Analysis", "Analytics"},
                {"Communication", "Soft skill"}, {"Project Management", "Management"}, {"Teamwork", "Soft skill"}
            };
            for (String[] s : seed) {
                Skill skill = new Skill();
                skill.setSkillName(s[0]);
                skill.setSkillCategory(s[1]);
                skills.save(skill);
            }
        }
    }
}
