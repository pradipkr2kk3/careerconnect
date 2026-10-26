-- CareerConnect: 12 tables from the relational model

CREATE TABLE IF NOT EXISTS users (
    user_id     BIGSERIAL PRIMARY KEY,
    user_name   VARCHAR(100) NOT NULL,
    email       VARCHAR(150) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    address     VARCHAR(255),
    role        VARCHAR(20)  NOT NULL DEFAULT 'JOB_SEEKER'   -- JOB_SEEKER or ADMIN
);

CREATE TABLE IF NOT EXISTS company (
    company_id       BIGSERIAL PRIMARY KEY,
    company_name     VARCHAR(150) NOT NULL,
    company_type     VARCHAR(100),
    company_location VARCHAR(150),
    company_email    VARCHAR(150) NOT NULL UNIQUE,
    company_contact  VARCHAR(30),
    password         VARCHAR(255) NOT NULL                   -- recruiter login
);

CREATE TABLE IF NOT EXISTS education (
    education_id   BIGSERIAL PRIMARY KEY,
    user_id        BIGINT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    institute_name VARCHAR(150) NOT NULL,
    degree         VARCHAR(100),
    start_year     INT,
    end_year       INT,
    grade          VARCHAR(30)
);

CREATE TABLE IF NOT EXISTS skill (
    skill_id       BIGSERIAL PRIMARY KEY,
    skill_name     VARCHAR(100) NOT NULL UNIQUE,
    skill_category VARCHAR(100),
    description    VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS user_skill (
    user_id            BIGINT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    skill_id           BIGINT NOT NULL REFERENCES skill(skill_id) ON DELETE CASCADE,
    proficiency_level  VARCHAR(30),
    year_of_experience INT,
    PRIMARY KEY (user_id, skill_id)
);

CREATE TABLE IF NOT EXISTS experience (
    experience_id    BIGSERIAL PRIMARY KEY,
    user_id          BIGINT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    company_id       BIGINT REFERENCES company(company_id) ON DELETE SET NULL,
    company_name     VARCHAR(150),
    designation      VARCHAR(100) NOT NULL,
    role_description TEXT,
    joining_date     DATE,
    end_date         DATE
);

CREATE TABLE IF NOT EXISTS post (
    post_id      BIGSERIAL PRIMARY KEY,
    user_id      BIGINT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    post_content TEXT NOT NULL,
    post_date    TIMESTAMP NOT NULL DEFAULT NOW(),
    location     VARCHAR(150)
);

CREATE TABLE IF NOT EXISTS job (
    job_id          BIGSERIAL PRIMARY KEY,
    company_id      BIGINT NOT NULL REFERENCES company(company_id) ON DELETE CASCADE,
    job_title       VARCHAR(150) NOT NULL,
    job_role        VARCHAR(100),
    job_description TEXT,
    salary          NUMERIC(12,2),
    job_post_date   DATE NOT NULL DEFAULT CURRENT_DATE,
    skill_required  VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS job_application (
    application_id     BIGSERIAL PRIMARY KEY,
    user_id            BIGINT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    job_id             BIGINT NOT NULL REFERENCES job(job_id) ON DELETE CASCADE,
    application_date   DATE NOT NULL DEFAULT CURRENT_DATE,
    application_status VARCHAR(30) NOT NULL DEFAULT 'APPLIED',
    UNIQUE (user_id, job_id)
);

CREATE TABLE IF NOT EXISTS likes (
    user_id     BIGINT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    post_id     BIGINT NOT NULL REFERENCES post(post_id) ON DELETE CASCADE,
    like_status BOOLEAN NOT NULL DEFAULT TRUE,
    liked_time  TIMESTAMP,
    like_count  INT NOT NULL DEFAULT 1,
    PRIMARY KEY (user_id, post_id)
);

CREATE TABLE IF NOT EXISTS comments (
    comment_id      BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    post_id         BIGINT NOT NULL REFERENCES post(post_id) ON DELETE CASCADE,
    comment_content TEXT NOT NULL,
    comment_time    TIMESTAMP NOT NULL DEFAULT NOW(),
    comment_count   INT NOT NULL DEFAULT 1
);

CREATE TABLE IF NOT EXISTS share (
    user_id       BIGINT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    post_id       BIGINT NOT NULL REFERENCES post(post_id) ON DELETE CASCADE,
    share_content TEXT,
    share_time    TIMESTAMP,
    share_count   INT NOT NULL DEFAULT 1,
    PRIMARY KEY (user_id, post_id)
);
