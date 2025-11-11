-- =============================================
-- Database: gestion_scolarite
-- Student Management System SQL Schema
-- =============================================

CREATE DATABASE IF NOT EXISTS gestion_scolarite;
USE gestion_scolarite;

-- =============================================
-- Table: programs
-- =============================================
CREATE TABLE programs (
    program_id INT PRIMARY KEY AUTO_INCREMENT,
    program_name VARCHAR(100) NOT NULL,
    program_year INT NOT NULL,
    description TEXT,
    program_type ENUM('LICENCE', 'MASTER', 'DUT', 'INGENIEUR') DEFAULT 'LICENCE',
    total_credits INT DEFAULT 180,
    duration_years INT DEFAULT 3,
    department VARCHAR(100),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =============================================
-- Table: students
-- =============================================
CREATE TABLE students (
    student_id INT PRIMARY KEY AUTO_INCREMENT,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100),
    phone VARCHAR(20),
    school_origin VARCHAR(100) NOT NULL,
    program_id INT NULL,
    academic_year VARCHAR(20) NULL,
    current_year INT DEFAULT 1,
    current_semester INT DEFAULT 1,
    student_group VARCHAR(20) NULL,
    registration_status ENUM('Inscrit', 'En attente', 'Exclu', 'Redoublant', 'Admis') DEFAULT 'En attente',
    final_status ENUM('ADMIS', 'REDOUBLANT', 'EXCLU') NULL,
    total_credits_earned INT DEFAULT 0,
    registration_date DATE DEFAULT CURRENT_DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (program_id) REFERENCES programs(program_id)
);

-- =============================================
-- Table: student_programs
-- =============================================
CREATE TABLE student_programs (
    student_program_id INT PRIMARY KEY AUTO_INCREMENT,
    student_id INT NOT NULL,
    program_id INT NOT NULL,
    academic_year VARCHAR(20) NOT NULL,
    registration_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (student_id) REFERENCES students(student_id),
    FOREIGN KEY (program_id) REFERENCES programs(program_id),
    UNIQUE KEY unique_student_program_year (student_id, program_id, academic_year)
);

-- =============================================
-- Table: teachers
-- =============================================
CREATE TABLE teachers (
    teacher_id INT PRIMARY KEY AUTO_INCREMENT,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100),
    specialty VARCHAR(100),
    phone VARCHAR(20),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =============================================
-- Table: subjects
-- =============================================
CREATE TABLE subjects (
    subject_id INT PRIMARY KEY AUTO_INCREMENT,
    subject_name VARCHAR(100) NOT NULL,
    subject_code VARCHAR(20) UNIQUE,
    objectives TEXT,
    semester INT NOT NULL,
    coefficient DECIMAL(3,2) NOT NULL,
    credits INT DEFAULT 6,
    volume_horaire INT DEFAULT 60,
    program_id INT NOT NULL,
    is_optional BOOLEAN DEFAULT FALSE,
    prerequisite_id INT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (program_id) REFERENCES programs(program_id),
    FOREIGN KEY (prerequisite_id) REFERENCES subjects(subject_id)
);

-- =============================================
-- Table: teacher_subjects
-- =============================================
CREATE TABLE teacher_subjects (
    teacher_subject_id INT PRIMARY KEY AUTO_INCREMENT,
    teacher_id INT NOT NULL,
    subject_id INT NOT NULL,
    academic_year VARCHAR(20) DEFAULT '2024-2025',
    is_responsible BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (teacher_id) REFERENCES teachers(teacher_id),
    FOREIGN KEY (subject_id) REFERENCES subjects(subject_id),
    UNIQUE KEY unique_teacher_subject (teacher_id, subject_id, academic_year)
);

-- =============================================
-- Table: exams
-- =============================================
CREATE TABLE exams (
    exam_id INT PRIMARY KEY AUTO_INCREMENT,
    exam_name VARCHAR(100) NOT NULL,
    subject_id INT NOT NULL,
    exam_type ENUM('CONTROLE', 'EXAMEN', 'PROJET', 'TP') NOT NULL,
    coefficient DECIMAL(3,2) NOT NULL,
    teacher_id INT NOT NULL,
    max_score DECIMAL(4,2) DEFAULT 20.00,
    exam_date DATE,
    creation_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (subject_id) REFERENCES subjects(subject_id),
    FOREIGN KEY (teacher_id) REFERENCES teachers(teacher_id)
);

-- =============================================
-- Table: grades
-- =============================================
CREATE TABLE grades (
    grade_id INT PRIMARY KEY AUTO_INCREMENT,
    student_id INT NOT NULL,
    exam_id INT NOT NULL,
    score DECIMAL(4,2) NOT NULL,
    grade_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    comments TEXT,
    is_approved BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES students(student_id),
    FOREIGN KEY (exam_id) REFERENCES exams(exam_id),
    UNIQUE KEY unique_student_exam (student_id, exam_id)
);

-- =============================================
-- Table: academic_years
-- =============================================
CREATE TABLE academic_years (
    year_id INT PRIMARY KEY AUTO_INCREMENT,
    start_year INT NOT NULL,
    end_year INT NOT NULL,
    is_current BOOLEAN DEFAULT FALSE,
    description VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =============================================
-- Table: program_configuration
-- =============================================
CREATE TABLE program_configuration (
    config_id INT PRIMARY KEY AUTO_INCREMENT,
    program_id INT NOT NULL,
    has_controle BOOLEAN DEFAULT TRUE,
    has_tp BOOLEAN DEFAULT FALSE,
    has_project BOOLEAN DEFAULT FALSE,
    controle_weight DECIMAL(3,2) DEFAULT 0.4,
    tp_weight DECIMAL(3,2) DEFAULT 0.2,
    project_weight DECIMAL(3,2) DEFAULT 0.2,
    exam_weight DECIMAL(3,2) DEFAULT 0.6,
    min_pass_grade DECIMAL(4,2) DEFAULT 10.00,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (program_id) REFERENCES programs(program_id)
);

-- =============================================
-- Table: users
-- =============================================
CREATE TABLE users (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    email VARCHAR(100),
    user_type ENUM('STUDENT', 'TEACHER', 'ADMIN', 'RESPONSABLE') NOT NULL,
    student_id INT NULL,
    teacher_id INT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    last_login TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES students(student_id),
    FOREIGN KEY (teacher_id) REFERENCES teachers(teacher_id)
);

-- =============================================
-- Table: semesters
-- =============================================
CREATE TABLE semesters (
    semester_id INT PRIMARY KEY AUTO_INCREMENT,
    program_id INT NOT NULL,
    semester_number INT NOT NULL,
    semester_name VARCHAR(50),
    total_credits INT DEFAULT 30,
    start_date DATE,
    end_date DATE,
    is_current BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (program_id) REFERENCES programs(program_id)
);

-- =============================================
-- Table: registrations
-- =============================================
CREATE TABLE registrations (
    registration_id INT PRIMARY KEY AUTO_INCREMENT,
    student_id INT NOT NULL,
    program_id INT NOT NULL,
    year_id INT NOT NULL,
    semester_id INT NOT NULL,
    registration_date DATE,
    final_status ENUM('ADMIS', 'REDOUBLANT', 'EXCLU') NULL,
    overall_average DECIMAL(4,2) NULL,
    credits_earned INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES students(student_id),
    FOREIGN KEY (program_id) REFERENCES programs(program_id),
    FOREIGN KEY (year_id) REFERENCES academic_years(year_id),
    FOREIGN KEY (semester_id) REFERENCES semesters(semester_id)
);

-- =============================================
-- Table: attendance
-- =============================================
CREATE TABLE attendance (
    attendance_id INT PRIMARY KEY AUTO_INCREMENT,
    student_id INT NOT NULL,
    subject_id INT NOT NULL,
    session_date DATE NOT NULL,
    status ENUM('PRESENT', 'ABSENT', 'JUSTIFIED') NOT NULL,
    comments TEXT,
    recorded_by INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES students(student_id),
    FOREIGN KEY (subject_id) REFERENCES subjects(subject_id),
    FOREIGN KEY (recorded_by) REFERENCES teachers(teacher_id)
);

-- =============================================
-- Table: notifications
-- =============================================
CREATE TABLE notifications (
    notification_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    title VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,
    notification_type ENUM('INFO', 'WARNING', 'SUCCESS', 'ERROR') DEFAULT 'INFO',
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

-- =============================================
-- INSERT SAMPLE DATA
-- =============================================

-- Insert Academic Years
INSERT INTO academic_years (start_year, end_year, is_current, description) VALUES 
(2024, 2025, TRUE, 'Academic Year 2024-2025'),
(2023, 2024, FALSE, 'Academic Year 2023-2024'),
(2022, 2023, FALSE, 'Academic Year 2022-2023');

-- Insert Programs (including ISIL and SI)
INSERT INTO programs (program_name, program_year, description, program_type, total_credits, duration_years, department) VALUES 
('ING1 TC', 1, 'First Year Common Core', 'LICENCE', 180, 3, 'Computer Science'),
('ING2 GI', 2, 'Computer Engineering', 'LICENCE', 180, 3, 'Computer Science'),
('ING3 GL', 3, 'Software Engineering', 'LICENCE', 180, 3, 'Computer Science'),
('Master AI', 2, 'Master in Artificial Intelligence', 'MASTER', 120, 2, 'Computer Science'),
('DUT Info', 2, 'University Diploma in Computer Technology', 'DUT', 120, 2, 'Computer Science'),
('ISIL', 3, 'Software Engineering and Information Systems', 'LICENCE', 180, 3, 'Computer Science'),
('SI', 3, 'Information Systems', 'LICENCE', 180, 3, 'Computer Science');

-- Insert Program Configurations
INSERT INTO program_configuration (program_id, has_controle, has_tp, has_project) VALUES 
(1, TRUE, TRUE, FALSE),
(2, TRUE, TRUE, TRUE),
(3, TRUE, TRUE, TRUE),
(4, TRUE, TRUE, TRUE),
(5, TRUE, TRUE, FALSE),
(6, TRUE, TRUE, TRUE),
(7, TRUE, TRUE, TRUE);

-- Insert Semesters for all programs
INSERT INTO semesters (program_id, semester_number, semester_name, total_credits) VALUES 
-- ING1 TC (6 semesters)
(1, 1, 'S1', 30), (1, 2, 'S2', 30),
(1, 3, 'S3', 30), (1, 4, 'S4', 30),
(1, 5, 'S5', 30), (1, 6, 'S6', 30),
-- ING2 GI (6 semesters)
(2, 1, 'S1', 30), (2, 2, 'S2', 30),
(2, 3, 'S3', 30), (2, 4, 'S4', 30),
(2, 5, 'S5', 30), (2, 6, 'S6', 30),
-- ISIL (6 semesters)
(6, 1, 'S1', 30), (6, 2, 'S2', 30),
(6, 3, 'S3', 30), (6, 4, 'S4', 30),
(6, 5, 'S5', 30), (6, 6, 'S6', 30),
-- SI (6 semesters)
(7, 1, 'S1', 30), (7, 2, 'S2', 30),
(7, 3, 'S3', 30), (7, 4, 'S4', 30),
(7, 5, 'S5', 30), (7, 6, 'S6', 30);

-- Insert Teachers
INSERT INTO teachers (first_name, last_name, email, specialty, phone) VALUES 
('Dr. Karim', 'Benzema', 'karim.benzema@univ.edu', 'Computer Science', '+213-661-234-567'),
('Prof. Leila', 'Tunis', 'leila.tunis@univ.edu', 'Mathematics', '+213-662-234-567'),
('Dr. Ahmed', 'Mansouri', 'ahmed.mansouri@univ.edu', 'Networks and Telecommunications', '+213-663-234-567'),
('Prof. Fatima', 'Zohra', 'fatima.zohra@univ.edu', 'Database Systems', '+213-664-234-567'),
('Dr. Samir', 'Kadri', 'samir.kadri@univ.edu', 'Artificial Intelligence', '+213-665-234-567'),
('Mohamed', 'Moustfaoui', 'm.moustfaoui@univ.edu', 'Informatique', '+212-661-123456'),
('Fatima', 'Maatoug', 'f.maatoug@univ.edu', 'Informatique', '+212-662-234567'),
('Hassan', 'Maazouz', 'h.maazouz@univ.edu', 'Informatique', '+212-663-345678'),
('Amina', 'Mebarek', 'a.mebarek@univ.edu', 'Informatique', '+212-664-456789'),
('Karim', 'Bougassa', 'k.bougassa@univ.edu', 'Informatique', '+212-665-567890'),
('Nadia', 'Alami', 'n.alami@univ.edu', 'Informatique', '+212-666-678901'),
('Youssef', 'Bennani', 'y.bennani@univ.edu', 'Informatique', '+212-667-789012'),
('Samira', 'Chraibi', 's.chraibi@univ.edu', 'Informatique', '+212-668-890123'),
('Rachid', 'Dahmani', 'r.dahmani@univ.edu', 'Informatique', '+212-669-901234'),
('Leila', 'Elkouri', 'l.elkouri@univ.edu', 'Informatique', '+212-670-012345'),
('Ahmed', 'Farsi', 'a.farsi@univ.edu', 'Informatique', '+212-671-123450'),
('Zahra', 'Guessous', 'z.guessous@univ.edu', 'Informatique', '+212-672-234561'),
('Omar', 'Hassani', 'o.hassani@univ.edu', 'Informatique', '+212-673-345672');

-- Insert Subjects for ING1 TC
INSERT INTO subjects (subject_name, subject_code, objectives, semester, coefficient, credits, volume_horaire, program_id) VALUES 
('Java Programming', 'INF101', 'Master Object-Oriented Programming in Java', 1, 1.5, 6, 60, 1),
('Algorithms and Data Structures', 'INF102', 'Understand fundamental algorithms and data structures', 1, 1.5, 6, 60, 1),
('Mathematics for Computer Science', 'MATH101', 'Mathematical foundations for computer science', 1, 1.0, 4, 45, 1),
('Information Systems', 'INF103', 'Understand enterprise information systems', 2, 1.2, 5, 50, 1),
('Database Systems', 'INF104', 'Design and manipulate databases', 2, 1.5, 6, 60, 1),
('Networks and Telecommunications', 'INF105', 'Fundamentals of computer networks', 2, 1.3, 5, 55, 1);

-- Insert Subjects for ING2 GI
INSERT INTO subjects (subject_name, subject_code, objectives, semester, coefficient, credits, volume_horaire, program_id) VALUES 
('Advanced Web Development', 'INF201', 'Develop advanced web applications', 3, 1.5, 6, 60, 2),
('Software Engineering', 'INF202', 'Software development methodologies and practices', 3, 1.5, 6, 60, 2),
('Operating Systems', 'INF203', 'Study of operating system concepts and design', 3, 1.2, 5, 50, 2),
('Artificial Intelligence', 'INF204', 'Introduction to AI concepts and algorithms', 4, 1.5, 6, 60, 2),
('Computer Security', 'INF205', 'Principles of computer and network security', 4, 1.3, 5, 55, 2),
('Mobile Development', 'INF206', 'Develop mobile applications for iOS and Android', 4, 1.4, 6, 58, 2);

-- Insert Subjects for ISIL (Semester 1)
INSERT INTO subjects (subject_name, subject_code, semester, coefficient, credits, volume_horaire, program_id) VALUES 
('Interface Homme Machine', 'SI6-S1-IHM', 1, 4.00, 6, 60, 6),
('Programmation Linéaire', 'SI6-S1-PL', 1, 4.50, 6, 60, 6),
('Probabilités et Statistique', 'SI6-S1-PS', 1, 3.50, 6, 60, 6),
('Economie numérique et veille stratégique', 'SI6-S1-ENVS', 1, 3.00, 6, 60, 6),
('Génie Logiciel', 'SI6-S1-GL', 1, 3.50, 6, 60, 6);

-- Insert Subjects for ISIL (Semester 2)
INSERT INTO subjects (subject_name, subject_code, semester, coefficient, credits, volume_horaire, program_id) VALUES 
('Applications Mobiles', 'SI6-S2-AM', 2, 4.00, 6, 60, 6),
('Sécurité Informatique', 'SI6-S2-SI', 2, 4.50, 6, 60, 6),
('Intelligence Artificielle', 'SI6-S2-IA', 2, 5.00, 6, 60, 6),
('Données semi-structurées', 'SI6-S2-DSS', 2, 3.50, 6, 60, 6),
('Projet', 'SI6-S2-PRJ', 2, 4.00, 3, 60, 6),
('Créer et développer une startup', 'SI6-S2-STARTUP', 2, 3.00, 3, 60, 6);

-- Insert Subjects for SI (Semester 1)
INSERT INTO subjects (subject_name, subject_code, semester, coefficient, credits, volume_horaire, program_id) VALUES 
('Interface Homme Machine', 'SI7-S1-IHM', 1, 4.00, 4, 40, 7),
('Economie numérique et veille stratégique', 'SI7-S1-ENVS', 1, 3.50, 4, 40, 7),
('Systeme d information distribue', 'SI7-S1-SID', 1, 4.50, 5, 50, 7),
('Systeme d aide a la disicion', 'SI7-S1-SAD', 1, 4.00, 4, 40, 7),
('Administration des systeme d information', 'SI7-S1-ASI', 1, 4.50, 5, 50, 7),
('Programation avancee pour le web', 'SI7-S1-PAW', 1, 5.00, 4, 40, 7),
('Genie logiciel', 'SI7-S1-GL', 1, 4.00, 4, 40, 7);

-- Insert Subjects for SI (Semester 2)
INSERT INTO subjects (subject_name, subject_code, semester, coefficient, credits, volume_horaire, program_id) VALUES 
('Recherche d information', 'SI7-S2-RI', 2, 3.50, 4, 40, 7),
('Sécurité Informatique', 'SI7-S2-SEC', 2, 4.50, 5, 50, 7),
('Données semi structurées', 'SI7-S2-DSS', 2, 4.00, 4, 40, 7),
('Système d exploitation 2', 'SI7-S2-SE2', 2, 4.00, 4, 40, 7),
('Projet', 'SI7-S2-PRJ', 2, 4.50, 5, 50, 7),
('Business Intelligence', 'SI7-S2-BI', 2, 5.00, 4, 40, 7),
('Rédaction Scientifique', 'SI7-S2-RS', 2, 3.00, 4, 40, 7);

-- Insert Teacher-Subject Assignments
INSERT INTO teacher_subjects (teacher_id, subject_id, is_responsible) VALUES 
(1, 1, TRUE),  -- Karim teaches Java Programming
(1, 2, FALSE), -- Karim teaches Algorithms
(2, 3, TRUE),  -- Leila teaches Mathematics
(3, 6, TRUE),  -- Ahmed teaches Networks
(4, 5, TRUE),  -- Fatima teaches Database Systems
(4, 4, FALSE), -- Fatima teaches Information Systems
(5, 10, TRUE); -- Samir teaches AI

-- Insert Students
INSERT INTO students (first_name, last_name, email, phone, school_origin, program_id, academic_year, student_group, registration_status) VALUES 
('Ahmed', 'Mohamed', 'ahmed.mohamed@student.univ.edu', '0612345678', 'DUT', 1, '2024-2025', 'Group A', 'Inscrit'),
('Fatima', 'Zahra', 'fatima.zahra@student.univ.edu', '0622345678', 'CPGE', 1, '2024-2025', 'Group A', 'Inscrit'),
('Youssef', 'Ali', 'youssef.ali@student.univ.edu', '0632345678', 'CPI', 1, '2024-2025', 'Group B', 'Inscrit'),
('Amina', 'Khalil', 'amina.khalil@student.univ.edu', '0642345678', 'DUT', 2, '2024-2025', 'Group A', 'Inscrit'),
('Mehdi', 'Bouzid', 'mehdi.bouzid@student.univ.edu', '0652345678', 'CPGE', 2, '2024-2025', 'Group B', 'Inscrit'),
('Sara', 'Benali', 'sara.benali@student.univ.edu', '0662345678', 'DUT', 6, '2024-2025', 'ISIL Group A', 'Inscrit'),
('Omar', 'Khaldi', 'omar.khaldi@student.univ.edu', '0672345678', 'CPGE', 6, '2024-2025', 'ISIL Group A', 'Inscrit'),
('Lina', 'Mansouri', 'lina.mansouri@student.univ.edu', '0682345678', 'CPI', 7, '2024-2025', 'SI Group A', 'Inscrit'),
('Hakim', 'Zeroual', 'hakim.zeroual@student.univ.edu', '0692345678', 'DUT', 7, '2024-2025', 'SI Group B', 'Inscrit');

-- Insert Student Programs
INSERT INTO student_programs (student_id, program_id, academic_year) VALUES
(1, 6, '2024-2025'),
(2, 6, '2024-2025'),
(3, 7, '2024-2025');

-- Insert Exams
INSERT INTO exams (exam_name, subject_id, exam_type, coefficient, teacher_id, max_score, exam_date) VALUES 
('Java Final Exam', 1, 'EXAMEN', 0.6, 1, 20.00, '2024-12-15'),
('Java Control Test', 1, 'CONTROLE', 0.4, 1, 20.00, '2024-10-20'),
('Algorithms Project', 2, 'PROJET', 1.0, 1, 20.00, '2024-11-30'),
('Mathematics Exam', 3, 'EXAMEN', 1.0, 2, 20.00, '2024-12-20'),
('Database TP', 5, 'TP', 1.0, 4, 20.00, '2024-11-15'),
('Networks Final', 6, 'EXAMEN', 0.7, 3, 20.00, '2024-12-18'),
('Networks Control', 6, 'CONTROLE', 0.3, 3, 20.00, '2024-10-25');

-- Insert Grades
INSERT INTO grades (student_id, exam_id, score, comments) VALUES 
(1, 1, 16.5, 'Good performance'),
(1, 2, 14.0, 'Average'),
(1, 4, 15.5, 'Very good'),
(2, 1, 18.0, 'Excellent'),
(2, 2, 16.5, 'Very good'),
(2, 3, 17.5, 'Excellent project'),
(3, 1, 12.5, 'Needs improvement'),
(3, 2, 11.0, 'Below average'),
(4, 5, 15.0, 'Good practical work'),
(4, 6, 16.0, 'Very good'),
(5, 6, 19.0, 'Outstanding'),
(5, 7, 18.5, 'Excellent');

-- Insert Users with simple passwords
INSERT INTO users (username, password_hash, email, user_type, student_id, teacher_id) VALUES 
-- Admin user
('admin', 'password', 'admin@univ.edu', 'ADMIN', NULL, NULL),

-- Student users
('ahmed.mohamed', 'password', 'ahmed.mohamed@student.univ.edu', 'STUDENT', 1, NULL),
('fatima.zahra', 'password', 'fatima.zahra@student.univ.edu', 'STUDENT', 2, NULL),
('youssef.ali', 'password', 'youssef.ali@student.univ.edu', 'STUDENT', 3, NULL),
('amina.khalil', 'password', 'amina.khalil@student.univ.edu', 'STUDENT', 4, NULL),
('mehdi.bouzid', 'password', 'mehdi.bouzid@student.univ.edu', 'STUDENT', 5, NULL),
('sara.benali', 'password', 'sara.benali@student.univ.edu', 'STUDENT', 6, NULL),
('omar.khaldi', 'password', 'omar.khaldi@student.univ.edu', 'STUDENT', 7, NULL),
('lina.mansouri', 'password', 'lina.mansouri@student.univ.edu', 'STUDENT', 8, NULL),
('hakim.zeroual', 'password', 'hakim.zeroual@student.univ.edu', 'STUDENT', 9, NULL),

-- Teacher users
('prof.karim', 'password', 'karim.benzema@univ.edu', 'TEACHER', NULL, 1),
('prof.leila', 'password', 'leila.tunis@univ.edu', 'TEACHER', NULL, 2),
('prof.ahmed', 'password', 'ahmed.mansouri@univ.edu', 'TEACHER', NULL, 3),
('prof.fatima', 'password', 'fatima.zohra@univ.edu', 'TEACHER', NULL, 4),
('prof.samir', 'password', 'samir.kadri@univ.edu', 'TEACHER', NULL, 5),
('prof.moustfaoui', 'password', 'm.moustfaoui@univ.edu', 'TEACHER', NULL, 6),
('prof.maatoug', 'password', 'f.maatoug@univ.edu', 'TEACHER', NULL, 7),
('prof.maazouz', 'password', 'h.maazouz@univ.edu', 'TEACHER', NULL, 8),
('prof.mebarek', 'password', 'a.mebarek@univ.edu', 'TEACHER', NULL, 9),
('prof.bougassa', 'password', 'k.bougassa@univ.edu', 'TEACHER', NULL, 10),
('prof.alami', 'password', 'n.alami@univ.edu', 'TEACHER', NULL, 11),
('prof.bennani', 'password', 'y.bennani@univ.edu', 'TEACHER', NULL, 12),
('prof.chraibi', 'password', 's.chraibi@univ.edu', 'TEACHER', NULL, 13),
('prof.dahmani', 'password', 'r.dahmani@univ.edu', 'TEACHER', NULL, 14),
('prof.elkouri', 'password', 'l.elkouri@univ.edu', 'TEACHER', NULL, 15),
('prof.farsi', 'password', 'a.farsi@univ.edu', 'TEACHER', NULL, 16),
('prof.guessous', 'password', 'z.guessous@univ.edu', 'TEACHER', NULL, 17),
('prof.hassani', 'password', 'o.hassani@univ.edu', 'TEACHER', NULL, 18),

-- Responsable user
('resp', 'password', 'responsable@univ.edu', 'RESPONSABLE', NULL, NULL);

-- Insert Registrations
INSERT INTO registrations (student_id, program_id, year_id, semester_id, registration_date, final_status, overall_average, credits_earned) VALUES 
(1, 1, 1, 1, '2024-09-01', NULL, 15.33, 16),
(2, 1, 1, 1, '2024-09-01', NULL, 17.33, 16),
(3, 1, 1, 1, '2024-09-01', NULL, 11.75, 12),
(4, 2, 1, 3, '2024-09-01', NULL, 15.50, 30),
(5, 2, 1, 3, '2024-09-01', NULL, 18.75, 30),
(6, 6, 1, 5, '2024-09-01', NULL, 14.50, 120),
(7, 6, 1, 5, '2024-09-01', NULL, 16.25, 120),
(8, 7, 1, 5, '2024-09-01', NULL, 15.75, 120),
(9, 7, 1, 5, '2024-09-01', NULL, 17.00, 120);

-- Insert Sample Attendance
INSERT INTO attendance (student_id, subject_id, session_date, status, recorded_by) VALUES 
(1, 1, '2024-10-10', 'PRESENT', 1),
(1, 1, '2024-10-17', 'ABSENT', 1),
(2, 1, '2024-10-10', 'PRESENT', 1),
(2, 1, '2024-10-17', 'PRESENT', 1),
(3, 1, '2024-10-10', 'JUSTIFIED', 1);

-- Insert Sample Notifications
INSERT INTO notifications (user_id, title, message, notification_type) VALUES 
(2, 'New Grade Available', 'Your grade for Java Final Exam has been published', 'INFO'),
(3, 'Attendance Warning', 'You have multiple absences in Java Programming', 'WARNING'),
(6, 'New Assignment', 'You have been assigned to teach Database Systems', 'SUCCESS');

-- =============================================
-- CREATE VIEWS FOR REPORTING
-- =============================================

-- View: Student grades with details
CREATE VIEW student_grades_view AS
SELECT 
    s.student_id,
    s.first_name,
    s.last_name,
    sub.subject_name,
    e.exam_name,
    e.exam_type,
    g.score,
    g.grade_date,
    p.program_name
FROM students s
JOIN grades g ON s.student_id = g.student_id
JOIN exams e ON g.exam_id = e.exam_id
JOIN subjects sub ON e.subject_id = sub.subject_id
JOIN programs p ON s.program_id = p.program_id;

-- View: Teacher assignments
CREATE VIEW teacher_assignments_view AS
SELECT 
    t.teacher_id,
    t.first_name,
    t.last_name,
    t.specialty,
    s.subject_name,
    s.subject_code,
    p.program_name,
    ts.academic_year,
    ts.is_responsible
FROM teachers t
JOIN teacher_subjects ts ON t.teacher_id = ts.teacher_id
JOIN subjects s ON ts.subject_id = s.subject_id
JOIN programs p ON s.program_id = p.program_id;

-- View: Program subjects summary
CREATE VIEW program_subjects_view AS
SELECT 
    p.program_id,
    p.program_name,
    COUNT(s.subject_id) as total_subjects,
    SUM(s.credits) as total_credits,
    AVG(s.coefficient) as avg_coefficient
FROM programs p
LEFT JOIN subjects s ON p.program_id = s.program_id
GROUP BY p.program_id, p.program_name;

-- View: Student academic summary
CREATE VIEW student_academic_summary AS
SELECT 
    s.student_id,
    s.first_name,
    s.last_name,
    p.program_name,
    s.academic_year,
    s.current_semester,
    COUNT(g.grade_id) as exams_taken,
    AVG(g.score) as average_grade,
    SUM(sub.credits) as credits_earned,
    s.registration_status
FROM students s
LEFT JOIN programs p ON s.program_id = p.program_id
LEFT JOIN grades g ON s.student_id = g.student_id
LEFT JOIN exams e ON g.exam_id = e.exam_id
LEFT JOIN subjects sub ON e.subject_id = sub.subject_id
GROUP BY s.student_id, s.first_name, s.last_name, p.program_name, s.academic_year, s.current_semester, s.registration_status;

-- View: Student programs view
CREATE VIEW student_programs_view AS
SELECT 
    sp.student_program_id,
    s.student_id,
    s.first_name,
    s.last_name,
    p.program_id,
    p.program_name,
    sp.academic_year,
    sp.registration_date,
    sp.is_active
FROM student_programs sp
JOIN students s ON sp.student_id = s.student_id
JOIN programs p ON sp.program_id = p.program_id;

-- =============================================
-- CREATE INDEXES FOR PERFORMANCE
-- =============================================

-- Indexes for students table
CREATE INDEX idx_students_program ON students(program_id);
CREATE INDEX idx_students_email ON students(email);
CREATE INDEX idx_students_name ON students(first_name, last_name);

-- Indexes for student_programs table
CREATE INDEX idx_student_programs_student ON student_programs(student_id);
CREATE INDEX idx_student_programs_program ON student_programs(program_id);
CREATE INDEX idx_student_programs_year ON student_programs(academic_year);

-- Indexes for subjects table
CREATE INDEX idx_subjects_program ON subjects(program_id);
CREATE INDEX idx_subjects_semester ON subjects(semester);

-- Indexes for grades table
CREATE INDEX idx_grades_student ON grades(student_id);
CREATE INDEX idx_grades_exam ON grades(exam_id);
CREATE INDEX idx_grades_date ON grades(grade_date);

-- Indexes for exams table
CREATE INDEX idx_exams_subject ON exams(subject_id);
CREATE INDEX idx_exams_teacher ON exams(teacher_id);
CREATE INDEX idx_exams_date ON exams(exam_date);

-- Indexes for teacher_subjects table
CREATE INDEX idx_teacher_subjects_teacher ON teacher_subjects(teacher_id);
CREATE INDEX idx_teacher_subjects_subject ON teacher_subjects(subject_id);

-- Indexes for users table
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_type ON users(user_type);
CREATE INDEX idx_users_student ON users(student_id);
CREATE INDEX idx_users_teacher ON users(teacher_id);

-- =============================================
-- CREATE STORED PROCEDURES
-- =============================================

-- Procedure: Calculate student average for a subject
DELIMITER //
CREATE PROCEDURE CalculateStudentSubjectAverage(
    IN student_id_param INT,
    IN subject_id_param INT,
    OUT average_score DECIMAL(4,2)
)
BEGIN
    SELECT AVG(g.score) INTO average_score
    FROM grades g
    JOIN exams e ON g.exam_id = e.exam_id
    WHERE g.student_id = student_id_param 
    AND e.subject_id = subject_id_param;
END //
DELIMITER ;

-- Procedure: Get teacher's students
DELIMITER //
CREATE PROCEDURE GetTeacherStudents(IN teacher_id_param INT)
BEGIN
    SELECT DISTINCT s.student_id, s.first_name, s.last_name, p.program_name
    FROM students s
    JOIN grades g ON s.student_id = g.student_id
    JOIN exams e ON g.exam_id = e.exam_id
    JOIN subjects sub ON e.subject_id = sub.subject_id
    JOIN teacher_subjects ts ON sub.subject_id = ts.subject_id
    WHERE ts.teacher_id = teacher_id_param
    ORDER BY s.first_name, s.last_name;
END //
DELIMITER ;

-- Procedure: Update student final status
DELIMITER //
CREATE PROCEDURE UpdateStudentStatus(IN student_id_param INT)
BEGIN
    DECLARE avg_grade DECIMAL(4,2);
    
    -- Calculate overall average
    SELECT AVG(score) INTO avg_grade
    FROM grades
    WHERE student_id = student_id_param;
    
    -- Update final status based on average
    IF avg_grade >= 10 THEN
        UPDATE students SET final_status = 'ADMIS' WHERE student_id = student_id_param;
    ELSEIF avg_grade >= 7 THEN
        UPDATE students SET final_status = 'REDOUBLANT' WHERE student_id = student_id_param;
    ELSE
        UPDATE students SET final_status = 'EXCLU' WHERE student_id = student_id_param;
    END IF;
END //
DELIMITER ;

-- =============================================
-- CREATE TRIGGERS
-- =============================================

-- Trigger: Auto-create user for new student
DELIMITER //
CREATE TRIGGER after_student_insert
AFTER INSERT ON students
FOR EACH ROW
BEGIN
    INSERT INTO users (username, password_hash, user_type, student_id, email)
    VALUES (
        LOWER(CONCAT(NEW.first_name, '.', NEW.last_name)),
        'password', -- default password
        'STUDENT',
        NEW.student_id,
        NEW.email
    );
END //
DELIMITER ;

-- Trigger: Update student credits when grade is inserted
DELIMITER //
CREATE TRIGGER after_grade_insert
AFTER INSERT ON grades
FOR EACH ROW
BEGIN
    DECLARE subject_credits INT;
    DECLARE exam_score DECIMAL(4,2);
    
    -- Get subject credits and exam score
    SELECT s.credits, NEW.score INTO subject_credits, exam_score
    FROM subjects s
    JOIN exams e ON s.subject_id = e.subject_id
    WHERE e.exam_id = NEW.exam_id;
    
    -- If student passed (score >= 10), add credits
    IF exam_score >= 10 THEN
        UPDATE students 
        SET total_credits_earned = total_credits_earned + subject_credits
        WHERE student_id = NEW.student_id;
    END IF;
END //
DELIMITER ;

-- Trigger: Set current academic year
DELIMITER //
CREATE TRIGGER before_academic_year_insert
BEFORE INSERT ON academic_years
FOR EACH ROW
BEGIN
    -- If new year is set as current, unset all others
    IF NEW.is_current = TRUE THEN
        UPDATE academic_years SET is_current = FALSE;
    END IF;
END //
DELIMITER ;

-- =============================================
-- FINAL MESSAGE
-- =============================================

SELECT 'Database schema created successfully!' as message;
SELECT COUNT(*) as total_programs FROM programs;
SELECT COUNT(*) as total_students FROM students;
SELECT COUNT(*) as total_teachers FROM teachers;
SELECT COUNT(*) as total_subjects FROM subjects;
SELECT COUNT(*) as total_student_programs FROM student_programs;