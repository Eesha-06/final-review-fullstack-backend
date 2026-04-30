-- ============================================================
-- LMS DATABASE SCHEMA + SAMPLE DATA
-- HOW TO USE:
--   1. Open MySQL Workbench
--   2. File → Open SQL Script → select this file
--   3. Click the lightning bolt ⚡ to run
-- ============================================================

-- Create and select the database
CREATE DATABASE IF NOT EXISTS lms_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE lms_db;

-- ============================================================
-- TABLE: roles
-- ============================================================
CREATE TABLE IF NOT EXISTS roles (
    id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
    -- values: ADMIN, INSTRUCTOR, STUDENT, CONTENT_CREATOR
);

-- ============================================================
-- TABLE: users
-- ============================================================
CREATE TABLE IF NOT EXISTS users (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    email       VARCHAR(150) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    role_id     BIGINT NOT NULL,
    bio         TEXT,
    profile_pic VARCHAR(255),
    is_active   BOOLEAN DEFAULT TRUE,
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (role_id) REFERENCES roles(id)
);

-- ============================================================
-- TABLE: categories
-- ============================================================
CREATE TABLE IF NOT EXISTS categories (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL UNIQUE,
    description TEXT
);

-- ============================================================
-- TABLE: courses
-- ============================================================
CREATE TABLE IF NOT EXISTS courses (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    title         VARCHAR(200) NOT NULL,
    description   TEXT,
    instructor_id BIGINT NOT NULL,
    category_id   BIGINT,
    thumbnail     VARCHAR(255),
    price         DECIMAL(10,2) DEFAULT 0.00,
    level         ENUM('BEGINNER','INTERMEDIATE','ADVANCED') DEFAULT 'BEGINNER',
    status        ENUM('DRAFT','PUBLISHED','ARCHIVED') DEFAULT 'DRAFT',
    created_at    DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (instructor_id) REFERENCES users(id),
    FOREIGN KEY (category_id)   REFERENCES categories(id)
);

-- ============================================================
-- TABLE: course_contents  (lessons inside a course)
-- ============================================================
CREATE TABLE IF NOT EXISTS course_contents (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    course_id    BIGINT NOT NULL,
    title        VARCHAR(200) NOT NULL,
    content_type ENUM('VIDEO','PDF','TEXT','LINK') DEFAULT 'TEXT',
    content_url  VARCHAR(500),
    content_text LONGTEXT,
    order_index  INT DEFAULT 0,
    duration_min INT DEFAULT 0,
    created_by   BIGINT,
    created_at   DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (course_id)  REFERENCES courses(id) ON DELETE CASCADE,
    FOREIGN KEY (created_by) REFERENCES users(id)
);

-- ============================================================
-- TABLE: enrollments  (which student is in which course)
-- ============================================================
CREATE TABLE IF NOT EXISTS enrollments (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id   BIGINT NOT NULL,
    course_id    BIGINT NOT NULL,
    enrolled_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
    completed    BOOLEAN DEFAULT FALSE,
    completed_at DATETIME,
    UNIQUE KEY uq_enrollment (student_id, course_id),
    FOREIGN KEY (student_id) REFERENCES users(id),
    FOREIGN KEY (course_id)  REFERENCES courses(id) ON DELETE CASCADE
);

-- ============================================================
-- TABLE: assignments
-- ============================================================
CREATE TABLE IF NOT EXISTS assignments (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    course_id   BIGINT NOT NULL,
    title       VARCHAR(200) NOT NULL,
    description TEXT,
    due_date    DATETIME,
    max_score   INT DEFAULT 100,
    created_by  BIGINT,
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (course_id)  REFERENCES courses(id) ON DELETE CASCADE,
    FOREIGN KEY (created_by) REFERENCES users(id)
);

-- ============================================================
-- TABLE: submissions  (student's answer to an assignment)
-- ============================================================
CREATE TABLE IF NOT EXISTS submissions (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    assignment_id   BIGINT NOT NULL,
    student_id      BIGINT NOT NULL,
    submission_text TEXT,
    file_url        VARCHAR(500),
    submitted_at    DATETIME DEFAULT CURRENT_TIMESTAMP,
    score           INT,
    feedback        TEXT,
    graded_at       DATETIME,
    graded_by       BIGINT,
    UNIQUE KEY uq_submission (assignment_id, student_id),
    FOREIGN KEY (assignment_id) REFERENCES assignments(id) ON DELETE CASCADE,
    FOREIGN KEY (student_id)    REFERENCES users(id),
    FOREIGN KEY (graded_by)     REFERENCES users(id)
);

-- ============================================================
-- TABLE: announcements
-- ============================================================
CREATE TABLE IF NOT EXISTS announcements (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    course_id  BIGINT NOT NULL,
    author_id  BIGINT NOT NULL,
    title      VARCHAR(200) NOT NULL,
    message    TEXT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
    FOREIGN KEY (author_id) REFERENCES users(id)
);

-- ============================================================
-- TABLE: progress  (tracks which lessons a student completed)
-- ============================================================
CREATE TABLE IF NOT EXISTS progress (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL,
    content_id BIGINT NOT NULL,
    course_id  BIGINT NOT NULL,
    completed  BOOLEAN DEFAULT FALSE,
    viewed_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_progress (student_id, content_id),
    FOREIGN KEY (student_id) REFERENCES users(id),
    FOREIGN KEY (content_id) REFERENCES course_contents(id) ON DELETE CASCADE,
    FOREIGN KEY (course_id)  REFERENCES courses(id) ON DELETE CASCADE
);

-- ============================================================
-- SAMPLE DATA
-- ============================================================

-- Roles
INSERT INTO roles (name) VALUES
('ADMIN'),
('INSTRUCTOR'),
('STUDENT'),
('CONTENT_CREATOR');

-- Users  (all passwords = "password123" hashed with BCrypt)
INSERT INTO users (name, email, password, role_id, is_active) VALUES
('Alice Admin',     'admin@lms.com',      '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 1, true),
('Ivan Instructor', 'instructor@lms.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 2, true),
('Sam Student',     'student@lms.com',    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 3, true),
('Cara Creator',    'creator@lms.com',    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 4, true),
('Bob Brown',       'bob@lms.com',        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 3, true),
('Jane Smith',      'jane@lms.com',       '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 2, true);

-- Categories
INSERT INTO categories (name, description) VALUES
('Web Development',  'HTML, CSS, JavaScript, React, Spring Boot'),
('Data Science',     'Python, Machine Learning, Data Analysis'),
('Mobile Dev',       'Android, iOS, Flutter'),
('Cloud & DevOps',   'AWS, Docker, Kubernetes'),
('Cyber Security',   'Ethical Hacking, Network Security');

-- Courses
INSERT INTO courses (title, description, instructor_id, category_id, level, status) VALUES
('Full Stack Web Development',
 'Learn HTML, CSS, JavaScript, React and Spring Boot from scratch. Build real projects.',
 2, 1, 'BEGINNER', 'PUBLISHED'),

('Python for Data Science',
 'Master Python, Pandas, NumPy, and Machine Learning fundamentals.',
 2, 2, 'INTERMEDIATE', 'PUBLISHED'),

('React Advanced Patterns',
 'Deep dive into React hooks, context, performance optimization, and testing.',
 6, 1, 'ADVANCED', 'PUBLISHED'),

('AWS Cloud Essentials',
 'Core AWS services including EC2, S3, RDS, Lambda, and cloud architecture.',
 6, 4, 'BEGINNER', 'PUBLISHED');

-- Course Contents (lessons)
INSERT INTO course_contents (course_id, title, content_type, content_text, order_index, duration_min, created_by) VALUES
-- Full Stack Web Dev
(1, 'Introduction to HTML',
 'TEXT',
 'HTML (HyperText Markup Language) is the standard markup language for web pages. In this lesson you will learn HTML tags, structure, headings, paragraphs, links, images and forms.',
 1, 20, 2),

(1, 'CSS Styling Fundamentals',
 'TEXT',
 'CSS (Cascading Style Sheets) controls the look and feel of your web pages. Learn selectors, colors, fonts, box model, flexbox and grid layout.',
 2, 25, 2),

(1, 'JavaScript Basics',
 'TEXT',
 'JavaScript is the programming language of the web. Learn variables, functions, loops, arrays, objects, DOM manipulation and event handling.',
 3, 30, 2),

(1, 'Introduction to React',
 'TEXT',
 'React is a JavaScript library for building user interfaces. Learn components, props, state, and JSX syntax.',
 4, 35, 2),

-- Python for Data Science
(2, 'Python Setup and Basics',
 'TEXT',
 'Install Python and set up your development environment. Learn variables, data types, loops, functions and file operations.',
 1, 20, 2),

(2, 'NumPy and Pandas',
 'TEXT',
 'NumPy provides powerful N-dimensional arrays. Pandas provides DataFrame and Series for data manipulation. Learn to load, clean, and analyze datasets.',
 2, 35, 2),

(2, 'Data Visualization',
 'TEXT',
 'Use Matplotlib and Seaborn to create line charts, bar charts, scatter plots, heatmaps and more.',
 3, 30, 2),

-- React Advanced
(3, 'React Hooks Deep Dive',
 'TEXT',
 'useState, useEffect, useContext, useRef, useMemo, useCallback — understand when and how to use each hook.',
 1, 40, 6),

(3, 'Context API and State Management',
 'TEXT',
 'Manage global state using React Context. Compare with Redux and Zustand.',
 2, 35, 6),

-- AWS
(4, 'AWS Core Services Overview',
 'TEXT',
 'Amazon Web Services offers 200+ services. This lesson covers the most important: EC2 (virtual servers), S3 (storage), RDS (databases), IAM (security), and Lambda (serverless).',
 1, 30, 6);

-- Enrollments (students enrolled in courses)
INSERT INTO enrollments (student_id, course_id) VALUES
(3, 1),  -- Sam enrolled in Full Stack
(3, 2),  -- Sam enrolled in Python
(5, 1),  -- Bob enrolled in Full Stack
(5, 3);  -- Bob enrolled in React Advanced

-- Assignments
INSERT INTO assignments (course_id, title, description, due_date, max_score, created_by) VALUES
(1, 'Build a Personal Portfolio Website',
 'Create a multi-page website using HTML and CSS. Must include Home, About, and Contact pages. Should be responsive.',
 DATE_ADD(NOW(), INTERVAL 7 DAY), 100, 2),

(1, 'JavaScript Calculator',
 'Build a fully working calculator using vanilla JavaScript. Must support +, -, *, / operations.',
 DATE_ADD(NOW(), INTERVAL 14 DAY), 100, 2),

(2, 'Exploratory Data Analysis',
 'Perform EDA on the Titanic dataset. Use Pandas for cleaning and Matplotlib for visualization. Submit a Jupyter notebook.',
 DATE_ADD(NOW(), INTERVAL 10 DAY), 100, 2),

(3, 'Custom Hook Implementation',
 'Create three custom React hooks: useLocalStorage, useFetch, and useDebounce. Write tests for each.',
 DATE_ADD(NOW(), INTERVAL 12 DAY), 100, 6);

-- Announcements
INSERT INTO announcements (course_id, author_id, title, message) VALUES
(1, 2,
 'Welcome to Full Stack Web Development!',
 'Hi everyone! Welcome to the course. Please complete the HTML module by end of this week. Feel free to post questions.'),

(2, 2,
 'First Assignment Released',
 'The EDA assignment on the Titanic dataset has been posted. Deadline is in 10 days. Use Google Colab if you do not have Jupyter installed.'),

(3, 6,
 'Office Hours This Friday',
 'I will be hosting live office hours this Friday at 3 PM IST via Google Meet. Link will be shared in the group.');

-- ============================================================
-- VERIFY DATA  (optional — run these to check)
-- ============================================================
-- SELECT * FROM roles;
-- SELECT u.name, r.name AS role FROM users u JOIN roles r ON u.role_id = r.id;
-- SELECT c.title, u.name AS instructor FROM courses c JOIN users u ON c.instructor_id = u.id;
-- SELECT * FROM enrollments;
-- SELECT * FROM assignments;
