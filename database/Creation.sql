USE gestion_scolarite;


-- الطلاب (Updated to match Java code expectations)
CREATE TABLE students (
    student_id INT PRIMARY KEY AUTO_INCREMENT,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    school_origin VARCHAR(100) NOT NULL, -- Changed from ENUM to VARCHAR
    email VARCHAR(100),
    phone VARCHAR(20),
    program_id INT NULL, -- Added this field
    academic_year VARCHAR(20) NULL, -- Added this field
    final_status VARCHAR(50) NULL, -- Added this field
    registration_date DATE DEFAULT CURRENT_DATE,
    FOREIGN KEY (program_id) REFERENCES programs(program_id)
);

-- البرامج (Keep as is)
CREATE TABLE programs (
    program_id INT PRIMARY KEY AUTO_INCREMENT,
    program_name VARCHAR(100) NOT NULL,
    program_year INT NOT NULL,
    description TEXT,
    prerequisite_id INT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (prerequisite_id) REFERENCES programs(program_id)
);

-- المواد (Keep as is)
CREATE TABLE subjects (
    subject_id INT PRIMARY KEY AUTO_INCREMENT,
    subject_name VARCHAR(100) NOT NULL,
    objectives TEXT,
    semester INT NOT NULL,
    coefficient DECIMAL(3,2) NOT NULL,
    program_id INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (program_id) REFERENCES programs(program_id)
);

-- الأساتذة (Keep as is)
CREATE TABLE teachers (
    teacher_id INT PRIMARY KEY AUTO_INCREMENT,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100),
    specialty VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Add the missing teacher_subjects table
CREATE TABLE teacher_subjects (
    teacher_subject_id INT PRIMARY KEY AUTO_INCREMENT,
    teacher_id INT NOT NULL,
    subject_id INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (teacher_id) REFERENCES teachers(teacher_id),
    FOREIGN KEY (subject_id) REFERENCES subjects(subject_id),
    UNIQUE KEY unique_teacher_subject (teacher_id, subject_id)
);

-- الاختبارات (Updated column names to match Java code)
CREATE TABLE exams (
    exam_id INT PRIMARY KEY AUTO_INCREMENT,
    exam_name VARCHAR(100) NOT NULL, -- Changed order
    subject_id INT NOT NULL,
    exam_type ENUM('CONTROLE', 'EXAMEN', 'PROJET', 'TP') NOT NULL, -- Changed values to match Java
    coefficient DECIMAL(3,2) NOT NULL,
    teacher_id INT NOT NULL,
    creation_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- Changed from exam_date, created_at
    FOREIGN KEY (subject_id) REFERENCES subjects(subject_id),
    FOREIGN KEY (teacher_id) REFERENCES teachers(teacher_id)
);

-- السنوات الدراسية (Keep as is)
CREATE TABLE academic_years (
    year_id INT PRIMARY KEY AUTO_INCREMENT,
    start_year INT NOT NULL,
    end_year INT NOT NULL,
    is_current BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- التسجيلات (Keep as is, but Java code might not use this)
CREATE TABLE registrations (
    registration_id INT PRIMARY KEY AUTO_INCREMENT,
    student_id INT NOT NULL,
    program_id INT NOT NULL,
    year_id INT NOT NULL,
    registration_date DATE,
    final_status ENUM('ADMIS', 'REDOUBLANT', 'EXCLU') NULL,
    overall_average DECIMAL(4,2) NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES students(student_id),
    FOREIGN KEY (program_id) REFERENCES programs(program_id),
    FOREIGN KEY (year_id) REFERENCES academic_years(year_id)
);

-- الدرجات (Updated column names)
CREATE TABLE grades (
    grade_id INT PRIMARY KEY AUTO_INCREMENT,
    student_id INT NOT NULL,
    exam_id INT NOT NULL,
    score DECIMAL(4,2) NOT NULL,
    grade_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- Changed from entry_date, created_at
    FOREIGN KEY (student_id) REFERENCES students(student_id),
    FOREIGN KEY (exam_id) REFERENCES exams(exam_id),
    UNIQUE KEY unique_student_exam (student_id, exam_id)
);

-- المستخدمين (Keep as is)
CREATE TABLE users (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    user_type ENUM('STUDENT', 'TEACHER', 'ADMIN', 'RESPONSABLE') NOT NULL,
    student_id INT NULL,
    teacher_id INT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES students(student_id),
    FOREIGN KEY (teacher_id) REFERENCES teachers(teacher_id)
);

-- إدراج بيانات تجريبية (Updated)
INSERT INTO academic_years (start_year, end_year, is_current) VALUES 
(2024, 2025, TRUE);

INSERT INTO programs (program_name, program_year, description) VALUES 
('ING1 TC', 1, 'Tronc Commun Première Année'),
('ING2 GI', 2, 'Génie Informatique'),
('ING3 GL', 3, 'Génie Logiciel');

INSERT INTO students (first_name, last_name, school_origin, email, phone, program_id, academic_year) VALUES 
('Ahmed', 'Mohamed', 'DUT', 'ahmed@email.com', '0612345678', 1, '2024-2025'),
('Fatima', 'Zahra', 'CPGE', 'fatima@email.com', '0622345678', 1, '2024-2025'),
('Youssef', 'Ali', 'CPI', 'youssef@email.com', '0632345678', 1, '2024-2025');

INSERT INTO teachers (first_name, last_name, email, specialty) VALUES 
('Dr. Karim', 'Benzema', 'karim@email.com', 'Informatique'),
('Prof. Leila', 'Tunis', 'leila@email.com', 'Mathématiques');

INSERT INTO subjects (subject_name, objectives, semester, coefficient, program_id) VALUES 
('Programmation Java', 'Maîtriser la POO en Java', 1, 1.5, 1),
('Base de Données', 'Concevoir et manipuler des BDD', 1, 1.2, 1),
('Mathématiques', 'Algèbre et Analyse', 2, 1.0, 1);

-- Add teacher-subject relationships
INSERT INTO teacher_subjects (teacher_id, subject_id) VALUES 
(1, 1), -- Karim teaches Java
(1, 2), -- Karim teaches Databases
(2, 3); -- Leila teaches Mathematics

INSERT INTO exams (exam_name, subject_id, exam_type, coefficient, teacher_id) VALUES 
('Examen Final Java', 1, 'EXAMEN', 0.6, 1),
('Contrôle POO', 1, 'CONTROLE', 0.4, 1),
('Projet BDD', 2, 'PROJET', 1.0, 1);

INSERT INTO users (username, password_hash, user_type, student_id, teacher_id) VALUES 
('ahmed', 'password', 'STUDENT', 1, NULL),
('fatima', 'password', 'STUDENT', 2, NULL),
('youssef', 'password', 'STUDENT', 3, NULL),
('prof1', 'password', 'TEACHER', NULL, 1),
('prof2', 'password', 'TEACHER', NULL, 2),
('admin', 'password', 'ADMIN', NULL, NULL),
('resp', 'password', 'RESPONSABLE', NULL, NULL);

INSERT INTO grades (student_id, exam_id, score) VALUES 
(1, 1, 16.5),
(1, 2, 14.0),
(2, 1, 18.0),
(2, 3, 17.5),
(3, 1, 12.5);